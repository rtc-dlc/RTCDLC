package net.crizo.rtcextras.procedures;

@net.neoforged.fml.common.EventBusSubscriber
public class ProShroomExplodeProcedure {
	@net.neoforged.bus.api.SubscribeEvent
	public static void onBlockBreak(net.neoforged.neoforge.event.level.BlockEvent.BreakEvent event) {
		execute(event, event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getState());
	}

	public static void execute(net.minecraft.world.level.LevelAccessor world, double x, double y, double z, net.minecraft.world.level.block.state.BlockState blockstate) {
		execute(null, world, x, y, z, blockstate);
	}

	private static void execute(@javax.annotation.Nullable net.neoforged.bus.api.Event event, net.minecraft.world.level.LevelAccessor world, double x, double y, double z, net.minecraft.world.level.block.state.BlockState blockstate) {
		// Where to spawn & where to aim (player eye)
		final net.minecraft.world.phys.Vec3 origin = new net.minecraft.world.phys.Vec3(x + 0.5, y + 0.5, z + 0.5);
		net.minecraft.world.phys.Vec3 axis = new net.minecraft.world.phys.Vec3(0, 1, 0); // fallback axis
		if (event instanceof net.neoforged.neoforge.event.level.BlockEvent.BreakEvent be && be.getPlayer() != null) {
			net.minecraft.world.entity.player.Player player = be.getPlayer();
			net.minecraft.world.phys.Vec3 toPlayerEye = player.getEyePosition(1.0F).subtract(origin);
			if (toPlayerEye.lengthSqr() > 1.0e-6)
				axis = toPlayerEye.normalize();
		}
		if (world instanceof net.minecraft.server.level.ServerLevel projectileLevel) {
			final net.minecraft.util.RandomSource rand = projectileLevel.getRandom();
			final float speed = 0.9f; // tweak to taste
			final double maxAngleRad = Math.toRadians(25); // cone half-angle
			if (blockstate.getBlock() == net.crizo.rtcextras.init.RtcExtrasModBlocks.BROWN_VELUTIPE_MUSHROOM.get()) {
				if (Math.random() < 0.33) {
					int n = net.minecraft.util.Mth.nextInt(rand, 3, 6);
					for (int i = 0; i < n; i++) {
						net.minecraft.world.entity.projectile.Projectile proj = initArrowProjectile(
								new net.crizo.rtcextras.entity.ProjBrownSporeEntity(net.crizo.rtcextras.init.RtcExtrasModEntities.PROJ_BROWN_SPORE.get(), 0, 0, 0, projectileLevel, createArrowWeaponItemStack(projectileLevel, 1, (byte) 0)), null, 5,
								true, false, false, net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED);
						proj.setPos(origin.x, origin.y, origin.z);
						net.minecraft.world.phys.Vec3 dir = randomDirInCone(rand, axis, maxAngleRad);
						proj.shoot(dir.x, dir.y, dir.z, speed, 0);
						projectileLevel.addFreshEntity(proj);
					}
				}
			} else if (blockstate.getBlock() == net.crizo.rtcextras.init.RtcExtrasModBlocks.RED_VELUTIPE_MUSHROOM.get()) {
				if (Math.random() < 0.33) {
					int n = net.minecraft.util.Mth.nextInt(rand, 3, 6);
					for (int i = 0; i < n; i++) {
						net.minecraft.world.entity.projectile.Projectile proj = initArrowProjectile(
								new net.crizo.rtcextras.entity.ProjRedSporeEntity(net.crizo.rtcextras.init.RtcExtrasModEntities.PROJ_RED_SPORE.get(), 0, 0, 0, projectileLevel, createArrowWeaponItemStack(projectileLevel, 1, (byte) 0)), null, 5, true,
								false, false, net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED);
						proj.setPos(origin.x, origin.y, origin.z);
						net.minecraft.world.phys.Vec3 dir = randomDirInCone(rand, axis, maxAngleRad);
						proj.shoot(dir.x, dir.y, dir.z, speed, 0);
						projectileLevel.addFreshEntity(proj);
					}
				}
			} else if (blockstate.getBlock() == net.crizo.rtcextras.init.RtcExtrasModBlocks.MAGENTA_VELUTIPE_MUSHROOM.get()) {
				if (Math.random() < 0.33) {
					int n = net.minecraft.util.Mth.nextInt(rand, 3, 6);
					for (int i = 0; i < n; i++) {
						net.minecraft.world.entity.projectile.Projectile proj = initArrowProjectile(
								new net.crizo.rtcextras.entity.ProjMagentaSporeEntity(net.crizo.rtcextras.init.RtcExtrasModEntities.PROJ_MAGENTA_SPORE.get(), 0, 0, 0, projectileLevel, createArrowWeaponItemStack(projectileLevel, 1, (byte) 0)), null,
								5, true, false, false, net.minecraft.world.entity.projectile.AbstractArrow.Pickup.DISALLOWED);
						proj.setPos(origin.x, origin.y, origin.z);
						net.minecraft.world.phys.Vec3 dir = randomDirInCone(rand, axis, maxAngleRad);
						proj.shoot(dir.x, dir.y, dir.z, speed, 0);
						projectileLevel.addFreshEntity(proj);
					}
				}
			}
		}
	}

