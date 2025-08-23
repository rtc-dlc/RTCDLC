package net.crizo.rtcextras.procedures;

import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;

import net.crizo.rtcextras.init.RtcExtrasModBlocks;

import javax.annotation.Nullable;

@EventBusSubscriber
public class ProShroomExplodeProcedure {
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		execute(event, event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getState());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, BlockState blockstate) {
		execute(null, world, x, y, z, blockstate);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, BlockState blockstate) {
		if (blockstate.getBlock() == RtcExtrasModBlocks.BROWN_VELUTIPE_MUSHROOM.get()) {
			if (Math.random() < 0.33) {
				for (int index0 = 0; index0 < (int) Mth.nextDouble(RandomSource.create(), 0, 3); index0++) {
					if (world instanceof ServerLevel projectileLevel) {
						Projectile _entityToSpawn = new Snowball(EntityType.SNOWBALL, projectileLevel);
						_entityToSpawn.setPos(x, y, z);
						_entityToSpawn.shoot((Mth.nextDouble(RandomSource.create(), -1, 1)), (Mth.nextDouble(RandomSource.create(), -1, 1)), (Mth.nextDouble(RandomSource.create(), -1, 1)), (float) 0.5, 0);
						projectileLevel.addFreshEntity(_entityToSpawn);
					}
				}
			}
		} else if (blockstate.getBlock() == RtcExtrasModBlocks.RED_VELUTIPE_MUSHROOM.get()) {
			if (Math.random() < 0.33) {
				for (int index1 = 0; index1 < (int) Mth.nextDouble(RandomSource.create(), 0, 3); index1++) {
					if (world instanceof ServerLevel projectileLevel) {
						Projectile _entityToSpawn = new Snowball(EntityType.SNOWBALL, projectileLevel);
						_entityToSpawn.setPos(x, y, z);
						_entityToSpawn.shoot((Mth.nextDouble(RandomSource.create(), -1, 1)), (Mth.nextDouble(RandomSource.create(), -1, 1)), (Mth.nextDouble(RandomSource.create(), -1, 1)), (float) 0.5, 0);
						projectileLevel.addFreshEntity(_entityToSpawn);
					}
				}
			}
		} else if (blockstate.getBlock() == RtcExtrasModBlocks.MAGENTA_VELUTIPE_MUSHROOM.get()) {
			if (Math.random() < 0.33) {
				for (int index2 = 0; index2 < (int) Mth.nextDouble(RandomSource.create(), 0, 3); index2++) {
					if (world instanceof ServerLevel projectileLevel) {
						Projectile _entityToSpawn = new Snowball(EntityType.SNOWBALL, projectileLevel);
						_entityToSpawn.setPos(x, y, z);
						_entityToSpawn.shoot((Mth.nextDouble(RandomSource.create(), -1, 1)), (Mth.nextDouble(RandomSource.create(), -1, 1)), (Mth.nextDouble(RandomSource.create(), -1, 1)), (float) 0.5, 0);
						projectileLevel.addFreshEntity(_entityToSpawn);
					}
				}
			}
		}
	}
}