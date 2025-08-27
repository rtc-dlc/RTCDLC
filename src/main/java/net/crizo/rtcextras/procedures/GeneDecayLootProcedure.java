package net.crizo.rtcextras.procedures;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import net.crizo.rtcextras.item.BacteriumItem;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

public class GeneDecayLootProcedure {
	// ===== Defaults / knobs =====
	private static final double DEFAULT_PER_ITEM_CHANCE = 0.05; // 5% per bacterium item in the stack
	// Simple output definition: registry id + chance per item (0 => use default)

	private record DecayOutput(String itemId, double chancePerItem) {
	}

	/** Back-compat: call with entity position. */
	public static void onBacteriumDecay(Level level, Entity where, ItemStack bacteriumStack) {
		if (where == null)
			return;
		onBacteriumDecay(level, where.getX(), where.getY() + 0.25, where.getZ(), where, bacteriumStack);
	}

	/** Call this BEFORE the stack is destroyed. Provides (x,y,z) for damage/FX cases. */
	public static void onBacteriumDecay(Level level, double x, double y, double z, Entity source, ItemStack bacteriumStack) {
		if (level.isClientSide() || bacteriumStack.isEmpty())
			return;
		var tag = BacteriumItem.getTag(bacteriumStack);
		String geneId = tag.getString("rtc_gene_id"); // may be ""
		int stackCount = Math.max(0, bacteriumStack.getCount());
		if (stackCount <= 0)
			return;
		RandomSource rng = level.getRandom();
		// 1) Side effects (fire, damage, small boom, aura) — per-gene probabilities below
		applySideEffects(level, x, y, z, source, geneId, stackCount, rng);
		// 2) Item drops (accumulate totals for nicer stacks)
		List<DecayOutput> outputs = getOutputsForGene(geneId);
		if (outputs.isEmpty())
			return;
		Map<Item, Integer> totals = new HashMap<>();
		for (DecayOutput out : outputs) {
			Item item = resolve(out.itemId());
			if (item == null)
				continue;
			double p = out.chancePerItem() > 0 ? out.chancePerItem() : DEFAULT_PER_ITEM_CHANCE;
			int drops = 0;
			for (int i = 0; i < stackCount; i++) {
				if (rng.nextDouble() < p)
					drops++;
			}
			if (drops > 0)
				totals.merge(item, drops, Integer::sum);
		}
		for (Map.Entry<Item, Integer> e : totals.entrySet()) {
			spawnStacked(level, x, y, z, e.getKey(), e.getValue());
		}
	}