	private static net.minecraft.core.Direction getDirectionFromBlockState(net.minecraft.world.level.block.state.BlockState blockState) {
		net.minecraft.world.level.block.state.properties.Property<?> prop = blockState.getBlock().getStateDefinition().getProperty("facing");
		if (prop instanceof net.minecraft.world.level.block.state.properties.DirectionProperty dp)
			return blockState.getValue(dp);
		prop = blockState.getBlock().getStateDefinition().getProperty("axis");
		return prop instanceof net.minecraft.world.level.block.state.properties.EnumProperty ep && ep.getPossibleValues().toArray()[0] instanceof net.minecraft.core.Direction.Axis
				? net.minecraft.core.Direction.fromAxisAndDirection((net.minecraft.core.Direction.Axis) blockState.getValue(ep), net.minecraft.core.Direction.AxisDirection.POSITIVE)
				: net.minecraft.core.Direction.NORTH;
	}

	private static net.minecraft.world.entity.projectile.AbstractArrow initArrowProjectile(net.minecraft.world.entity.projectile.AbstractArrow entityToSpawn, net.minecraft.world.entity.Entity shooter, float damage, boolean silent, boolean fire,
			boolean particles, net.minecraft.world.entity.projectile.AbstractArrow.Pickup pickup) {
		entityToSpawn.setOwner(shooter);
		entityToSpawn.setBaseDamage(damage);
		if (silent)
			entityToSpawn.setSilent(true);
		if (fire)
			entityToSpawn.igniteForSeconds(100);
		if (particles)
			entityToSpawn.setCritArrow(true);
		entityToSpawn.pickup = pickup;
		return entityToSpawn;
	}

	private static net.minecraft.world.item.ItemStack createArrowWeaponItemStack(net.minecraft.world.level.Level level, int knockback, byte piercing) {
		net.minecraft.world.item.ItemStack weapon = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW);
		if (knockback > 0)
			weapon.enchant(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.KNOCKBACK), knockback);
		if (piercing > 0)
			weapon.enchant(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.PIERCING), piercing);
		return weapon;
	}

	/** Sample a random unit vector within a cone around 'axis' (uniform over spherical cap). */
	private static net.minecraft.world.phys.Vec3 randomDirInCone(net.minecraft.util.RandomSource rand, net.minecraft.world.phys.Vec3 axis, double maxAngleRad) {
		net.minecraft.world.phys.Vec3 a = axis.normalize();
		// Find a perpendicular basis (u, v)
		net.minecraft.world.phys.Vec3 w = Math.abs(a.y) < 0.99 ? new net.minecraft.world.phys.Vec3(0, 1, 0) : new net.minecraft.world.phys.Vec3(1, 0, 0);
		net.minecraft.world.phys.Vec3 u = a.cross(w).normalize();
		net.minecraft.world.phys.Vec3 v = a.cross(u).normalize();
		double cosMax = Math.cos(maxAngleRad);
		double cosAlpha = net.minecraft.util.Mth.lerp(rand.nextDouble(), cosMax, 1.0); // uniform cap
		double sinAlpha = Math.sqrt(Math.max(0.0, 1.0 - cosAlpha * cosAlpha));
		double theta = rand.nextDouble() * (Math.PI * 2);
		net.minecraft.world.phys.Vec3 ring = u.scale(Math.cos(theta)).add(v.scale(Math.sin(theta))).scale(sinAlpha);
		return a.scale(cosAlpha).add(ring).normalize();
	}
}