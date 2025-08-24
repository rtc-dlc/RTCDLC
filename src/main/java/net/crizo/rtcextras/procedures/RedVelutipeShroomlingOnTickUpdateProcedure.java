package net.crizo.rtcextras.procedures;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;

public class RedVelutipeShroomlingOnTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (world.canSeeSkyFromBelowWater(BlockPos.containing(x, y, z))) {
			if (!world.isClientSide()) {
				BlockPos _bp = BlockPos.containing(x, y, z);
				BlockEntity _blockEntity = world.getBlockEntity(_bp);
				BlockState _bs = world.getBlockState(_bp);
				if (_blockEntity != null)
					_blockEntity.getPersistentData().putDouble("time", (getBlockNBTNumber(world, BlockPos.containing(x, y, z), "time") + 1));
				if (world instanceof Level _level)
					_level.sendBlockUpdated(_bp, _bs, _bs, 3);
			}
			if (getBlockNBTNumber(world, BlockPos.containing(x, y, z), "time") >= 20) {
				if (Math.random() < 0.5) {
					if (world instanceof ServerLevel _level)
						_level.holderOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.parse("rtc_extras:feature_red_velutipe_mushroom"))).value().place(_level, _level.getChunkSource().getGenerator(), _level.getRandom(),
								BlockPos.containing(x, y, z));
				} else {
					if (world instanceof ServerLevel _level)
						_level.holderOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.parse("rtc_extras:feature_2_red_velutipe_mushroom"))).value().place(_level, _level.getChunkSource().getGenerator(), _level.getRandom(),
								BlockPos.containing(x, y, z));
				}
			}
		}
	}

	private static double getBlockNBTNumber(LevelAccessor world, BlockPos pos, String tag) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity != null)
			return blockEntity.getPersistentData().getDouble(tag);
		return -1;
	}
}