package net.crizo.rtcextras;

import org.checkerframework.checker.units.qual.t;
import org.checkerframework.checker.units.qual.h;

@net.neoforged.fml.common.EventBusSubscriber
public final class GeneEffects {
	// ========== registries ==========
	private static final java.util.Map<String, InBodyHooks> IN_BODY = new java.util.HashMap<>();
	private static final java.util.Map<String, OnDecayHooks> ON_DECAY = new java.util.HashMap<>();
	private static final java.util.Map<String, OnProductionHooks> ON_PROD = new java.util.HashMap<>();

	public static void init() {
		// Already present
		registerInBody("immortal_cell_cycle", new ImmortalCellCycle());
		registerInBody("bioluminescence", new Bioluminescence());
		registerInBody("hypermetabolism", new Hypermetabolism());
		// Player-centric behaviors from your JSON
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
		// Decay/production special cases
		registerOnDecay("nitration_pack", new NitrationPackOnDecay());
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

	private static String fq(String id) {
		return id.indexOf(':') >= 0 ? id : "rtc_extras:" + id;
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

	// ========== Hook interfaces ==========
	public interface InBodyHooks {
		default void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
		}

		default void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
		}

		default void onDeath(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingDeathEvent e, int level) {
		}
	}

	public interface OnDecayHooks {
		void onDecay(GeneEngine.Ctx c, int stackCount);
	}

	public interface OnProductionHooks {
		void onProduction(GeneEngine.Ctx c, int producedNow);
	}

	// ========== Example in-body modules ==========
	// One free death per in-game day
	public static final class ImmortalCellCycle implements InBodyHooks {
		private static final String KEY_LAST_DAY = "rtc_icc_last_day";
		private static final String KEY_TICK_GUARD = "rtc_icc_guard_tick";

