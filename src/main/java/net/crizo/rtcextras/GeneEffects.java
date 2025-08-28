package net.crizo.rtcextras;

import org.checkerframework.checker.units.qual.t;
import org.checkerframework.checker.units.qual.h;
import org.checkerframework.checker.units.qual.cd;

import net.neoforged.fml.common.EventBusSubscriber;

import net.crizo.rtcextras.network.RtcExtrasModVariables;

@EventBusSubscriber
public final class GeneEffects {
	// ========== registries ==========
	private static final java.util.Map<String, InBodyHooks> IN_BODY = new java.util.HashMap<>();
	private static final java.util.Map<String, OnDecayHooks> ON_DECAY = new java.util.HashMap<>();
	private static final java.util.Map<String, OnProductionHooks> ON_PROD = new java.util.HashMap<>();
	static {
		init();
	}
	// Where we expect the CSV to live (player persistent NBT). Change the key name if you prefer.
	private static final String KEY_ACTIVE_GENES_CSV = "rtc_active_genes_csv";
	// --- attribute modifier ids reused across add/remove ---
	private static final net.minecraft.resources.ResourceLocation MOD_MARFAN_REACH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:marfan_reach");
	private static final net.minecraft.resources.ResourceLocation MOD_MARFAN_HEALTH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:marfan_health");
	private static final net.minecraft.resources.ResourceLocation MOD_CHITIN_ARMOR = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chitin_armor");
	private static final net.minecraft.resources.ResourceLocation MOD_CHITIN_KB = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chitin_kb");
	private static final net.minecraft.resources.ResourceLocation MOD_FG_REACH_BLOCK = net.minecraft.resources.ResourceLocation.parse("rtc_extras:fg_reach_block");
	private static final net.minecraft.resources.ResourceLocation MOD_FG_REACH_ENTITY = net.minecraft.resources.ResourceLocation.parse("rtc_extras:fg_reach_entity");