	// ===== Side effects per gene (probabilities are per *stack*, scale by stackCount) =====
	private static void applySideEffects(Level level, double x, double y, double z, Entity source, String geneId, int stackCount, RandomSource rng) {
		if (geneId == null)
			geneId = "";
		switch (geneId) {
			// --- player/environment safety style effects ---
			case "thermogenesis" -> {
				// Rarely ignite the holder on decay. Chance scales with stack size but capped.
				// Example: 0.5% * stackCount, capped at 10%. Burn 3s.
				double p = Math.min(0.005 * stackCount, 0.10);
				if (source instanceof LivingEntity le && rng.nextDouble() < p) {
					le.igniteForSeconds(3);
				}
				// tiny warmth "pop": nearby mobs (r=3) get brief fire 1s at much lower chance
				if (rng.nextDouble() < p * 0.25) {
					for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(x - 3, y - 3, z - 3, x + 3, y + 3, z + 3))) {
						if (e == source)
							continue;
						e.igniteForSeconds(1);
					}
				}
			}
			case "venom_synthesis" -> {
				// Short poison burst nearby (radius 3), very rare.
				// 0.25% * stackCount (cap 5%) -> Poison I for 4s
				double p = Math.min(0.0025 * stackCount, 0.05);
				if (rng.nextDouble() < p) {
					for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(x - 3, y - 3, z - 3, x + 3, y + 3, z + 3))) {
						if (e == source)
							continue;
						e.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
					}
				}
			}
			case "neuroinhibitor_aura" -> {
				// Weakness aura (radius 4) at tiny chance.
				// 0.2% * stackCount (cap 4%) -> Weakness I for 6s
				double p = Math.min(0.002 * stackCount, 0.04);
				if (rng.nextDouble() < p) {
					for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(x - 4, y - 4, z - 4, x + 4, y + 4, z + 4))) {
						if (e == source)
							continue;
						e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));
					}
				}
			}
			case "nitration_pack" -> {
				// Teensy pop explosion rarely (no block damage). 0.1% * stackCount (cap 2%).
				double p = Math.min(0.001 * stackCount, 0.02);
				if (rng.nextDouble() < p) {
					level.explode(source, x, y, z, 0.6f, Level.ExplosionInteraction.NONE); // harmless to blocks
				}
			}
			case "radiant_bioluminescence" -> {
				// Glare: brief blindness nearby at low chance.
				// 0.2% * stackCount (cap 4%) -> Blindness 2s
				double p = Math.min(0.002 * stackCount, 0.04);
				if (rng.nextDouble() < p) {
					for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(x - 3, y - 3, z - 3, x + 3, y + 3, z + 3))) {
						if (e == source)
							continue;
						e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
					}
				}
			}
			case "adrenaline_trigger" -> {
				// Short Speed I on the holder at small chance.
				double p = Math.min(0.003 * stackCount, 0.06);
				if (source instanceof LivingEntity le && rng.nextDouble() < p) {
					le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 80, 0));
				}
			}
			// many other genes: no decay-time side effects (drops only / none). Add later if desired.
			default -> {
				/* no special decay side effects */ }
		}
	}

	// ===== Item drops per gene (all ids from your JSON wired; customize registry ids as you add items) =====
	private static List<DecayOutput> getOutputsForGene(String geneId) {
		if (geneId == null)
			geneId = "";
		return switch (geneId) {
			// ---------- COMMON ----------
			case "hypernatremia" -> List.of(io("rtc:sodium_chloride", 0.03));
			case "hypermetabolism" -> List.of();
			case "photosynthesis" -> List.of(io("minecraft:sugar", 0.03));
			case "detox_enzyme" -> List.of();
			case "fermentation_pathway" -> List.of();
			case "basic_healing_factor" -> List.of();
			case "spore_resistance" -> List.of();
			case "heat_shock_protein" -> List.of();
			case "cold_tolerance" -> List.of();
			case "chitin_layer" -> List.of(io("minecraft:turtle_scute", 0.02));
			case "bioluminescence" -> List.of();
			case "lactate_cycle" -> List.of();
			case "acetobacter_lineage" -> List.of();
			case "methylotrophy" -> List.of();
			case "cellulose_synthase" -> List.of(io("rtc:item_plant_fiber", 0.03));
			case "penicillin_like" -> List.of();
			case "pectinase_expression" -> List.of();
			case "amylase_pack" -> List.of();
			case "slime_polysaccharide" -> List.of(io("minecraft:slimeball", 0.04));
			case "vitamin_pathway_basic" -> List.of();
			case "aquagenic_urticaria" -> List.of();
			case "cancerous_growth" -> List.of();
			case "sickle_cell_disease" -> List.of();
			case "osteogenesis_imperfecta" -> List.of();
			// ---------- RARE ----------
			case "neurotransmitter_boost" -> List.of();
			//eventually makes LDOPA and Serotonin
			case "hemoglobin_variant" -> List.of();
			case "adrenaline_trigger" -> List.of();
			case "venom_synthesis" -> List.of();
			case "silk_spinneret" -> List.of(io("minecraft:string", 0.04));
			case "chloroplast_optimization" -> List.of();
			case "burst_regeneration" -> List.of();
			case "toxin_resistance" -> List.of();
			case "thermogenesis" -> List.of();
			case "kinetic_muscle" -> List.of();
			case "polycarbonate_pathway" -> List.of(io("rtc_extras:polycarbonate_pellet", 0.03));
			case "carbon_precursors" -> List.of(io("rtc:item_carbon_nanotubes", 0.01));
			case "nitration_pack" -> List.of();
			case "broad_spectrum_antibiotic" -> List.of();
			case "growth_hormone_line" -> List.of(io("rtc:item_polystyrene", 0.015));
			case "styrogenesis" -> List.of(io("rtc:styrene"));
			case "bioplastic_refinery" -> List.of();
			case "biolubricant_chain" -> List.of();
			case "antifungal_serum" -> List.of();
			case "collagen_weave" -> List.of();
			case "chemoautotrophy_pack" -> List.of(io("rtc:niter", 0.04));
			case "fibroblast_growth" -> List.of();
			case "hemophilia" -> List.of();
			// ---------- EXOTIC ----------
			case "neural_overclock" -> List.of();
			case "quantum_enzyme" -> List.of();
			case "extreme_regeneration" -> List.of();
			case "adaptive_chitin" -> List.of(io("minecraft:turtle_scute", 0.045));
			case "mycelial_symbiosis" -> List.of();
			case "oxygen_synthesis" -> List.of();
			case "radiant_bioluminescence" -> List.of();
			case "neuroinhibitor_aura" -> List.of();
			case "immortal_cell_cycle" -> List.of();
			case "nanocellulose_forge" -> List.of();
			case "biophotonic_array" -> List.of();
			case "alien_neurotoxin_line" -> List.of();
			case "bio_dynamism_cascade" -> List.of();
			case "advanced_nootropics" -> List.of();
			case "protein_scaffold_fab" -> List.of();
			case "phase_change_mucus" -> List.of();
			case "electrocyte_array" -> List.of();
			case "hardened_carapace" -> List.of();
			case "photoshade_negentropy" -> List.of();
			case "void_pressure_tolerance" -> List.of();
			case "prion_instability" -> List.of();
			// ===== default/testing =====
			default -> List.of(); // or: List.of(io("minecraft:diamond"))
		};
	}

	// === helpers ===
	private static DecayOutput io(String id) {
		return new DecayOutput(id, 0.0);
	}

	private static DecayOutput io(String id, double p) {
		return new DecayOutput(id, p);
	}

	private static Item resolve(String id) {
		try {
			var rl = ResourceLocation.parse(id);
			return BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
		} catch (Exception ignored) {
			return null;
		}
	}

	private static void spawnStacked(Level level, double x, double y, double z, Item item, int total) {
		int max = item.getDefaultMaxStackSize();
		while (total > 0) {
			int n = Math.min(max, total);
			level.addFreshEntity(new ItemEntity(level, x, y, z, new ItemStack(item, n)));
			total -= n;
		}
	}
}