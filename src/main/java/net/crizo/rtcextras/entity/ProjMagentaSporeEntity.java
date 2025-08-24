package net.crizo.rtcextras.entity;

@net.neoforged.api.distmarker.OnlyIn(value = net.neoforged.api.distmarker.Dist.CLIENT, _interface = net.minecraft.world.entity.projectile.ItemSupplier.class)
public class ProjMagentaSporeEntity extends net.minecraft.world.entity.projectile.AbstractArrow implements net.minecraft.world.entity.projectile.ItemSupplier {
	public static final net.minecraft.world.item.ItemStack PROJECTILE_ITEM = new net.minecraft.world.item.ItemStack(net.crizo.rtcextras.init.RtcExtrasModItems.MAGENTA_SPORE_SINGLET.get());
	private int knockback = 0;
	// Tuning for the fake explosion
	private static final double AOE_RADIUS = 1.5; // very small range
	private static final float AOE_DAMAGE = 2.0F; // small damage

	public ProjMagentaSporeEntity(net.minecraft.world.entity.EntityType<? extends ProjMagentaSporeEntity> type, net.minecraft.world.level.Level world) {
		super(type, world);
	}

	public ProjMagentaSporeEntity(net.minecraft.world.entity.EntityType<? extends ProjMagentaSporeEntity> type, double x, double y, double z, net.minecraft.world.level.Level world,
			@javax.annotation.Nullable net.minecraft.world.item.ItemStack firedFromWeapon) {
		super(type, x, y, z, world, PROJECTILE_ITEM, firedFromWeapon);
		if (firedFromWeapon != null) {
			setKnockback(net.minecraft.world.item.enchantment.EnchantmentHelper
					.getItemEnchantmentLevel(world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK), firedFromWeapon));
		}
	}

	public ProjMagentaSporeEntity(net.minecraft.world.entity.EntityType<? extends ProjMagentaSporeEntity> type, net.minecraft.world.entity.LivingEntity shooter, net.minecraft.world.level.Level world,
			@javax.annotation.Nullable net.minecraft.world.item.ItemStack firedFromWeapon) {
		super(type, shooter, world, PROJECTILE_ITEM, firedFromWeapon);
		if (firedFromWeapon != null) {
			setKnockback(net.minecraft.world.item.enchantment.EnchantmentHelper
					.getItemEnchantmentLevel(world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK), firedFromWeapon));
		}
	}

	@Override
	@net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
	public net.minecraft.world.item.ItemStack getItem() {
		return PROJECTILE_ITEM;
	}

	@Override
	protected net.minecraft.world.item.ItemStack getDefaultPickupItem() {
		return new net.minecraft.world.item.ItemStack(net.crizo.rtcextras.init.RtcExtrasModItems.MAGENTA_SPORE_SINGLET.get());
	}

	@Override
	protected void doPostHurtEffects(net.minecraft.world.entity.LivingEntity entity) {
		super.doPostHurtEffects(entity);
		entity.setArrowCount(entity.getArrowCount() - 1);
	}

	public void setKnockback(int knockback) {
		this.knockback = knockback;
	}

	@Override
	protected void doKnockback(net.minecraft.world.entity.LivingEntity livingEntity, net.minecraft.world.damagesource.DamageSource damageSource) {
		if (knockback > 0.0) {
			double d1 = Math.max(0.0, 1.0 - livingEntity.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE));
			net.minecraft.world.phys.Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale(knockback * 0.6 * d1);
			if (vec3.lengthSqr() > 0.0) {
				livingEntity.push(vec3.x, 0.1, vec3.z);
			}
		} else {
			super.doKnockback(livingEntity, damageSource);
		}
	}

	/* -----------------------
	   Impact handling
	   ----------------------- */
	@Override
	protected void onHitEntity(net.minecraft.world.phys.EntityHitResult hit) {
		super.onHitEntity(hit); // keeps normal arrow-on-entity damage
		// Pop where we hit the entity
		this.fakePopAndDamage(this.level(), this.position());
		this.discard();
	}

	@Override
	protected void onHitBlock(net.minecraft.world.phys.BlockHitResult hit) {
		super.onHitBlock(hit);
		// Try to plant on top if innoculable & top face
		tryPlantRedShroomling(this.level(), hit);
		// Pop right at the impact point
		this.fakePopAndDamage(this.level(), net.minecraft.world.phys.Vec3.atCenterOf(hit.getBlockPos()));
		this.discard();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.inGround) {
			// Safety: if somehow we get stuck without onHitBlock firing, still pop
			this.fakePopAndDamage(this.level(), this.position());
			this.discard();
		}
	}

	/* -----------------------
	   Fire helpers
	   ----------------------- */
	public static ProjMagentaSporeEntity shoot(net.minecraft.world.level.Level world, net.minecraft.world.entity.LivingEntity entity, net.minecraft.util.RandomSource source) {
		return shoot(world, entity, source, 1f, 5, 0);
	}

	public static ProjMagentaSporeEntity shoot(net.minecraft.world.level.Level world, net.minecraft.world.entity.LivingEntity entity, net.minecraft.util.RandomSource source, float pullingPower) {
		return shoot(world, entity, source, pullingPower * 1f, 5, 0);
	}

	public static ProjMagentaSporeEntity shoot(net.minecraft.world.level.Level world, net.minecraft.world.entity.LivingEntity entity, net.minecraft.util.RandomSource random, float power, double damage, int knockback) {
		ProjMagentaSporeEntity entityarrow = new ProjMagentaSporeEntity(net.crizo.rtcextras.init.RtcExtrasModEntities.PROJ_MAGENTA_SPORE.get(), entity, world, null);
		entityarrow.shoot(entity.getViewVector(1).x, entity.getViewVector(1).y, entity.getViewVector(1).z, power * 2, 0);
		entityarrow.setSilent(true);
		entityarrow.setCritArrow(false);
		entityarrow.setBaseDamage(damage);
		entityarrow.setKnockback(knockback);
		world.addFreshEntity(entityarrow);
		return entityarrow;
	}

	public static ProjMagentaSporeEntity shoot(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.entity.LivingEntity target) {
		ProjMagentaSporeEntity entityarrow = new ProjMagentaSporeEntity(net.crizo.rtcextras.init.RtcExtrasModEntities.PROJ_MAGENTA_SPORE.get(), entity, entity.level(), null);
		double dx = target.getX() - entity.getX();
		double dy = target.getY() + target.getEyeHeight() - 1.1;
		double dz = target.getZ() - entity.getZ();
		entityarrow.shoot(dx, dy - entityarrow.getY() + Math.hypot(dx, dz) * 0.2F, dz, 1f * 2, 12.0F);
		entityarrow.setSilent(true);
		entityarrow.setBaseDamage(5);
		entityarrow.setKnockback(0);
		entityarrow.setCritArrow(false);
		entity.level().addFreshEntity(entityarrow);
		return entityarrow;
	}

	/* -----------------------
	   Fake explosion + shroom planting
	   ----------------------- */
	private void fakePopAndDamage(net.minecraft.world.level.Level level, net.minecraft.world.phys.Vec3 at) {
		if (level == null)
			return;
		// Play custom pop sound with pitch variance
		float pitch = 0.85F + level.getRandom().nextFloat() * 0.3F;
		net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.parse("rtc_extras:shroom_explode");
		net.minecraft.sounds.SoundSource src = net.minecraft.sounds.SoundSource.NEUTRAL;
		if (!level.isClientSide) {
			level.playSound(null, net.minecraft.core.BlockPos.containing(at.x, at.y, at.z), net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(rl), src, 1.0F, pitch);
		} else {
			level.playLocalSound(at.x, at.y, at.z, net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(rl), src, 1.0F, pitch, false);
		}
		if (!level.isClientSide) {
			// Small AoE damage (no real explosion, no block breaking)
			double r = AOE_RADIUS;
			net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(at.x - r, at.y - r, at.z - r, at.x + r, at.y + r, at.z + r);
			java.util.List<net.minecraft.world.entity.Entity> hits = level.getEntities(this, box);
			for (net.minecraft.world.entity.Entity e : hits) {
				if (e == this)
					continue;
				if (this.getOwner() != null && e.getUUID().equals(this.getOwner().getUUID()))
					continue; // don't pop owner
				// deal small damage
				e.hurt(level.damageSources().indirectMagic(this, (this.getOwner() instanceof net.minecraft.world.entity.LivingEntity l) ? l : null), AOE_DAMAGE);
				// tiny push away from center to sell the "pop"
				net.minecraft.world.phys.Vec3 dir = e.position().subtract(at).normalize();
				if (dir.lengthSqr() > 0) {
					e.push(dir.x * 0.15, 0.05, dir.z * 0.15);
				}
			}
		}
		// (Optional) small particles for feedback on client
		if (level.isClientSide) {
			for (int i = 0; i < 8; i++) {
				level.addParticle(net.minecraft.core.particles.ParticleTypes.POOF, at.x, at.y, at.z, 0, 0, 0);
			}
		}
	}

	private static void tryPlantRedShroomling(net.minecraft.world.level.Level level, net.minecraft.world.phys.BlockHitResult hit) {
		if (level.isClientSide)
			return;
		net.minecraft.core.BlockPos basePos = hit.getBlockPos();
		net.minecraft.core.Direction face = hit.getDirection();
		// Must hit the TOP face
		if (face != net.minecraft.core.Direction.UP)
			return;
		net.minecraft.world.level.block.state.BlockState belowState = level.getBlockState(basePos);
		// Block must be tagged rtc_extras:innoculable and have sturdy top
		boolean validBlock = belowState.is(net.minecraft.tags.BlockTags.create(net.minecraft.resources.ResourceLocation.parse("rtc_extras:innoculable")));
		boolean sturdyTop = belowState.isFaceSturdy(level, basePos, net.minecraft.core.Direction.UP);
		if (!validBlock || !sturdyTop)
			return;
		net.minecraft.core.BlockPos placePos = basePos.above();
		net.minecraft.world.level.block.state.BlockState aboveState = level.getBlockState(placePos);
		boolean spaceClear = level.isEmptyBlock(placePos) || aboveState.canBeReplaced();
		if (!spaceClear)
			return;
		net.minecraft.world.level.block.state.BlockState toPlace = net.crizo.rtcextras.init.RtcExtrasModBlocks.MAGENTA_VELUTIPE_SHROOMLING.get().defaultBlockState();
		if (!toPlace.canSurvive(level, placePos))
			return;
		if (level.getRandom().nextFloat() >= 0.10F)
			return;
		level.setBlock(placePos, toPlace, 3);
	}
}