	/*
	// small helper
	private static void removeAttr(net.minecraft.world.entity.player.Player p, net.minecraft.world.entity.ai.attributes.Attribute a, net.minecraft.resources.ResourceLocation id) {
		var inst = p.getAttribute(a);
		if (inst != null)
			inst.removeModifier(id);
	}
	*/
	// ===== 1.21.x Attribute helpers (Holder-based, explicit types) =====
	private static net.minecraft.world.entity.ai.attributes.AttributeInstance attrByKey(net.minecraft.world.entity.player.Player p, String namespacedKey) {
		try {
			net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.parse(namespacedKey);
			net.minecraft.core.Registry<net.minecraft.world.entity.ai.attributes.Attribute> reg = p.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE);
			net.minecraft.resources.ResourceKey<net.minecraft.world.entity.ai.attributes.Attribute> key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ATTRIBUTE, rl);
			java.util.Optional<net.minecraft.core.Holder.Reference<net.minecraft.world.entity.ai.attributes.Attribute>> opt = reg.getHolder(key);
			if (opt.isEmpty())
				return null;
			net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> holder = opt.get();
			return p.getAttribute(holder); // <-- getAttribute(Holder<Attribute>) in 1.21.x
		} catch (Throwable ignored) {
			return null;
		}
	}

	/** Map short names to proper keys. "generic.*" for most, "player.*" for reach. */
	private static String ak(String shortName) {
		return switch (shortName) {
			case "block_interaction_range", "entity_interaction_range" -> "minecraft:player." + shortName;
			default -> "minecraft:generic." + shortName;
		};
	}

	private static void ensurePermAttrByKey(net.minecraft.world.entity.player.Player p, String attrKey, net.minecraft.resources.ResourceLocation id, double val, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation op) {
		net.minecraft.world.entity.ai.attributes.AttributeInstance inst = attrByKey(p, attrKey);
		if (inst != null && inst.getModifier(id) == null) {
			inst.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, val, op));
		}
	}

	// Overload used by the gene methods I sent (6 args).
	private static void ensureToggleAttrByKey(net.minecraft.world.entity.player.Player p, String key, net.minecraft.resources.ResourceLocation id, double val, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation op, boolean enable) {
		// Delegate to your existing 7-arg version, using transient modifiers by default
		ensureToggleAttrByKey(p, key, id, val, op, enable, true);
	}

	// If you DO already have it, make sure its signature/body matches (safe to replace).
	private static void ensureToggleAttrByKey(net.minecraft.world.entity.player.Player p, String key, net.minecraft.resources.ResourceLocation id, double val, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation op, boolean enable,
			boolean useTransient) {
		// requires attrByKey(String) helper to exist in this class
		var inst = attrByKey(p, key);
		if (inst == null)
			return;
		boolean has = inst.getModifier(id) != null;
		if (enable && !has) {
			var mod = new net.minecraft.world.entity.ai.attributes.AttributeModifier(id, val, op);
			if (useTransient)
				inst.addTransientModifier(mod);
			else
				inst.addPermanentModifier(mod);
		} else if (!enable && has) {
			inst.removeModifier(id);
		}
	}

	/** Remove modifier by vanilla Attribute HOLDER (for code paths that still use Attributes.*) */
	private static void removeAttr(net.minecraft.world.entity.player.Player p, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> holder, net.minecraft.resources.ResourceLocation id) {
		net.minecraft.world.entity.ai.attributes.AttributeInstance inst = p.getAttribute(holder);
		if (inst != null)
			inst.removeModifier(id);
	}

	/** Remove modifier by namespaced key (e.g. "minecraft:generic.movement_speed") */
	private static void removeAttrByKey(net.minecraft.world.entity.player.Player p, String attrKey, net.minecraft.resources.ResourceLocation id) {
		net.minecraft.world.entity.ai.attributes.AttributeInstance inst = attrByKey(p, attrKey);
		if (inst != null)
			inst.removeModifier(id);
	}

	// ---------- Modifier IDs (one per gene/attribute you touch) ----------
	private static final net.minecraft.resources.ResourceLocation MOD_HYPER_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hyper_ms"), MOD_HYPER_AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hyper_as"),
			MOD_HYPER_MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hyper_mine"), MOD_HEMO_AD = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hemo_atk"),
			MOD_HEMO_AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hemo_aspd"), MOD_HBV_O2 = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hbv_o2"),
			MOD_HBV_WATERMOVE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hbv_water_move"), MOD_HBV_SUBMINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hbv_submine"),
			MOD_ADREN_AD = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adren_atk"), MOD_ADREN_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adren_ms"),
			MOD_CHLORO_MS_POS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chloro_ms_pos"), MOD_CHLORO_M_POS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chloro_mine_pos"),
			MOD_CHLORO_MS_NEG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chloro_ms_neg"), MOD_CHLORO_M_NEG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chloro_mine_neg"),
			MOD_KIN_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:kin_ms"), MOD_KIN_SAFEFALL = net.minecraft.resources.ResourceLocation.parse("rtc_extras:kin_safefall"),
			MOD_KIN_FALLMULT = net.minecraft.resources.ResourceLocation.parse("rtc_extras:kin_fallmult"), MOD_KIN_STEP = net.minecraft.resources.ResourceLocation.parse("rtc_extras:kin_step");

	/** Get a fully-qualified id ("mod:id"), defaulting to rtc_extras for bare ids. */
	private static String fq(String id) {
		return id.indexOf(':') >= 0 ? id : "rtc_extras:" + id;
	}

	/** Parse CSV on-demand → Map<fqId, level>. Empty/absent CSV returns empty map. */
	private static java.util.Map<String, Integer> parseActiveGeneLevelsFromCsv(net.minecraft.world.entity.player.Player p) {
		var out = new java.util.HashMap<String, Integer>();
		var vars = p.getData(RtcExtrasModVariables.PLAYER_VARIABLES);
		String csv = vars.geneContent; // <-- your player variable
		if (csv == null || csv.isBlank())
			return out;
		for (String raw : csv.split(",")) {
			String tok = raw.trim();
			if (tok.isEmpty())
				continue;
			String id = fq(tok);
			out.merge(id, 1, Integer::sum);
		}
		return out;
	}

	public static void init() {
		// Already present / from earlier
		registerInBody("immortal_cell_cycle", new ImmortalCellCycle());
		registerInBody("bioluminescence", new Bioluminescence());
		registerInBody("hypermetabolism", new Hypermetabolism());
		registerInBody("sickle_cell_disease", new SickleCellDisease());
		registerInBody("hemophilia", new Hemophilia());
		registerInBody("venom_biosynthesis", new VenomBiosynthesis());
		registerInBody("thermogenesis", new Thermogenesis());
		registerInBody("hemoglobin_variant", new HemoglobinVariant());
		registerInBody("adrenaline_trigger", new AdrenalineTrigger());
		registerInBody("chloroplast_optimization", new ChloroplastOptimization());
		registerInBody("burst_regeneration", new BurstRegeneration());
		registerInBody("toxinase", new Toxinase());
		registerInBody("kinetic_muscle", new KineticMuscle());
		registerInBody("marfan_syndrome", new MarfanSyndrome());
		registerInBody("neuroinhibitor_aura", new NeuroinhibitorAura());
		registerInBody("photosynthesis", new Photosynthesis());
		registerInBody("photoshade_negentropy", new PhotoshadeNegentropy());
		registerInBody("prion_instability", new PrionInstability());
		// New from your list
		registerInBody("osteogenesis_imperfecta", new OsteogenesisImperfecta());
		registerInBody("cancerous_growth", new CancerousGrowth());
		registerInBody("aquagenic_urticaria", new AquagenicUrticaria());
		registerInBody("hyperimmune_response", new HyperimmuneResponse());
		registerInBody("regeneration_factor", new RegenerationFactor());
		registerInBody("heat_tolerance", new HeatTolerance());
		registerInBody("cold_tolerance", new ColdTolerance());
		registerInBody("chitin_layer", new ChitinLayer());
		registerInBody("fibroblast_growth", new FibroblastGrowth());
		registerInBody("neurotransmitter_boost", new NeurotransmitterBoost());
		registerInBody("neural_overclock", new NeuralOverclock());
		registerInBody("extreme_regeneration", new ExtremeRegeneration());
		registerInBody("mycelial_symbiosis", new MycelialSymbiosis());
		registerInBody("heme_overproduction", new HemeOverproduction());
		registerInBody("hypernatremia", new Hypernatremia());
		registerInBody("methylotrophy", new Methylotrophy());
		registerInBody("slime_polysaccharide", new SlimePolysaccharide());
		registerInBody("penicillin_like", new PenicillinLike());
		registerInBody("acetobacter_lineage", new AcetobacterLineage());
		registerInBody("cellulose_synthase", new CelluloseSynthase());
		registerInBody("polycarbonate_pathway", new PolycarbonatePathway());
		registerInBody("styrogenesis", new Styrogenesis());
		registerInBody("biolubricant_chain", new BiolubricantChain());
		registerInBody("antifungal_serum", new AntifungalSerum());
		registerInBody("chemoautotrophy_pack", new ChemoautotrophyPack());
		registerInBody("biophotonic_array", new BiophotonicArray());
		registerInBody("alien_neurotoxin_line", new AlienNeurotoxinLine());
		registerInBody("carbon_precursors", new CarbonPrecursors());
		registerInBody("fermentation_pathway", new FermentationPathway());
		registerInBody("spore_resistance", new SporeResistance());
		registerInBody("acromegaly", new Acromegaly());
		registerInBody("quantum_enzyme", new QuantumEnzyme());
		registerInBody("adaptive_chitin", new AdaptiveChitin());
		registerInBody("advanced_nootropics", new AdvancedNootropics());
		registerInBody("phase_change_mucus", new PhaseChangeMucus());
		registerInBody("hardened_carapace", new HardenedCarapace());
		// Decay hooks
		registerOnDecay("nitration_pack", new NitrationPackOnDecay());
	}

	// called by GeneAPI.setCsv after it updates the player’s list
	static void postSetCsvCleanup(net.minecraft.world.entity.player.Player p) {
		// ---- Marfan cleanup
		{
			int lvl = getInstalledLevel(p, "marfan_syndrome");
			var MOD_REACH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:marfan_reach");
			var MOD_HEALTH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:marfan_health");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("block_interaction_range"), MOD_REACH);
				removeAttrByKey(p, ak("entity_interaction_range"), MOD_REACH);
				removeAttrByKey(p, ak("max_health"), MOD_HEALTH);
				// clamp current health in case it was above the new max
				if (p.getHealth() > p.getMaxHealth())
					p.setHealth(p.getMaxHealth());
			}
		}
		// ---- Chitin Layer cleanup
		{
			int lvl = getInstalledLevel(p, "chitin_layer");
			var MOD_ARMOR = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chitin_armor");
			var MOD_KB = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chitin_kb");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("armor"), MOD_ARMOR);
				removeAttrByKey(p, ak("knockback_resistance"), MOD_KB);
			}
		}
		// ---- Fibroblast Growth cleanup
		{
			int lvl = getInstalledLevel(p, "fibroblast_growth");
			var MOD_R1 = net.minecraft.resources.ResourceLocation.parse("rtc_extras:fg_reach_block");
			var MOD_R2 = net.minecraft.resources.ResourceLocation.parse("rtc_extras:fg_reach_entity");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("block_interaction_range"), MOD_R1);
				removeAttrByKey(p, ak("entity_interaction_range"), MOD_R2);
			}
		}
		// ---- Hypermetabolism cleanup
		{
			int lvl = getInstalledLevel(p, "hypermetabolism");
			var MOD_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hyper_ms");
			var MOD_AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hyper_as");
			var MOD_MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hyper_mine");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("movement_speed"), MOD_MS);
				removeAttrByKey(p, ak("attack_speed"), MOD_AS);
				removeAttrByKey(p, ak("mining_efficiency"), MOD_MINE);
			}
		}
		// ---- Hemoglobin Variant (oxygen & water work)
		{
			int lvl = getInstalledLevel(p, "hemoglobin_variant");
			var OX = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hemo_oxyg");
			var SWIM = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hemo_swim");
			var SUB = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hemo_submine");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("oxygen_bonus"), OX);
				removeAttrByKey(p, ak("water_movement_efficiency"), SWIM);
				removeAttrByKey(p, ak("submerged_mining_speed"), SUB);
			}
		}
		// ---- Adrenaline Trigger (transient safety)
		{
			var DMG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adrenal_dmg");
			var MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adrenal_ms");
			removeAttrByKey(p, ak("attack_damage"), DMG);
			removeAttrByKey(p, ak("movement_speed"), MS);
		}
		// ---- Chloroplast Optimization (both polarity ids)
		{
			var POS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chloro_mine");
			var NEG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chloro_mine_penalty");
			removeAttrByKey(p, ak("mining_efficiency"), POS);
			removeAttrByKey(p, ak("mining_efficiency"), NEG);
		}
		// ---- Kinetic Muscle
		{
			int lvl = getInstalledLevel(p, "kinetic_muscle");
			var MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:km_ms");
			var JUMP = net.minecraft.resources.ResourceLocation.parse("rtc_extras:km_jump");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("movement_speed"), MS);
				removeAttrByKey(p, ak("jump_strength"), JUMP);
			}
		}
		// ---- Neurotransmitter Boost
		{
			int lvl = getInstalledLevel(p, "neurotransmitter_boost");
			var AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:ntb_as");
			var MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:ntb_mine");
			var LUCK = net.minecraft.resources.ResourceLocation.parse("rtc_extras:ntb_luck");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("attack_speed"), AS);
				removeAttrByKey(p, ak("mining_efficiency"), MINE);
				removeAttrByKey(p, ak("luck"), LUCK);
			}
		}
		// ---- Neural Overclock (transient safety)
		{
			var AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:nclock_as");
			var MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:nclock_mine");
			var MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:nclock_ms");
			removeAttrByKey(p, ak("attack_speed"), AS);
			removeAttrByKey(p, ak("mining_efficiency"), MINE);
			removeAttrByKey(p, ak("movement_speed"), MS);
		}
		// ---- Hyperimmune Response (transient safety)
		{
			var AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hir_as");
			var MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hir_mine");
			var DMG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hir_dmg");
			removeAttrByKey(p, ak("attack_speed"), AS);
			removeAttrByKey(p, ak("mining_efficiency"), MINE);
			removeAttrByKey(p, ak("attack_damage"), DMG);
		}
		// ---- Hypernatremia (transient safety if hungry)
		{
			var MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hn_ms_neg");
			var DMG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hn_dmg_neg");
			removeAttrByKey(p, ak("movement_speed"), MS);
			removeAttrByKey(p, ak("attack_damage"), DMG);
		}
		// ---- Methylotrophy (transient speed toggle)
		{
			var MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:methyl_ms");
			removeAttrByKey(p, ak("movement_speed"), MS);
		}
		// ---- Cellulose Synthase (if you add these attrs later)
		{
			int lvl = getInstalledLevel(p, "cellulose_synthase");
			var ARM = net.minecraft.resources.ResourceLocation.parse("rtc_extras:cell_armor");
			var KB = net.minecraft.resources.ResourceLocation.parse("rtc_extras:cell_kb");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("armor"), ARM);
				removeAttrByKey(p, ak("knockback_resistance"), KB);
			}
		}
		// ---- Polycarbonate Pathway
		{
			int lvl = getInstalledLevel(p, "polycarbonate_pathway");
			var MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:poly_ms");
			if (lvl <= 0)
				removeAttrByKey(p, ak("movement_speed"), MS);
		}
		// ---- Styrogenesis
		{
			int lvl = getInstalledLevel(p, "styrogenesis");
			var MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:styro_mine");
			if (lvl <= 0)
				removeAttrByKey(p, ak("mining_efficiency"), MINE);
		}
		// ---- Biolubricant Chain
		{
			int lvl = getInstalledLevel(p, "biolubricant_chain");
			var MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:bio_ms");
			var JUMP = net.minecraft.resources.ResourceLocation.parse("rtc_extras:bio_jump");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("movement_speed"), MS);
				removeAttrByKey(p, ak("jump_strength"), JUMP);
			}
		}
		// ---- Biophotonic Array (luck attr if used)
		{
			int lvl = getInstalledLevel(p, "biophotonic_array");
			var LUCK = net.minecraft.resources.ResourceLocation.parse("rtc_extras:bioarray_luck");
			if (lvl <= 0)
				removeAttrByKey(p, ak("luck"), LUCK);
		}
		// ---- Carbon Precursors
		{
			int lvl = getInstalledLevel(p, "carbon_precursors");
			var MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:carb_mine");
			if (lvl <= 0)
				removeAttrByKey(p, ak("mining_efficiency"), MINE);
		}
		// ========================
		//  Extras you asked to fold in
		// ========================
		// ---- Fermentation Pathway
		{
			int lvl = getInstalledLevel(p, "fermentation_pathway");
			var MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:ferm_mine");
			if (lvl <= 0)
				removeAttrByKey(p, ak("mining_efficiency"), MINE);
		}
		// ---- Spore Resistance
		{
			int lvl = getInstalledLevel(p, "spore_resistance");
			var AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:spore_as");
			var MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:spore_mine");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("attack_speed"), AS);
				removeAttrByKey(p, ak("mining_efficiency"), MINE);
			}
		}
		// ---- Acromegaly (+reach/+dmg, -speed)
		{
			int lvl = getInstalledLevel(p, "acromegaly");
			var Rb = net.minecraft.resources.ResourceLocation.parse("rtc_extras:acro_reach_b");
			var Re = net.minecraft.resources.ResourceLocation.parse("rtc_extras:acro_reach_e");
			var DMG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:acro_dmg");
			var MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:acro_ms");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("block_interaction_range"), Rb);
				removeAttrByKey(p, ak("entity_interaction_range"), Re);
				removeAttrByKey(p, ak("attack_damage"), DMG);
				removeAttrByKey(p, ak("movement_speed"), MS);
			}
		}
		// ---- Adaptive Chitin (heavy plating)
		{
			int lvl = getInstalledLevel(p, "adaptive_chitin");
			var ARM = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adchitin_armor");
			var TOUGH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adchitin_tough");
			var KB = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adchitin_kb");
			var MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adchitin_ms");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("armor"), ARM);
				removeAttrByKey(p, ak("armor_toughness"), TOUGH);
				removeAttrByKey(p, ak("knockback_resistance"), KB);
				removeAttrByKey(p, ak("movement_speed"), MS);
			}
		}
		// ---- Advanced Nootropics
		{
			int lvl = getInstalledLevel(p, "advanced_nootropics");
			var AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:noot_as");
			var MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:noot_mine");
			var LUCK = net.minecraft.resources.ResourceLocation.parse("rtc_extras:noot_luck");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("attack_speed"), AS);
				removeAttrByKey(p, ak("mining_efficiency"), MINE);
				removeAttrByKey(p, ak("luck"), LUCK);
			}
		}
		// ---- Phase-Change Mucus (slippery sprint)
		{
			int lvl = getInstalledLevel(p, "phase_change_mucus");
			var MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:pcm_sprint");
			if (lvl <= 0)
				removeAttrByKey(p, ak("movement_speed"), MS);
		}
		// ---- Hardened Carapace
		{
			int lvl = getInstalledLevel(p, "hardened_carapace");
			var ARM = net.minecraft.resources.ResourceLocation.parse("rtc_extras:carapace_armor");
			var TOUGH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:carapace_tough");
			var MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:carapace_ms");
			if (lvl <= 0) {
				removeAttrByKey(p, ak("armor"), ARM);
				removeAttrByKey(p, ak("armor_toughness"), TOUGH);
				removeAttrByKey(p, ak("movement_speed"), MS);
			}
		}
	}

	// The negative-effects probe some genes reference
	private static boolean hasAnyNegativeEffect(net.minecraft.world.entity.LivingEntity le) {
		return le.hasEffect(net.minecraft.world.effect.MobEffects.POISON) || le.hasEffect(net.minecraft.world.effect.MobEffects.WITHER) || le.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)
				|| le.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN) || le.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS) || le.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)
				|| le.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION) || le.hasEffect(net.minecraft.world.effect.MobEffects.HUNGER);
	}

	public static void registerInBody(String idOrBare, InBodyHooks h) {
		IN_BODY.put(fq(idOrBare), h);
	}

	public static void registerOnDecay(String idOrBare, OnDecayHooks h) {
		ON_DECAY.put(fq(idOrBare), h);
	}

	public static void registerOnProduction(String idOrBare, OnProductionHooks h) {
		ON_PROD.put(fq(idOrBare), h);
	}

	// ========== “dispatch” helpers used by GeneEngine ==========
	static void dispatchInBody(GeneSpec spec, GeneEngine.Ctx c) {
		var h = IN_BODY.get(fq(spec.id));
		if (h == null)
			return;
		// one-shot call (rarely used; normal path is via tick event below)
		if (c.source() instanceof net.minecraft.world.entity.player.Player p) {
			int lvl = getInstalledLevel(p, spec.id);
			if (lvl > 0)
				h.onPlayerTick(p, lvl);
		}
	}

	static void dispatchOnDecay(GeneSpec spec, GeneEngine.Ctx c) {
		var h = ON_DECAY.get(fq(spec.id));
		if (h != null)
			h.onDecay(c, c.stackCount());
	}

	static void dispatchOnProduction(GeneSpec spec, GeneEngine.Ctx c, int producedNow) {
		var h = ON_PROD.get(fq(spec.id));
		if (h != null)
			h.onProduction(c, producedNow);
	}

	// ========== Installed-level helper (stacking) ==========
	public static int getInstalledLevel(net.minecraft.world.entity.Entity e, String geneIdRaw) {
		if (!(e instanceof net.minecraft.world.entity.player.Player p))
			return 0;
		return getInstalledLevel(p, geneIdRaw);
	}

	private static int getInstalledLevel(net.minecraft.world.entity.player.Player p, String geneIdRaw) {
		// 1) CSV source (preferred)
		var fromCsv = parseActiveGeneLevelsFromCsv(p);
		if (!fromCsv.isEmpty()) {
			return fromCsv.getOrDefault(fq(geneIdRaw), 0);
		}
		// 2) Fallback: legacy inventory scan (rtc_installed:1b)
		var inv = p.getInventory();
		if (inv == null)
			return 0;
		String fqid = fq(geneIdRaw);
		int level = 0;
		for (var st : inv.items) {
			if (st == null || st.isEmpty())
				continue;
			if (!(st.getItem() instanceof net.crizo.rtcextras.item.BacteriumItem))
				continue;
			var tag = net.crizo.rtcextras.item.BacteriumItem.getTag(st);
			if (tag.getBoolean("rtc_installed")) {
				String id = tag.getString("rtc_gene_id");
				if (!id.isEmpty()) {
					if (!id.contains(":"))
						id = "rtc_extras:" + id;
					if (id.equals(fqid))
						level++;
				}
			}
		}
		return level;
	}

	// ========== Event bridges (NeoForge 1.21.1) ==========
	@net.neoforged.bus.api.SubscribeEvent
	public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e) {
		var p = e.getEntity();
		if (p.level().isClientSide())
			return;
		for (var ent : IN_BODY.entrySet()) {
			int lvl = getInstalledLevel(p, ent.getKey());
			if (lvl > 0)
				ent.getValue().onPlayerTick(p, lvl);
		}
	}

	// 1.21.1 uses LivingIncomingDamageEvent (pre-application)
	@net.neoforged.bus.api.SubscribeEvent
	public static void onIncomingDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e) {
		var le = e.getEntity();
		if (le.level().isClientSide())
			return;
		for (var ent : IN_BODY.entrySet()) {
			int lvl = getInstalledLevel(le, ent.getKey());
			if (lvl > 0)
				ent.getValue().onIncomingDamage(le, e, lvl);
		}
	}

	@net.neoforged.bus.api.SubscribeEvent
	public static void onDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent e) {
		var le = e.getEntity();
		if (le.level().isClientSide())
			return;
		for (var ent : IN_BODY.entrySet()) {
			int lvl = getInstalledLevel(le, ent.getKey());
			if (lvl > 0)
				ent.getValue().onDeath(le, e, lvl);
		}
	}

	@net.neoforged.bus.api.SubscribeEvent
	public static void onHeal(net.neoforged.neoforge.event.entity.living.LivingHealEvent e) {
		var le = e.getEntity();
		if (le.level().isClientSide())
			return;
		for (var ent : IN_BODY.entrySet()) {
			int lvl = getInstalledLevel(le, ent.getKey());
			if (lvl > 0)
				ent.getValue().onHeal(le, e, lvl);
		}
	}

	// ========== Hook interfaces ==========
	public interface InBodyHooks {
		default void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
		}

		default void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
		}

		default void onDeath(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingDeathEvent e, int level) {
		}

		default void onHeal(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingHealEvent e, int level) {
		}
	}

	public interface OnDecayHooks {
		void onDecay(GeneEngine.Ctx c, int stackCount);
	}

	public interface OnProductionHooks {
		void onProduction(GeneEngine.Ctx c, int producedNow);
	}

	// ========== Example in-body modules ==========
	// ===== Immortal Cell Cycle (cryptic ready ping + daily death cancel) =====
	public static final class ImmortalCellCycle implements InBodyHooks {
		private static final String KEY_LAST_DAY = "rtc_icc_last_day";
		private static final String KEY_TICK_GUARD = "rtc_icc_guard_tick";
		private static final String KEY_READY_SHOWN_DAY = "rtc_icc_ready_day";

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			long day = p.level().getGameTime() / 24000L;
			var tag = p.getPersistentData();
			long usedDay = tag.getLong(KEY_LAST_DAY);
			long shownDay = tag.getLong(KEY_READY_SHOWN_DAY);
			if (usedDay != day && shownDay != day) {
				tag.putLong(KEY_READY_SHOWN_DAY, day);
				p.sendSystemMessage(net.minecraft.network.chat.Component.literal("You feel a latent resilience hum through your cells...").withStyle(net.minecraft.ChatFormatting.AQUA));
			}
		}

		@Override
		public void onDeath(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingDeathEvent e, int level) {
			long gameTime = le.level().getGameTime();
			long dayIndex = gameTime / 24000L;
			var tag = le.getPersistentData();
			long last = tag.getLong(KEY_LAST_DAY);
			long guardTick = tag.getLong(KEY_TICK_GUARD);
			if (guardTick == gameTime)
				return;
			if (last == dayIndex)
				return;
			tag.putLong(KEY_LAST_DAY, dayIndex);
			tag.putLong(KEY_TICK_GUARD, gameTime);
			e.setCanceled(true);
			float heal = Math.max(1.0f, Math.min(le.getMaxHealth(), le.getMaxHealth() * 0.40f + 2.0f * level));
			le.setHealth(heal);
			le.removeAllEffects();
			le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 200, Math.min(2, level)));
			le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 200, Math.max(0, level - 1)));
			le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 400, Math.min(2, level)));
			le.level().levelEvent(2003, le.blockPosition(), 0);
			if (le instanceof net.minecraft.world.entity.player.Player p) {
				p.playNotifySound(net.minecraft.sounds.SoundEvents.TOTEM_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
			}
		}
	}

	// ===== Bioluminescence (keep NV effect) =====
	public static final class Bioluminescence implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			if (p.level().getMaxLocalRawBrightness(p.blockPosition()) > 7)
				return;
			int dur = 60 + 20 * level;
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, dur, 0, false, false, false));
		}
	}

	// ===== Hypermetabolism (attributes) =====
	public static final class Hypermetabolism implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_HYPER_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hyper_ms");
		private static final net.minecraft.resources.ResourceLocation MOD_HYPER_AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hyper_as");
		private static final net.minecraft.resources.ResourceLocation MOD_HYPER_MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hyper_mine");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			ensurePermAttrByKey(p, ak("movement_speed"), MOD_HYPER_MS, 0.02D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			ensurePermAttrByKey(p, ak("attack_speed"), MOD_HYPER_AS, 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			ensurePermAttrByKey(p, ak("mining_efficiency"), MOD_HYPER_MINE, 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		}
	}

	// ===== Sickle Cell Trait (anti-poison + tiny stamina drain) =====
	public static final class SickleCellDisease implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) {
				p.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
			}
			p.causeFoodExhaustion(0.0025F * level);
		}
	}

	// ===== Hemophilia (bleed DoT; while bleeding add transient attr buffs) =====
	public static final class Hemophilia implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_Hemo_AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hemo_as");
		private static final net.minecraft.resources.ResourceLocation MOD_Hemo_Mi = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hemo_mine");

		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			if (le.level().isClientSide())
				return;
			le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 40 + 20 * level, 0, false, true, true));
		}

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			boolean bleeding = p.hasEffect(net.minecraft.world.effect.MobEffects.WITHER);
			ensureToggleAttrByKey(p, ak("attack_speed"), MOD_Hemo_AS, 0.05D * Math.max(1, level), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, bleeding);
			ensureToggleAttrByKey(p, ak("mining_efficiency"), MOD_Hemo_Mi, 0.05D * Math.max(1, level), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, bleeding);
		}
	}

	// ===== Venom biosynthesis (keep poison effect on victim) =====
	public static final class VenomBiosynthesis implements InBodyHooks {
		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity victim, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			var src = e.getSource().getEntity();
			if (!(src instanceof net.minecraft.world.entity.LivingEntity attacker))
				return;
			if (getInstalledLevel(attacker, "venom_biosynthesis") <= 0)
				return;
			victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 40 + 20 * level, Math.min(2, level - 1)));
		}
	}

	// ===== Thermogenesis (ignite attacker) =====
	public static final class Thermogenesis implements InBodyHooks {
		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			var srcEnt = e.getSource().getEntity();
			if (srcEnt instanceof net.minecraft.world.entity.LivingEntity atk) {
				atk.igniteForSeconds(1 + level);
			}
		}
	}

	// ===== Hemoglobin Variant (attributes: oxygen + swim/submerged mining) =====
	public static final class HemoglobinVariant implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_OXYG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hemo_oxyg");
		private static final net.minecraft.resources.ResourceLocation MOD_SWIM = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hemo_swim");
		private static final net.minecraft.resources.ResourceLocation MOD_SUBM = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hemo_submine");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			ensurePermAttrByKey(p, ak("oxygen_bonus"), MOD_OXYG, 20.0D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
			ensurePermAttrByKey(p, ak("water_movement_efficiency"), MOD_SWIM, 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			ensurePermAttrByKey(p, ak("submerged_mining_speed"), MOD_SUBM, 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		}
	}

	// ===== Adrenaline Trigger (low HP → transient attr boosts) =====
	public static final class AdrenalineTrigger implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_DMG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adrenal_dmg");
		private static final net.minecraft.resources.ResourceLocation MOD_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adrenal_ms");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			boolean crisis = p.getHealth() <= p.getMaxHealth() * 0.30f;
			ensureToggleAttrByKey(p, ak("attack_damage"), MOD_DMG, 3.0D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, crisis);
			ensureToggleAttrByKey(p, ak("movement_speed"), MOD_MS, 0.5D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, crisis);
		}
	}

	// ===== Chloroplast Optimization (attributes in sun; small penalty in dark) =====
	public static final class ChloroplastOptimization implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_POS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chloro_mine");
		private static final net.minecraft.resources.ResourceLocation MOD_NEG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chloro_mine_penalty");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			boolean sun = p.level().canSeeSky(p.blockPosition()) && p.level().isDay() && p.level().getMaxLocalRawBrightness(p.blockPosition()) >= 12;
			// positive boost when sunny
			ensureToggleAttrByKey(p, ak("mining_efficiency"), MOD_POS, 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, sun);
			// small penalty when not sunny
			ensureToggleAttrByKey(p, ak("mining_efficiency"), MOD_NEG, -0.03D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE, !sun);
		}
	}

	// ===== Burst Regeneration (effect, when damaged) =====
	public static final class BurstRegeneration implements InBodyHooks {
		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 80 + 20 * level, Math.min(2, level), false, true, true));
		}
	}

	// ===== Toxinase (convert negatives → positives; conservative) =====
	public static final class Toxinase implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			// poison → instant clear + short regen I
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) {
				p.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 60, 0));
			}
			// wither → reduce to short regen I
			var w = p.getEffect(net.minecraft.world.effect.MobEffects.WITHER);
			if (w != null) {
				p.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, Math.min(80, 20 + w.getDuration() / 2), 0));
			}
		}
	}

	// ===== Kinetic Muscle (attributes; keep fall mitigation) =====
	public static final class KineticMuscle implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:km_ms");
		private static final net.minecraft.resources.ResourceLocation MOD_JUMP = net.minecraft.resources.ResourceLocation.parse("rtc_extras:km_jump");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			ensurePermAttrByKey(p, ak("movement_speed"), MOD_MS, 0.03D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			ensurePermAttrByKey(p, ak("jump_strength"), MOD_JUMP, 0.20D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
		}

		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			if (e.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
				e.setAmount(e.getAmount() * (0.8f - 0.1f * Math.min(level, 3)));
			}
		}
	}

	// ===== Marfan Syndrome (attrs as before) =====
	public static final class MarfanSyndrome implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_REACH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:marfan_reach");
		private static final net.minecraft.resources.ResourceLocation MOD_HEALTH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:marfan_health");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			try {
				var a1 = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE);
				var a2 = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE);
				if (a1 != null && a1.getModifier(MOD_REACH) == null) {
					a1.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(MOD_REACH, 0.75D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
				}
				if (a2 != null && a2.getModifier(MOD_REACH) == null) {
					a2.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(MOD_REACH, 0.75D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
				}
			} catch (Throwable ignored) {
			}
			var mh = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
			if (mh != null && mh.getModifier(MOD_HEALTH) == null) {
				mh.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(MOD_HEALTH, -2.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
			}
		}
	}

	// ===== Neuroinhibitor Aura (keep effect Weakness on hostiles) =====
	public static final class NeuroinhibitorAura implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			var r = 4.0 + level;
			var aabb = new net.minecraft.world.phys.AABB(p.getX() - r, p.getY() - r, p.getZ() - r, p.getX() + r, p.getY() + r, p.getZ() + r);
			for (var e : p.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb, t -> t != p && !t.getType().getCategory().isFriendly())) {
				e.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 40, 0));
			}
		}
	}

	// ===== Photosynthesis (≈1 full hunger bar / day in sunlight) =====
	public static final class Photosynthesis implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			boolean sun = p.level().canSeeSky(p.blockPosition()) && p.level().isDay() && p.level().getMaxLocalRawBrightness(p.blockPosition()) >= 12;
			if (!sun)
				return;
			// 24000 ticks/day → eat 1 every 1200 ticks ≈ 20 per day (1 full bar)
			if ((p.tickCount % 1200) == 0) {
				try {
					p.getFoodData().eat(1, 0.1f);
				} catch (Throwable ignored) {
				}
			}
		}
	}

	// ===== Photoshade Negentropy (very slow heal in darkness + exhaustion counter) =====
	public static final class PhotoshadeNegentropy implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			boolean dark = p.level().getMaxLocalRawBrightness(p.blockPosition()) <= 4;
			if (!dark)
				return;
			if ((p.tickCount % 200) == 0)
				p.heal(0.5f); // gentle
			p.causeFoodExhaustion(-0.005f * Math.max(1, level));
		}
	}

	// ===== Prion Instability (degeneration + light chaos) =====
	public static final class PrionInstability implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			if ((p.tickCount % (60 * 20)) == 0)
				p.hurt(p.damageSources().magic(), 0.5f);
			if (p.getRandom().nextDouble() < 0.005 * level) {
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 100, 0));
			}
		}
	}

	// ===== Nitration Pack (decay): tiny pops =====
	public static final class NitrationPackOnDecay implements OnDecayHooks {
		@Override
		public void onDecay(GeneEngine.Ctx c, int stackCount) {
			double p = Math.min(0.001 * stackCount, 0.02);
			if (c.rng().nextDouble() < p) {
				c.level().explode(c.source(), c.x(), c.y(), c.z(), 0.6f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
			}
		}
	}

	// ===== Osteogenesis Imperfecta =====
	public static final class OsteogenesisImperfecta implements InBodyHooks {
		private static final String FX_TAG = "rtc_oi_fracture_cd";

		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			var src = e.getSource();
			float mul = 1.0f;
			if (src.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
				mul = 1.5f + 0.25f * Math.min(level, 3);
			} else if (src.getEntity() instanceof net.minecraft.world.entity.LivingEntity && !src.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE) && !src.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)
					&& !src.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
				mul = 1.15f + 0.10f * Math.min(level, 3);
			}
			if (mul > 1.0f)
				e.setAmount(e.getAmount() * mul);
			var tag = le.getPersistentData();
			long now = le.level().getGameTime();
			long cd = tag.getLong(FX_TAG);
			if (now >= cd) {
				tag.putLong(FX_TAG, now + 8 * 20L);
				le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 140, 1));
				le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 140, 0));
			}
		}
	}

	// ===== Cancerous Growth =====
	public static final class CancerousGrowth implements InBodyHooks {
		private static final String LAST_TICK = "rtc_cancer_last";

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			var tag = p.getPersistentData();
			long now = p.level().getGameTime();
			long last = tag.getLong(LAST_TICK);
			if (now - last >= 100L) {
				tag.putLong(LAST_TICK, now);
				p.hurt(p.damageSources().magic(), 0.5f + 0.25f * Math.min(level, 3));
			}
		}

		@Override
		public void onHeal(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingHealEvent e, int level) {
			float mul = Math.max(0.35f, 1.0f - 0.20f * level);
			e.setAmount(e.getAmount() * mul);
		}
	}

	// ===== Aquagenic Urticaria =====
	public static final class AquagenicUrticaria implements InBodyHooks {
		private static final String CD = "rtc_au_cd";

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			if (p.isInWaterRainOrBubble() || p.isInWater() || p.isUnderWater()) {
				var tag = p.getPersistentData();
				long now = p.level().getGameTime();
				long cd = tag.getLong(CD);
				if (now >= cd) {
					tag.putLong(CD, now + 20L);
					p.hurt(p.damageSources().magic(), 0.5f + 0.25f * Math.min(2, level));
					p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 60, 0));
				}
			}
		}
	}

	// ===== Hyperimmune Response (toggle attrs while debuffed) =====
	public static final class HyperimmuneResponse implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hir_as");
		private static final net.minecraft.resources.ResourceLocation MOD_MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hir_mine");
		private static final net.minecraft.resources.ResourceLocation MOD_DMG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hir_dmg");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			boolean debuffed = hasAnyNegativeEffect(p);
			ensureToggleAttrByKey(p, ak("attack_speed"), MOD_AS, 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, debuffed);
			ensureToggleAttrByKey(p, ak("mining_efficiency"), MOD_MINE, 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, debuffed);
			ensureToggleAttrByKey(p, ak("attack_damage"), MOD_DMG, 0.05D * Math.max(0, level - 1), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, debuffed);
		}
	}

	// ===== Regeneration Factor =====
	public static final class RegenerationFactor implements InBodyHooks {
		private static final String LAST_HURT_TICK = "rtc_regen_last_hurt";

		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			le.getPersistentData().putLong(LAST_HURT_TICK, le.level().getGameTime());
		}

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			long now = p.level().getGameTime();
			long last = p.getPersistentData().getLong(LAST_HURT_TICK);
			if (now - last >= 100L && (p.tickCount % 40) == 0)
				p.heal(0.5f + 0.25f * Math.min(level, 2));
		}
	}

	// ===== Heat Tolerance (keep FR effect + burn down) =====
	public static final class HeatTolerance implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.isOnFire()) {
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 40, 0, false, false, false));
				int left = p.getRemainingFireTicks();
				if (left > 0)
					p.setRemainingFireTicks(Math.max(0, left - (1 + level)));
			}
		}
	}

	// ===== Cold Tolerance (thaw faster; tiny speed burst stays effect) =====
	public static final class ColdTolerance implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			int frozen = p.getTicksFrozen();
			if (frozen > 0) {
				p.setTicksFrozen(Math.max(0, frozen - (1 + level)));
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 40, 0, false, false, false));
			}
		}
	}

	// ===== Chitin Layer (attributes) =====
	public static final class ChitinLayer implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_ARMOR = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chitin_armor");
		private static final net.minecraft.resources.ResourceLocation MOD_KB = net.minecraft.resources.ResourceLocation.parse("rtc_extras:chitin_kb");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			var aArmor = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
			var aKb = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE);
			if (aArmor != null && aArmor.getModifier(MOD_ARMOR) == null) {
				aArmor.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(MOD_ARMOR, 1.0D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
			}
			if (aKb != null && aKb.getModifier(MOD_KB) == null) {
				aKb.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(MOD_KB, 0.1D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
			}
		}
	}

	// ===== Fibroblast Growth (short reach via attributes) =====
	public static final class FibroblastGrowth implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_R1 = net.minecraft.resources.ResourceLocation.parse("rtc_extras:fg_reach_block");
		private static final net.minecraft.resources.ResourceLocation MOD_R2 = net.minecraft.resources.ResourceLocation.parse("rtc_extras:fg_reach_entity");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			try {
				var a1 = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE);
				var a2 = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE);
				if (a1 != null && a1.getModifier(MOD_R1) == null) {
					a1.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(MOD_R1, -0.5D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
				}
				if (a2 != null && a2.getModifier(MOD_R2) == null) {
					a2.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(MOD_R2, -0.5D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
				}
			} catch (Throwable ignored) {
			}
		}
	}

	// ===== Neurotransmitter Boost (permanent attrs) =====
	public static final class NeurotransmitterBoost implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:ntb_as");
		private static final net.minecraft.resources.ResourceLocation MOD_MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:ntb_mine");
		private static final net.minecraft.resources.ResourceLocation MOD_LUCK = net.minecraft.resources.ResourceLocation.parse("rtc_extras:ntb_luck");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			ensurePermAttrByKey(p, ak("attack_speed"), MOD_AS, 0.10D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			ensurePermAttrByKey(p, ak("mining_efficiency"), MOD_MINE, 0.10D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			ensurePermAttrByKey(p, ak("luck"), MOD_LUCK, 0.50D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
		}
	}

	// ===== Neural Overclock (sprinting toggles big attrs + tunnel vision) =====
	public static final class NeuralOverclock implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:nclock_as");
		private static final net.minecraft.resources.ResourceLocation MOD_MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:nclock_mine");
		private static final net.minecraft.resources.ResourceLocation MOD_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:nclock_ms");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			boolean fast = p.isSprinting();
			ensureToggleAttrByKey(p, ak("attack_speed"), MOD_AS, 0.30D + 0.10D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, fast);
			ensureToggleAttrByKey(p, ak("mining_efficiency"), MOD_MINE, 0.30D + 0.10D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, fast);
			ensureToggleAttrByKey(p, ak("movement_speed"), MOD_MS, 0.10D + 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, fast);
			if (fast)
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 20, 0, false, false, false));
		}
	}

	// ===== Extreme Regeneration =====
	public static final class ExtremeRegeneration implements InBodyHooks {
		private static final String CD = "rtc_er_cd";

		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			if (!(le instanceof net.minecraft.world.entity.player.Player p))
				return;
			if (p.getHealth() > p.getMaxHealth() * 0.4f)
				return;
			long now = p.level().getGameTime();
			var tag = p.getPersistentData();
			long cd = tag.getLong(CD);
			if (now < cd)
				return;
			tag.putLong(CD, now + (20L * 20));
			p.causeFoodExhaustion(6.0f);
			p.heal(6.0f + 2.0f * Math.min(level, 2));
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 1));
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 200, 1));
		}
	}

	// ===== Mycelial Symbiosis =====
	public static final class MycelialSymbiosis implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			if ((p.tickCount % 40) != 0)
				return;
			double r = 4.0 + 2.0 * level;
			var aabb = new net.minecraft.world.phys.AABB(p.getX() - r, p.getY() - r, p.getZ() - r, p.getX() + r, p.getY() + r, p.getZ() + r);
			for (var e : p.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb, t -> t != p && t.getType().getCategory().isFriendly())) {
				e.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 60, 0, false, true, true));
			}
		}
	}

	// ===== Heme Overproduction (air supply) =====
	public static final class HemeOverproduction implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			int max = p.getMaxAirSupply();
			if (p.getAirSupply() < max)
				p.setAirSupply(max);
		}
	}

	// ===== Hypernatremia (hungry → transient negatives via attrs) =====
	public static final class Hypernatremia implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hn_ms_neg");
		private static final net.minecraft.resources.ResourceLocation MOD_DMG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:hn_dmg_neg");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			p.causeFoodExhaustion(0.0015F * level);
			boolean hungry = p.getFoodData().getFoodLevel() <= 6;
			ensureToggleAttrByKey(p, ak("movement_speed"), MOD_MS, -0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, hungry);
			ensureToggleAttrByKey(p, ak("attack_damage"), MOD_DMG, -0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, hungry);
		}
	}

	// ===== Methylotrophy (sprinting → attr speed, low sat → nausea) =====
	public static final class Methylotrophy implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:methyl_ms");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			boolean sprint = p.isSprinting();
			ensureToggleAttrByKey(p, ak("movement_speed"), MOD_MS, 0.05D * Math.min(1, level), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, sprint);
			if (sprint && p.getFoodData().getSaturationLevel() < 1.0f) {
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 40, 0, false, false, false));
			}
		}
	}

	// ===== Slime Polysaccharide (slow falling + fall soften) =====
	public static final class SlimePolysaccharide implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 40, 0, false, false, false));
		}

		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			if (e.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
				e.setAmount(e.getAmount() * (0.85f - 0.10f * Math.min(2, level)));
			}
		}
	}

	// ===== Penicillin-like (anti-poison → regen) =====
	public static final class PenicillinLike implements InBodyHooks {
		private static final String CD = "rtc_pen_cd";

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (!p.hasEffect(net.minecraft.world.effect.MobEffects.POISON))
				return;
			long now = p.level().getGameTime();
			var tag = p.getPersistentData();
			long cd = tag.getLong(CD);
			if (now < cd)
				return;
			tag.putLong(CD, now + 80L);
			p.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 60, 0));
		}
	}

	// ===== Acetobacter Lineage (acidic splash) =====
	public static final class AcetobacterLineage implements InBodyHooks {
		private static final String CD = "rtc_aceto_cd";

		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			var atk = e.getSource().getEntity();
			if (!(atk instanceof net.minecraft.world.entity.LivingEntity a))
				return;
			long now = le.level().getGameTime();
			var tag = le.getPersistentData();
			long cd = tag.getLong(CD);
			if (now < cd)
				return;
			tag.putLong(CD, now + 60L);
			a.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 40 + 20 * Math.min(1, level), 0));
		}
	}

	// ===== Cellulose Synthase (small natural armor/KB via attrs) =====
	public static final class CelluloseSynthase implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_ARM = net.minecraft.resources.ResourceLocation.parse("rtc_extras:cell_armor");
		private static final net.minecraft.resources.ResourceLocation MOD_KB = net.minecraft.resources.ResourceLocation.parse("rtc_extras:cell_kb");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			ensurePermAttrByKey(p, ak("armor"), MOD_ARM, 0.5D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
			ensurePermAttrByKey(p, ak("knockback_resistance"), MOD_KB, 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
		}
	}

	// ===== Polycarbonate Pathway (lightweight → minor speed attr) =====
	public static final class PolycarbonatePathway implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:poly_ms");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			ensurePermAttrByKey(p, ak("movement_speed"), MOD_MS, 0.02D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		}

		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			if (e.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL))
				e.setAmount(e.getAmount() * 0.85f);
		}
	}

	// ===== Styrogenesis (minor mining attr + occasional cave slowness) =====
	public static final class Styrogenesis implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:styro_mine");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			ensurePermAttrByKey(p, ak("mining_efficiency"), MOD_MINE, 0.03D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			boolean cavey = p.level().getMaxLocalRawBrightness(p.blockPosition()) <= 4;
			if (cavey && (p.tickCount % 80) == 0) {
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, false, false));
			}
		}
	}

	// ===== Biolubricant Chain (speed + tiny jump via attrs) =====
	public static final class BiolubricantChain implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:bio_ms");
		private static final net.minecraft.resources.ResourceLocation MOD_JUMP = net.minecraft.resources.ResourceLocation.parse("rtc_extras:bio_jump");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			ensurePermAttrByKey(p, ak("movement_speed"), MOD_MS, 0.02D * Math.max(0, level - 1), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			ensurePermAttrByKey(p, ak("jump_strength"), MOD_JUMP, 0.10D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
		}
	}

	// ===== Antifungal Serum (negative cleanses) =====
	public static final class AntifungalSerum implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS))
				p.removeEffect(net.minecraft.world.effect.MobEffects.BLINDNESS);
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS))
				p.removeEffect(net.minecraft.world.effect.MobEffects.DARKNESS);
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION))
				p.removeEffect(net.minecraft.world.effect.MobEffects.CONFUSION);
		}
	}

	// ===== Chemoautotrophy Pack (dark sustenance) =====
	public static final class ChemoautotrophyPack implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			boolean dark = p.level().getMaxLocalRawBrightness(p.blockPosition()) <= 4;
			if (dark && (p.tickCount % 100) == 0) {
				try {
					p.getFoodData().eat(1, 0.0f);
				} catch (Throwable ignored) {
				}
			}
		}
	}

	// ===== Biophotonic Array (NV + luck via attr) =====
	public static final class BiophotonicArray implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_LUCK = net.minecraft.resources.ResourceLocation.parse("rtc_extras:bioarray_luck");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 80, 0, false, false, false));
			ensurePermAttrByKey(p, ak("luck"), MOD_LUCK, 0.5D * Math.max(1, level), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
		}
	}

	// ===== Alien Neurotoxin Line (poison aura) =====
	public static final class AlienNeurotoxinLine implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if ((p.tickCount % 40) != 0)
				return;
			double r = 3.0 + level;
			var aabb = new net.minecraft.world.phys.AABB(p.getX() - r, p.getY() - r, p.getZ() - r, p.getX() + r, p.getY() + r, p.getZ() + r);
			for (var e : p.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb, t -> t != p && !t.getType().getCategory().isFriendly())) {
				e.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 40, 0, false, true, true));
			}
		}
	}

	// ===== Carbon Precursors (anti-fatigue + mining attr) =====
	public static final class CarbonPrecursors implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:carb_mine");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN))
				p.removeEffect(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN);
			ensurePermAttrByKey(p, ak("mining_efficiency"), MOD_MINE, 0.03D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		}
	}

	// ===== Fermentation Pathway (metabolic efficiency; tiny exhaustion refund) =====
	public static final class FermentationPathway implements InBodyHooks {
		private static final String CD = "rtc_ferm_tick";

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			// small mining efficiency bump via attribute (stacks with potions)
			ensurePermAttrByKey(p, ak("mining_efficiency"), net.minecraft.resources.ResourceLocation.parse("rtc_extras:ferm_mine"), 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			// gentle exhaustion relief ~1× per second while sprinting (conservative)
			long now = p.level().getGameTime();
			var tag = p.getPersistentData();
			long cd = tag.getLong(CD);
			if (p.isSprinting() && now >= cd) {
				tag.putLong(CD, now + 20L);
				p.causeFoodExhaustion(-0.0035f * level);
			}
		}
	}

	// ===== Spore Resistance (clear nausea/spore effects; brief “clarity” attributes) =====
	public static final class SporeResistance implements InBodyHooks {
		private static final String RECENT = "rtc_spore_recent";
		private static final net.minecraft.resources.ResourceLocation MOD_AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:spore_as");
		private static final net.minecraft.resources.ResourceLocation MOD_MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:spore_mine");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			boolean hadSporey = false;
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION)) {
				p.removeEffect(net.minecraft.world.effect.MobEffects.CONFUSION);
				hadSporey = true;
			}
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.BLINDNESS)) {
				p.removeEffect(net.minecraft.world.effect.MobEffects.BLINDNESS);
				hadSporey = true;
			}
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS)) {
				p.removeEffect(net.minecraft.world.effect.MobEffects.DARKNESS);
				hadSporey = true;
			}
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.HUNGER)) {
				p.removeEffect(net.minecraft.world.effect.MobEffects.HUNGER);
				hadSporey = true;
			}
			var tag = p.getPersistentData();
			long now = p.level().getGameTime();
			if (hadSporey)
				tag.putLong(RECENT, now + 100L); // 5s “clarity” window
			boolean clarity = now < tag.getLong(RECENT);
			// While “recently spored”, give precision via attrs (not potions)
			ensureToggleAttrByKey(p, ak("attack_speed"), MOD_AS, 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, clarity, false);
			ensureToggleAttrByKey(p, ak("mining_efficiency"), MOD_MINE, 0.05D * level, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, clarity, false);
		}
	}

	// ===== Acromegaly (big frame: +reach/+damage, slight -speed) =====
	public static final class Acromegaly implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_REACH_B = net.minecraft.resources.ResourceLocation.parse("rtc_extras:acro_reach_b");
		private static final net.minecraft.resources.ResourceLocation MOD_REACH_E = net.minecraft.resources.ResourceLocation.parse("rtc_extras:acro_reach_e");
		private static final net.minecraft.resources.ResourceLocation MOD_DMG = net.minecraft.resources.ResourceLocation.parse("rtc_extras:acro_dmg");
		private static final net.minecraft.resources.ResourceLocation MOD_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:acro_ms");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			double L = Math.max(1, level);
			// +reach (block & entity), +melee damage, -movement speed (conservative)
			try {
				ensurePermAttrByKey(p, ak("block_interaction_range"), MOD_REACH_B, 0.25D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
				ensurePermAttrByKey(p, ak("entity_interaction_range"), MOD_REACH_E, 0.25D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
			} catch (Throwable ignored) {
			}
			ensurePermAttrByKey(p, ak("attack_damage"), MOD_DMG, 0.05D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			ensurePermAttrByKey(p, ak("movement_speed"), MOD_MS, -0.03D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		}
	}

	// ===== Quantum Enzyme (short dash forward with cooldown; attr-free ability) =====
	public static final class QuantumEnzyme implements InBodyHooks {
		private static final String CD = "rtc_qe_cd";

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			// Simple rule: while sprinting & grounded & not riding, blink forward on cooldown
			if (!p.isSprinting() || p.isPassenger())
				return;
			var tag = p.getPersistentData();
			long now = p.level().getGameTime();
			long cd = tag.getLong(CD);
			if (now < cd)
				return;
			double dist = 2.5D + 0.75D * Math.min(3, level); // 2.5–4.75 blocks
			var look = p.getLookAngle();
			// horizontal dash (don’t fling upward)
			net.minecraft.world.phys.Vec3 dir = new net.minecraft.world.phys.Vec3(look.x, 0.0, look.z).normalize();
			if (dir.lengthSqr() < 1e-4)
				return;
			net.minecraft.world.phys.Vec3 from = p.position();
			net.minecraft.world.phys.Vec3 dest = from.add(dir.scale(dist));
			// Make sure destination is free (try same Y, then slight up, then slight down)
			net.minecraft.world.entity.player.Player pl = p;
			if (canStandAt(pl, dest)) {
				pl.teleportTo(dest.x, dest.y, dest.z);
			} else if (canStandAt(pl, dest.add(0, 1.0, 0))) {
				dest = dest.add(0, 1.0, 0);
				pl.teleportTo(dest.x, dest.y, dest.z);
			} else if (canStandAt(pl, dest.add(0, -1.0, 0))) {
				dest = dest.add(0, -1.0, 0);
				pl.teleportTo(dest.x, dest.y, dest.z);
			} else {
				return; // nowhere safe
			}
			p.resetFallDistance();
			tag.putLong(CD, now + (60L - 10L * Math.min(2, level))); // 3.0s → ~2.0s
			p.playNotifySound(net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.2f);
		}

		private static boolean canStandAt(net.minecraft.world.entity.player.Player p, net.minecraft.world.phys.Vec3 pos) {
			var aabb = p.getBoundingBox().move(pos.x - p.getX(), pos.y - p.getY(), pos.z - p.getZ());
			return p.level().noCollision(p, aabb);
		}
	}

	// ===== Adaptive Chitin (big armor + toughness + KB resist; mild speed tax) =====
	public static final class AdaptiveChitin implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_ARMOR = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adchitin_armor");
		private static final net.minecraft.resources.ResourceLocation MOD_TOUGH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adchitin_tough");
		private static final net.minecraft.resources.ResourceLocation MOD_KB = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adchitin_kb");
		private static final net.minecraft.resources.ResourceLocation MOD_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:adchitin_ms");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			double L = Math.max(1, level);
			ensurePermAttrByKey(p, ak("armor"), MOD_ARMOR, 2.0D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
			ensurePermAttrByKey(p, ak("armor_toughness"), MOD_TOUGH, 1.0D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
			ensurePermAttrByKey(p, ak("knockback_resistance"), MOD_KB, 0.05D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
			ensurePermAttrByKey(p, ak("movement_speed"), MOD_MS, -0.01D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		}
	}

	// ===== Advanced Nootropics (precision & throughput) =====
	public static final class AdvancedNootropics implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_AS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:noot_as");
		private static final net.minecraft.resources.ResourceLocation MOD_MINE = net.minecraft.resources.ResourceLocation.parse("rtc_extras:noot_mine");
		private static final net.minecraft.resources.ResourceLocation MOD_LUCK = net.minecraft.resources.ResourceLocation.parse("rtc_extras:noot_luck");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			double L = Math.max(1, level);
			ensurePermAttrByKey(p, ak("attack_speed"), MOD_AS, 0.12D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			ensurePermAttrByKey(p, ak("mining_efficiency"), MOD_MINE, 0.12D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			ensurePermAttrByKey(p, ak("luck"), MOD_LUCK, 1.0D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
		}
	}

	// ===== Phase-Change Mucus (sneak to slip through a 1-block wall; “slippery sprint”) =====
	public static final class PhaseChangeMucus implements InBodyHooks {
		private static final String CD = "rtc_pcm_cd";
		private static final net.minecraft.resources.ResourceLocation MOD_SPRINT = net.minecraft.resources.ResourceLocation.parse("rtc_extras:pcm_sprint");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			// Slippery sprint (attr, stacks with potions)
			boolean sprint = p.isSprinting();
			ensureToggleAttrByKey(p, ak("movement_speed"), MOD_SPRINT, 0.05D * Math.min(1, level), net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, sprint, false);
			// Sneak to phase through a 1-block wall directly in front (server-side)
			if (!p.isShiftKeyDown())
				return;
			var tag = p.getPersistentData();
			long now = p.level().getGameTime();
			long cd = tag.getLong(CD);
			if (now < cd)
				return;
			// Short ray in look direction to detect an immediate wall
			net.minecraft.world.phys.Vec3 eye = p.getEyePosition();
			net.minecraft.world.phys.Vec3 look = p.getLookAngle();
			net.minecraft.world.phys.Vec3 to = eye.add(look.scale(1.6D));
			var hit = p.level().clip(new net.minecraft.world.level.ClipContext(eye, to, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, p));
			if (hit.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK)
				return;
			var wall = hit.getBlockPos();
			var dir = hit.getDirection(); // face we hit
			var beyond = wall.relative(dir);
			// need two-block-tall clearance just beyond the wall
			if (!isPassable(p.level(), beyond) || !isPassable(p.level(), beyond.above()))
				return;
			// Teleport just beyond the wall, centered, keeping roughly current Y
			double destY = Math.max(p.getY(), beyond.getY());
			net.minecraft.world.phys.Vec3 dest = new net.minecraft.world.phys.Vec3(beyond.getX() + 0.5, destY, beyond.getZ() + 0.5);
			// final safety: collision check
			if (!p.level().noCollision(p, p.getBoundingBox().move(dest.x - p.getX(), dest.y - p.getY(), dest.z - p.getZ())))
				return;
			p.teleportTo(dest.x, dest.y, dest.z);
			p.resetFallDistance();
			tag.putLong(CD, now + (40L - 5L * Math.min(2, level))); // ~2.0s base
			p.playNotifySound(net.minecraft.sounds.SoundEvents.SLIME_BLOCK_PLACE, net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.1f);
		}

		private static boolean isPassable(net.minecraft.world.level.Level lvl, net.minecraft.core.BlockPos pos) {
			var st = lvl.getBlockState(pos);
			return st.isAir() || st.getCollisionShape(lvl, pos).isEmpty();
		}
	}

	// ===== Hardened Carapace (heavy plating) =====
	public static final class HardenedCarapace implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_ARMOR = net.minecraft.resources.ResourceLocation.parse("rtc_extras:carapace_armor");
		private static final net.minecraft.resources.ResourceLocation MOD_TOUGH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:carapace_tough");
		private static final net.minecraft.resources.ResourceLocation MOD_MS = net.minecraft.resources.ResourceLocation.parse("rtc_extras:carapace_ms");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			double L = Math.max(1, level);
			ensurePermAttrByKey(p, ak("armor"), MOD_ARMOR, 4.0D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
			ensurePermAttrByKey(p, ak("armor_toughness"), MOD_TOUGH, 2.0D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE);
			ensurePermAttrByKey(p, ak("movement_speed"), MOD_MS, -0.18D * L, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		}
	}
}