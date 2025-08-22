package net.crizo.rtcextras.procedures;

import org.checkerframework.checker.units.qual.t;
import org.checkerframework.checker.units.qual.s;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;

import net.crizo.rtcextras.init.RtcExtrasModItems;
import net.crizo.rtcextras.BacteriaGrowthCalculator;

public class BiocultivatorOnTickUpdateProcedure {
	// ---- Tunables ----
	private static final int BACTERIA_PER_ITEM = 1000; // cells contributed by each input bacterium item
	private static final int MAX_EXPORT_STACK = 64; // max items per export
	private static final int DEFAULT_CAPACITY = 64_000_000; // hard cap
	private static final double CONTAM_HARM_FRAC = 0.00025; // 0.025% per bad slot per tick (gentle)
	private static final TagKey<Item> TAG_CULTIVATOR_FOOD = TagKey.create(Registries.ITEM, ResourceLocation.parse("rtc_extras:cultivator_food"));
	// “what counts as perishable” for auto-stabilize
	private static final TagKey<Item> TAG_PERISHABLE = TagKey.create(Registries.ITEM, ResourceLocation.parse("rtc_extras:perishable"));

	public static void execute(LevelAccessor world, double x, double y, double z) {
		BlockPos pos = BlockPos.containing(x, y, z);
		var be = world.getBlockEntity(pos);
		if (be == null)
			return;
		CompoundTag tag = be.getPersistentData();
		// Inventory handler
		IItemHandler handler = null;
		if (world instanceof ILevelExtension ext) {
			handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
		}
		if (handler == null)
			return;
		int capacity = tag.contains("bact_capacity") ? tag.getInt("bact_capacity") : DEFAULT_CAPACITY;
		int current = Math.max(0, tag.getInt("bact_count"));
		// ========= 1) INPUT (slot 0): seed/extend matching strain, researched-only =========
		if (handler.getSlots() > 0) {
			ItemStack in = handler.getStackInSlot(0);
			if (!in.isEmpty() && in.getItem() == RtcExtrasModItems.BACTERIUM.get()) {
				CompoundTag it = in.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
				if (!it.getBoolean("researched")) {
					setStatus(tag, "Rejected: sample not researched");
				} else {
					String inputIds = parseGeneIdsString(it); // "id" or "id1+id2+id3"
					if (!inputIds.isEmpty()) {
						String activeIds = tag.getString("culture_gene_ids");
						if (activeIds.isEmpty() || current <= 0) {
							int add = in.getCount() * BACTERIA_PER_ITEM;
							current = Math.min(capacity, current + add);
							tag.putString("culture_gene_ids", inputIds);
							tag.putString("culture_gene_id_raw", it.getString("rtc_gene_id"));
							tag.putString("culture_gene_name", it.getString("rtc_gene_name"));
							tag.putString("culture_gene_tier", it.getString("rtc_gene_tier"));
							tag.putString("culture_gene_effect", it.getString("rtc_gene_effect"));
							tag.putFloat("culture_gene_interference", it.getFloat("rtc_gene_interference"));
							if (handler instanceof IItemHandlerModifiable mod)
								mod.setStackInSlot(0, ItemStack.EMPTY);
							setStatus(tag, "Loaded new strain");
						} else if (activeIds.equals(inputIds)) {
							int add = in.getCount() * BACTERIA_PER_ITEM;
							current = Math.min(capacity, current + add);
							if (handler instanceof IItemHandlerModifiable mod)
								mod.setStackInSlot(0, ItemStack.EMPTY);
							setStatus(tag, "Extended strain");
						} else {
							setStatus(tag, "Rejected mismatched strain");
						}
					} else {
						setStatus(tag, "Invalid bacterium (no genes)");
					}
				}
			}
		}
		// ========= 2) CONTAMINATION (slots 2..7): gentle background harm =========
		int badSlots = 0;
		for (int i = 2; i <= 7 && i < handler.getSlots(); i++) {
			ItemStack st = handler.getStackInSlot(i);
			if (st == null || st.isEmpty())
				continue;
			if (!isAcceptableFood(st))
				badSlots++;
		}
		if (badSlots > 0 && current > 0) {
			int harm = Math.max(0, (int) Math.floor(current * (CONTAM_HARM_FRAC * badSlots)));
			if (harm > 0) {
				current = Math.max(0, current - harm);
				setStatus(tag, "Contamination penalty (-" + harm + ")");
			}
		}
		// ========= 2.5) STABILIZE PERISHABLE FOOD IN SLOTS 2..7 =========
		stabilizePerishablesInFoodSlots(world, handler);
		// ========= 3) GROWTH / DECAY (calculator consumes <=1 food item/tick) =========
		String[] genes = readActiveGenes(tag);
		if (genes != null && genes.length > 0 && !genes[0].isEmpty()) {
			int delta = BacteriaGrowthCalculator.computeDelta(world, pos, handler, current, capacity, genes, tag);
			current = Math.max(0, Math.min(capacity, current + delta));
		} else {
			// still allow substrate & waste drift handled inside calculator when N==0
			BacteriaGrowthCalculator.computeDelta(world, pos, handler, 0, capacity, new String[0], tag);
			if (current <= 0)
				setStatus(tag, "Idle (no culture)");
		}
		// ========= 4) EXPORT (slot 1) =========
		boolean request = tag.getBoolean("requestBacteria");
		if (request) {
			boolean exported = tryExport(world, handler, tag, current);
			tag.putBoolean("requestBacteria", false);
			if (exported)
				current = Math.max(0, tag.getInt("bact_count"));
		}
		// ========= 5) Persist + sync =========
		tag.putInt("bact_count", current);
		if (!world.isClientSide() && world instanceof Level lvl) {
			BlockState bs = lvl.getBlockState(pos);
			lvl.sendBlockUpdated(pos, bs, bs, 3);
		}
	}

