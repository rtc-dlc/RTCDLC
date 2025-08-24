package net.crizo.rtcextras.procedures;
/* imports */

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.BlockPos;

import net.crizo.rtcextras.init.RtcExtrasModEntities;
import net.crizo.rtcextras.entity.ProjRedSporeEntity;
import net.crizo.rtcextras.entity.ProjMagentaSporeEntity;
import net.crizo.rtcextras.entity.ProjBrownSporeEntity;

public class ShroomCapDecayBurstProcedure {
	public enum SporeColor {
		BROWN, RED, MAGENTA
	}

	/** String-friendly overload: color can be "brown", "red", or "magenta" (case-insensitive). */
	public static void execute(LevelAccessor world, double x, double y, double z, int dirX, int dirY, int dirZ, String colorName) {
		SporeColor color = SporeColor.BROWN; // default
		if (colorName != null) {
			switch (colorName.toLowerCase()) {
				case "red" -> color = SporeColor.RED;
				case "magenta" -> color = SporeColor.MAGENTA;
				case "brown" -> color = SporeColor.BROWN;
			}
		}
		execute(world, x, y, z, dirX, dirY, dirZ, color);
	}

	/** Main overload: pass the color enum directly. */
	public static void execute(LevelAccessor world, double x, double y, double z, int dirX, int dirY, int dirZ, SporeColor color) {
		if (!(world instanceof Level lvl))
			return;
		final Vec3 origin = new Vec3(x, y, z);
		final RandomSource rand = lvl.getRandom();
		// --- sound (random pitch) ---
		float pitch = 0.85F + rand.nextFloat() * 0.3F;
		var sEvt = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("rtc_extras:shroom_explode"));
		if (!lvl.isClientSide) {
			lvl.playSound(null, BlockPos.containing(x, y, z), sEvt, SoundSource.NEUTRAL, 0.5F, pitch);
		} else {
			lvl.playLocalSound(x, y, z, sEvt, SoundSource.NEUTRAL, 0.5F, pitch, false);
		}
		// --- particles (block particles from the cap itself) ---
		BlockState stateAt = lvl.getBlockState(BlockPos.containing(x, y, z));
		for (int i = 0; i < 12; i++) {
			lvl.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, stateAt), x, y, z, 0, 0, 0);
		}
		// --- tiny fake AoE damage (no block breaking) ---
		if (!lvl.isClientSide) {
			double r = 1.5;
			AABB box = new AABB(x - r, y - r, z - r, x + r, y + r, z + r);
			for (Entity e : ((ServerLevel) lvl).getEntitiesOfClass(Entity.class, box)) {
				if (e.isRemoved())
					continue;
				e.hurt(((ServerLevel) lvl).damageSources().generic(), 2.0F);
				Vec3 dir = e.position().subtract(origin).normalize();
				if (dir.lengthSqr() > 0)
					e.push(dir.x * 0.15, 0.05, dir.z * 0.15);
			}
		}
		// --- spore cone in preferred direction ---
		if (lvl instanceof ServerLevel sl) {
			Vec3 axis = new Vec3(dirX, dirY, dirZ);
			// Fallback to straight up if the input dir is zero
			if (axis.lengthSqr() < 1.0e-6) {
				axis = new Vec3(0, 1, 0);
			} else {
				axis = axis.normalize();
			}
			final float speed = 0.9f;
			final double maxAngleRad = Math.toRadians(25);
			int count = Mth.nextInt(rand, 0, 2);
			for (int i = 0; i < count; i++) {
				Vec3 dir = randomDirInCone(rand, axis, maxAngleRad);
				spawnSpore(sl, x, y, z, dir, speed, color);
			}
		}
	}

	// ---------------- helpers ----------------
	private static void spawnSpore(ServerLevel sl, double x, double y, double z, Vec3 dir, float speed, SporeColor color) {
		switch (color) {
			case RED -> {
				var proj = new ProjRedSporeEntity(RtcExtrasModEntities.PROJ_RED_SPORE.get(), x, y, z, sl, null);
				proj.setOwner(null);
				proj.setBaseDamage(2.0);
				proj.setSilent(true);
				proj.setCritArrow(false);
				proj.pickup = AbstractArrow.Pickup.DISALLOWED;
				proj.shoot(dir.x, dir.y, dir.z, speed, 0);
				sl.addFreshEntity(proj);
			}
			case MAGENTA -> {
				var proj = new ProjMagentaSporeEntity(RtcExtrasModEntities.PROJ_MAGENTA_SPORE.get(), x, y, z, sl, null);
				proj.setOwner(null);
				proj.setBaseDamage(2.0);
				proj.setSilent(true);
				proj.setCritArrow(false);
				proj.pickup = AbstractArrow.Pickup.DISALLOWED;
				proj.shoot(dir.x, dir.y, dir.z, speed, 0);
				sl.addFreshEntity(proj);
			}
			case BROWN -> {
				var proj = new ProjBrownSporeEntity(RtcExtrasModEntities.PROJ_BROWN_SPORE.get(), x, y, z, sl, null);
				proj.setOwner(null);
				proj.setBaseDamage(2.0);
				proj.setSilent(true);
				proj.setCritArrow(false);
				proj.pickup = AbstractArrow.Pickup.DISALLOWED;
				proj.shoot(dir.x, dir.y, dir.z, speed, 0);
				sl.addFreshEntity(proj);
			}
		}
	}

	private static Vec3 randomDirInCone(RandomSource rand, Vec3 axis, double maxAngle) {
		Vec3 a = axis.normalize();
		Vec3 w = Math.abs(a.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
		Vec3 u = a.cross(w).normalize();
		Vec3 v = a.cross(u).normalize();
		double cosMax = Math.cos(maxAngle);
		double cosAlpha = Mth.lerp(rand.nextDouble(), cosMax, 1.0);
		double sinAlpha = Math.sqrt(Math.max(0.0, 1.0 - cosAlpha * cosAlpha));
		double theta = rand.nextDouble() * (Math.PI * 2);
		Vec3 ring = u.scale(Math.cos(theta)).add(v.scale(Math.sin(theta))).scale(sinAlpha);
		return a.scale(cosAlpha).add(ring).normalize();
	}
}