		@Override
		public void onDeath(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingDeathEvent e, int level) {
			long gameTime = le.level().getGameTime();
			long dayIndex = gameTime / 24000L;
			var tag = le.getPersistentData();
			long last = tag.getLong(KEY_LAST_DAY);
			long guardTick = tag.getLong(KEY_TICK_GUARD);
			if (guardTick == gameTime)
				return; // same-tick guard
			if (last == dayIndex)
				return; // already used today
			// consume the daily charge
			tag.putLong(KEY_LAST_DAY, dayIndex);
			tag.putLong(KEY_TICK_GUARD, gameTime);
			e.setCanceled(true);
			float heal = Math.max(1.0f, Math.min(le.getMaxHealth(), le.getMaxHealth() * 0.40f + 2.0f * level));
			le.setHealth(heal);
			le.removeAllEffects();
			le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 200, Math.min(2, level)));
			le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 200, Math.min(1, level - 1)));
			le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 400, Math.min(2, level)));
			le.level().levelEvent(2003, le.blockPosition(), 0);
			if (le instanceof net.minecraft.world.entity.player.Player p) {
				p.playNotifySound(net.minecraft.sounds.SoundEvents.TOTEM_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.0f);
			}
		}
	}

	// Soft light in darkness (cheap: refresh tiny NV without particles)
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

	// Haste + Speed that stacks by installed count
	public static final class Hypermetabolism implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			int amp = Math.max(0, level - 1);
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 40, amp, false, false, false));
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 40, amp, false, false, false));
		}
	}

	// --- Sickle Cell Trait: strong anti-poison; slight stamina drain ---
	public static final class SickleCellDisease implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			// remove/short-circuit poison each tick
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) {
				p.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
			}
			// small rolling exhaustion (stamina drain)
			p.causeFoodExhaustion(0.0025F * level);
		}
	}

	// --- Hemophilia: bleed on damage; while bleeding grant brief strength+haste ---
	public static final class Hemophilia implements InBodyHooks {
		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			if (le.level().isClientSide())
				return;
			le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 40 + 20 * level, 0, false, true, true));
			if (le instanceof net.minecraft.world.entity.player.Player p) {
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 60, Math.max(0, level - 1), false, false, false));
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 60, Math.max(0, level - 1), false, false, false));
			}
		}
	}

	// --- Venom biosynthesis: melee hits poison the victim ---
	public static final class VenomBiosynthesis implements InBodyHooks {
		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity victim, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			var src = e.getSource().getEntity();
			if (!(src instanceof net.minecraft.world.entity.LivingEntity attacker))
				return;
			// Only apply if the ATTACKER has the gene
			if (getInstalledLevel(attacker, "venom_biosynthesis") <= 0)
				return;
			victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, 40 + 20 * level, Math.min(2, level - 1)));
		}
	}

	// --- Thermogenesis: small flame "thorns" when hit ---
	public static final class Thermogenesis implements InBodyHooks {
		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			var srcEnt = e.getSource().getEntity();
			if (srcEnt instanceof net.minecraft.world.entity.LivingEntity atk) {
				atk.igniteForSeconds(1 + level);
			}
		}
	}

	// --- Hemoglobin variant: water breathing / low-O2 support ---
	public static final class HemoglobinVariant implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING, 60, 0, false, false, false));
		}
	}

	// --- Adrenaline trigger: low HP → strength + speed bursts ---
	public static final class AdrenalineTrigger implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			if (p.getHealth() <= p.getMaxHealth() * 0.3f) {
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 60, Math.min(2, level), false, false, false));
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 60, Math.min(1, level), false, false, false));
			}
		}
	}

	// --- Chloroplast optimization: sun = buff; dark = slight penalty ---
	public static final class ChloroplastOptimization implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			boolean sun = p.level().canSeeSky(p.blockPosition()) && p.level().isDay() && p.level().getMaxLocalRawBrightness(p.blockPosition()) >= 12;
			if (sun) {
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 40, 0, false, false, false));
			} else {
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN, 40, 0, false, false, false));
			}
		}
	}

	// --- Burst regeneration: big heal after taking damage ---
	public static final class BurstRegeneration implements InBodyHooks {
		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			le.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 80 + 20 * level, Math.min(2, level), false, true, true));
		}
	}

	// --- Toxinase: immune to poison; reduced wither ---
	public static final class Toxinase implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.hasEffect(net.minecraft.world.effect.MobEffects.POISON))
				p.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
			var wither = p.getEffect(net.minecraft.world.effect.MobEffects.WITHER);
			if (wither != null && wither.getDuration() > 0) {
				// halve the remaining time periodically
				if ((p.tickCount % 20) == 0)
					p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, wither.getDuration() / 2, wither.getAmplifier(), false, true, true));
			}
		}
	}

	// --- Kinetic muscle: jump + speed; reduce fall damage ---
	public static final class KineticMuscle implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.JUMP, 40, Math.min(2, level), false, false, false));
			p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 40, Math.min(1, level), false, false, false));
		}

		@Override
		public void onIncomingDamage(net.minecraft.world.entity.LivingEntity le, net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent e, int level) {
			if (e.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
				e.setAmount(e.getAmount() * (0.8f - 0.1f * Math.min(level, 3))); // up to 50% reduction
			}
		}
	}

	// --- Marfan syndrome: +reach, -max health (attributes) ---
	//might not work cuz of the change in syntax. 
	public static final class MarfanSyndrome implements InBodyHooks {
		private static final net.minecraft.resources.ResourceLocation MOD_REACH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:marfan_reach");
		private static final net.minecraft.resources.ResourceLocation MOD_HEALTH = net.minecraft.resources.ResourceLocation.parse("rtc_extras:marfan_health");

		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			// reach (block + entity) — attribute names vary; try both NeoForge/Forge names defensively
			try {
				var reachBlk = net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE;
				var reachEnt = net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE;
				var a1 = p.getAttribute(reachBlk);
				var a2 = p.getAttribute(reachEnt);
				if (a1 != null && a1.getModifier(MOD_REACH) == null) {
					a1.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(MOD_REACH, 0.75D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
				}
				if (a2 != null && a2.getModifier(MOD_REACH) == null) {
					a2.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(MOD_REACH, 0.75D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
				}
			} catch (Throwable ignored) {
			}
			// reduce max health slightly once
			var mh = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
			if (mh != null && mh.getModifier(MOD_HEALTH) == null) {
				mh.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(MOD_HEALTH, -2.0D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
			}
		}
	}

	// --- Neuroinhibitor aura: weakness around the host ---
	public static final class NeuroinhibitorAura implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			var r = 4.0 + level;
			var aabb = new net.minecraft.world.phys.AABB(p.getX() - r, p.getY() - r, p.getZ() - r, p.getX() + r, p.getY() + r, p.getZ() + r);
			for (var e : p.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, aabb, t -> t != p && t.getType().getCategory().isFriendly() == false)) {
				e.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 40, 0));
			}
		}
	}

	// --- Photosynthesis: restore hunger in sunlight ---
	public static final class Photosynthesis implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			if (p.level().canSeeSky(p.blockPosition()) && p.level().isDay() && p.level().getMaxLocalRawBrightness(p.blockPosition()) >= 12) {
				if ((p.tickCount % 80) == 0) { // ~4s
					try {
						p.getFoodData().eat(1, 0.2f);
					} catch (Throwable ignored) {
					}
				}
			}
		}
	}

	// --- Photoshade Negentropy: heal + hunger freeze in darkness ---
	public static final class PhotoshadeNegentropy implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			boolean dark = p.level().getMaxLocalRawBrightness(p.blockPosition()) <= 4;
			if (dark) {
				if ((p.tickCount % 40) == 0)
					p.heal(0.5f);
				// Counter exhaustion (roughly)
				p.causeFoodExhaustion(-0.01f * level);
			}
		}
	}

	// --- Prion Instability: slow degeneration; rare chaotic surges ---
	public static final class PrionInstability implements InBodyHooks {
		@Override
		public void onPlayerTick(net.minecraft.world.entity.player.Player p, int level) {
			if (p.level().isClientSide())
				return;
			if ((p.tickCount % 200) == 0) { // periodic chip damage
				p.hurt(p.damageSources().magic(), 0.5f);
			}
			// small chance to re-trigger one of the other modules randomly
			if (p.getRandom().nextDouble() < 0.005 * level) {
				p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONFUSION, 100, 0));
			}
		}
	}

	// --- Nitration Pack (decay): tiny pop explosion w/o block damage ---
	public static final class NitrationPackOnDecay implements OnDecayHooks {
		@Override
		public void onDecay(GeneEngine.Ctx c, int stackCount) {
			double p = Math.min(0.001 * stackCount, 0.02);
			if (c.rng().nextDouble() < p) {
				c.level().explode(c.source(), c.x(), c.y(), c.z(), 0.6f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);
			}
		}
	}
}