	// ------ Export logic ------
	private static boolean tryExport(LevelAccessor world, IItemHandler handler, CompoundTag tag, int current) {
		if (!(handler instanceof IItemHandlerModifiable mod)) {
			setStatus(tag, "No inventory (export failed)");
			return false;
		}
		if (current <= 0) {
			setStatus(tag, "No culture to export");
			return false;
		}
		ItemStack out = mod.getStackInSlot(1);
		if (!out.isEmpty()) {
			setStatus(tag, "Output slot occupied");
			return false;
		}
		int items = Math.min(MAX_EXPORT_STACK, current / BACTERIA_PER_ITEM);
		if (items <= 0) {
			setStatus(tag, "Not enough population to form a dose");
			return false;
		}
		ItemStack stack = new ItemStack(RtcExtrasModItems.BACTERIUM.get(), items);
		CompoundTag it = new CompoundTag();
		// Strain info
		String ids = tag.getString("culture_gene_ids");
		String idRaw = tag.getString("culture_gene_id_raw");
		String name = tag.getString("culture_gene_name");
		String tier = tag.getString("culture_gene_tier");
		String effect = tag.getString("culture_gene_effect");
		float interf = tag.getFloat("culture_gene_interference");
		String rtcGeneId = !idRaw.isEmpty() ? idRaw : (ids.contains("+") ? "composite(" + ids + ")" : ids);
		it.putString("rtc_gene_id", rtcGeneId);
		it.putString("rtc_gene_name", name.isEmpty() ? "Cultured Strain" : name);
		it.putString("rtc_gene_tier", tier.isEmpty() ? "common" : tier);
		if (!effect.isEmpty())
			it.putString("rtc_gene_effect", effect);
		it.putFloat("rtc_gene_interference", interf);
		it.putBoolean("rtc_stabilized", true);
		it.putBoolean("researched", true);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(it));
		mod.setStackInSlot(1, stack);
		int newCount = Math.max(0, current - items * BACTERIA_PER_ITEM);
		tag.putInt("bact_count", newCount);
		setStatus(tag, "Exported x" + items);
		if (world instanceof Level lvl && !lvl.isClientSide()) {
			lvl.addFreshEntity(new ExperienceOrb(lvl, posCenter(0.5), posCenter(1.0), posCenter(0.5), 0));
		}
		return true;
	}

	// ------ Helpers ------
	/** 
	* For slots 2..7: if an item is tagged rtc_extras:perishable, mark it stabilized and
	* refresh its creation timestamp so it has a full frozen lifetime while in the machine.
	*/
	private static void stabilizePerishablesInFoodSlots(LevelAccessor world, IItemHandler handler) {
		if (!(world instanceof Level lvl))
			return;
		final long now = lvl.getGameTime();
		final String TAG_CREATED = "rtc_created";
		final String TAG_STABILIZED = "rtc_stabilized";
		final String TAG_FROZEN_LEFT = "rtc_frozen_left";
		// same lifetime math as your BacteriumItem/PerishableMerge
		final long PER_ITEM_TICKS = 750L; // 48,000 / 64
		for (int i = 2; i <= 7 && i < handler.getSlots(); i++) {
			ItemStack st = handler.getStackInSlot(i);
			if (st.isEmpty())
				continue;
			if (!st.is(TAG_PERISHABLE))
				continue; // only items explicitly tagged as perishable
			// Compute a full lifetime for the current count, freeze it, and stamp created=now
			long fullFrozen = PER_ITEM_TICKS * Math.max(1, st.getCount());
			CompoundTag t = st.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			t.putBoolean(TAG_STABILIZED, true);
			t.putLong(TAG_FROZEN_LEFT, fullFrozen);
			// "Updating their creation date" — set to now.
			// If you prefer "only set if missing", replace with:
			// if (!t.contains(TAG_CREATED) || t.getLong(TAG_CREATED) == 0L) t.putLong(TAG_CREATED, now);
			t.putLong(TAG_CREATED, now);
			st.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
			// no need to replace the stack in the slot; ItemStack mutation is enough
			// but if you’d rather be explicit:
			if (handler instanceof IItemHandlerModifiable mod)
				mod.setStackInSlot(i, st);
		}
	}

	private static String parseGeneIdsString(CompoundTag itemTag) {
		if (itemTag == null)
			return "";
		String combo = itemTag.getString("rtc_gene_ids_combo");
		if (combo != null && !combo.isEmpty())
			return combo;
		String id = itemTag.getString("rtc_gene_id");
		if (id == null || id.isEmpty())
			return "";
		if (id.startsWith("composite(") && id.endsWith(")")) {
			return id.substring("composite(".length(), id.length() - 1);
		}
		return id;
	}

	private static String[] readActiveGenes(CompoundTag tag) {
		String ids = tag.getString("culture_gene_ids");
		if (ids == null || ids.isEmpty())
			return new String[0];
		String[] split = ids.split("\\+");
		if (split.length <= 3)
			return split;
		String[] out = new String[3];
		System.arraycopy(split, 0, out, 0, 3);
		return out;
	}

	/** Acceptable food if edible OR tagged as rtc_extras:cultivator_food OR explicitly registered. */
	private static boolean isAcceptableFood(ItemStack st) {
		if (st == null || st.isEmpty())
			return false;
		if (st.get(DataComponents.FOOD) != null)
			return true;
		if (st.is(TAG_CULTIVATOR_FOOD))
			return true;
		// If calculator has an explicit registry entry, accept as food here as well
		// (we can’t read its private map; fallback to tagged/edible for now)
		return false;
	}

	private static void setStatus(CompoundTag tag, String s) {
		if (tag != null)
			tag.putString("bact_status", s == null ? "" : s);
	}

	private static double posCenter(double v) {
		return v;
	} // cosmetic only
}