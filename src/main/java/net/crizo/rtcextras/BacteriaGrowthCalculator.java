package net.crizo.rtcextras;

import org.checkerframework.checker.units.qual.t;
import org.checkerframework.checker.units.qual.s;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.IItemHandler;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;

import java.util.function.IntPredicate;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

/**
 * Simple, stable growth model:
 * - Substrate units ("credits") pool in NBT: 'bact_substrate'
 * - At most 1 item from slots 2..end is consumed per tick -> adds substrate units
 * - Genes add photo/chemo trickles in units/tick
 * - Growth this tick is min(μ * N * limits, substrateUnits * yield), then integer-rounded at the end
 * - Waste rises with growth, decays gently, and slows growth via fWaste
 * Returns integer delta (can be negative). SERVER only.
 */
public final class BacteriaGrowthCalculator {
	// ---------- Food registry (item -> substrate UNITS per item) ----------
	private static final Map<Item, Integer> SUBSTRATE_UNITS = new HashMap<>();
	private static final TagKey<Item> TAG_CULTIVATOR_FOOD = TagKey.create(Registries.ITEM, ResourceLocation.parse("rtc_extras:cultivator_food"));
	private static final int TAG_DEFAULT_UNITS = 300; // tagged, non-edible fallback
	static {
		// Vanilla examples (you can register more at runtime)
		SUBSTRATE_UNITS.put(Items.SUGAR, 200);
		SUBSTRATE_UNITS.put(Items.WHEAT, 240);
		SUBSTRATE_UNITS.put(Items.BREAD, 1000);
		SUBSTRATE_UNITS.put(Items.POTATO, 300);
		SUBSTRATE_UNITS.put(Items.BEETROOT, 240);
		SUBSTRATE_UNITS.put(Items.APPLE, 400);
	}
	/** Edible fallback → substrate units from FoodProperties. */
	private static final double FOOD_NUTRITION_MULT = 80.0;
	private static final double FOOD_SAT_MULT = 100.0;
	// ---------- Waste tuning ----------
	private static final double WASTE_DECAY_FRAC = 0.005; // 0.5 % / tick
	private static final double WASTE_DECAY_CONST = 2.0; // +2 / tick
	private static final double WASTE_DILUTE_PER_ITEM = 0.02; // 2% when we ingest a fresh item
	// ---------- NBT keys ----------
	private static final String KEY_SUBSTRATE = "bact_substrate"; // double (substrate units)
	private static final String KEY_WASTE = "bact_waste"; // double
	private static final String KEY_ACC = "bact_acc"; // double (fractional population carry)

	/** Public hook for mods to register custom foods (e.g., Jelly). */
	public static void registerSubstrate(Item item, int unitsPerItem) {
		SUBSTRATE_UNITS.put(Objects.requireNonNull(item), Math.max(1, unitsPerItem));
	}

	// ---------- Public API ----------
	/** Default slot policy: treat every slot except 0 and 1 as food slots. */
	public static int computeDelta(LevelAccessor world, BlockPos pos, IItemHandler inv, int current, int capacity, String[] geneIds, CompoundTag beTag) {
		IntPredicate foodSlots = i -> inv != null && i >= 2 && i < inv.getSlots();
		return computeDelta(world, pos, inv, foodSlots, current, capacity, geneIds, beTag);
	}

