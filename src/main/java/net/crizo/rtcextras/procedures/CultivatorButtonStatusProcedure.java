package net.crizo.rtcextras.procedures;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;

public class CultivatorButtonStatusProcedure {
	public static String execute(LevelAccessor world, double x, double y, double z) {
		return getBlockNBTString(world, BlockPos.containing(x, y, z), "bact_status");
	}

	private static String getBlockNBTString(LevelAccessor world, BlockPos pos, String tag) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity != null)
			return blockEntity.getPersistentData().getString(tag);
		return "";
	}
}