	/** Core compute with custom slot filter. */
	public static int computeDelta(LevelAccessor world, BlockPos pos, IItemHandler inv, IntPredicate foodSlotFilter, int current, int capacity, String[] geneIds, CompoundTag tag) {
		if (world == null || world.isClientSide())
			return 0;
		// Build strain profile (baseline + up to 3 genes)
		GeneGrowthData.CombinedProfile p = GeneGrowthData.combine(GeneGrowthData.Baseline.balanced(), safe(geneIds, 0), safe(geneIds, 1), safe(geneIds, 2));
		// --- Substrate pool (substrate units), waste, and accumulator ---
		double substrate = get(tag, KEY_SUBSTRATE);
		double waste = get(tag, KEY_WASTE);
		double acc = get(tag, KEY_ACC);
		// --- Ingest at most ONE food item per tick into substrate units ---
		// --- Photo/Chemo trickles FIRST (may keep us from needing to eat) ---
		double light = lightFactor(world, pos); // 0..1
		if (p.photoUnitsPerTick > 0.0)
			substrate += p.photoUnitsPerTick * light;
		if (p.chemoUnitsPerTick > 0.0)
			substrate += p.chemoUnitsPerTick;
		// --- Only eat ONE item when substrate is empty ---
		if (substrate <= 1e-6 && current > 0 && inv instanceof IItemHandlerModifiable mod) {
			for (int i = 0; i < mod.getSlots(); i++) {
				if (!foodSlotFilter.test(i))
					continue;
				ItemStack st = mod.getStackInSlot(i);
				if (st.isEmpty())
					continue;
				int units = perItemUnits(st);
				if (units <= 0)
					continue;
				// Consume exactly one item → add its units
				ItemStack copy = st.copy();
				copy.shrink(1);
				mod.setStackInSlot(i, copy.isEmpty() ? ItemStack.EMPTY : copy);
				substrate += units;
				// Fresh media slightly dilutes accumulated waste
				waste = Math.max(0.0, waste * (1.0 - WASTE_DILUTE_PER_ITEM));
				break; // only one item when pool hits zero
			}
		}

		// If no culture alive yet, just store substrate & clean waste a bit.
		if (current <= 0) {
			waste = Math.max(0.0, waste - (waste * WASTE_DECAY_FRAC + WASTE_DECAY_CONST));
			put(tag, KEY_SUBSTRATE, substrate);
			put(tag, KEY_WASTE, waste);
			setStatus(tag, "Idle (no culture)");
			return 0;
		}
		// --- Environment scalars ---
		double oxygen = oxygenFactor(world, pos, light); // 0..1
		double fOxy = clamp01(oxygen * p.oxygenUse + (1.0 - p.oxygenUse));
		double fWaste = 1.0 / (1.0 + (waste / Math.max(1.0, p.wasteTol))); // gentle, linear-ish
		double logistic = (capacity > 0) ? clamp01(1.0 - (current / Math.max(1.0, (double) capacity))) : 1.0;
		// --- Positive growth demand (cells/tick) ---
		double growthDemand = p.mu * current * fOxy * fWaste * logistic;
		// --- Credits cap: cannot grow more cells than substrate*yield allows ---
		double growthFromCredits = substrate * p.yield;
		double growthReal = Math.min(growthDemand, growthFromCredits);
		// Spend substrate used this tick
		double unitsUsed = (growthReal <= 0.0) ? 0.0 : (growthReal / Math.max(1e-9, p.yield));
		substrate = Math.max(0.0, substrate - unitsUsed);
		// Waste: add from growth, then decay a bit each tick
		waste = Math.max(0.0, waste + (growthReal * p.wasteGen));
		waste = Math.max(0.0, waste - (waste * WASTE_DECAY_FRAC + WASTE_DECAY_CONST));
		// --- Death ---
		double deathReal = p.death * current;
		// --- Net change with fractional accumulator ---
		double dX = (growthReal - deathReal) + acc;
		int delta = (int) Math.floor(dX);
		// Clamp to bounds and keep fractional remainder
		if (capacity > 0)
			delta = Math.min(delta, capacity - current);
		delta = Math.max(delta, -current);
		put(tag, KEY_ACC, dX - delta);
		// Persist substrate & waste
		put(tag, KEY_SUBSTRATE, substrate);
		put(tag, KEY_WASTE, waste);
		// Status
		if (delta > 0)
			//setStatus(tag, String.format("Growing +%d (μ=%.4f, waste=%.0f, subs=%.0f)", delta, p.mu, waste, substrate));
			setStatus(tag, String.format("Growing"));
		else if (delta < 0)
			//setStatus(tag, String.format("Dying %d (death=%.4f, waste=%.0f, subs=%.0f)", delta, p.death, waste, substrate));
			setStatus(tag, String.format("Dying"));

		else if (logistic < 0.02)
			setStatus(tag, "At capacity");
		else if (growthDemand > 0 && growthFromCredits <= 1e-6)
			setStatus(tag, "Starving");
		else
			setStatus(tag, "Holding");
		// Debug once per second
		/*
		if (world instanceof Level lvl && (lvl.getGameTime() % 20L) == 0L) {
			net.crizo.rtcextras.RtcExtrasMod.LOGGER.info(String.format("Cultivator @%s N=%d d=%d subs=%.1f waste=%.1f oxy=%.2f", pos.toShortString(), current, delta, substrate, waste, oxygen));
		}
		*/
		return delta;
	}

	// ---------- Helpers ----------
	private static String safe(String[] arr, int i) {
		return (arr != null && i >= 0 && i < arr.length) ? arr[i] : null;
	}

	private static double lightFactor(LevelAccessor world, BlockPos pos) {
		int l = 0;
		if (world instanceof Level lvl)
			l = lvl.getMaxLocalRawBrightness(pos);
		return clamp01(l / 15.0);
	}

	private static double oxygenFactor(LevelAccessor world, BlockPos pos, double light) {
		// Light as a crude ventilation proxy
		return clamp01(0.5 + 0.5 * light);
	}

	private static int perItemUnits(ItemStack st) {
		Item item = st.getItem();
		Integer reg = SUBSTRATE_UNITS.get(item);
		if (reg != null && reg > 0)
			return reg;
		// edible fallback
		FoodProperties fp = st.get(DataComponents.FOOD);
		if (fp != null) {
			double units = FOOD_NUTRITION_MULT * fp.nutrition() + FOOD_SAT_MULT * fp.saturation();
			return (int) Math.max(1, Math.round(units));
		}
		// tagged non-edible fallback
		if (st.is(TAG_CULTIVATOR_FOOD))
			return TAG_DEFAULT_UNITS;
		return 0;
	}

	private static double get(CompoundTag t, String k) {
		return t != null ? t.getDouble(k) : 0.0;
	}

	private static void put(CompoundTag t, String k, double v) {
		if (t != null)
			t.putDouble(k, v);
	}

	private static void setStatus(CompoundTag t, String s) {
		if (t != null)
			t.putString("bact_status", s == null ? "" : s);
	}

	private static double clamp01(double v) {
		return v < 0 ? 0 : (v > 1 ? 1 : v);
	}

	private BacteriaGrowthCalculator() {
	}
}