package net.crizo.rtcextras.procedures;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;

public class CultivatorButtonCountProcedure {
	public static String execute(LevelAccessor world, double x, double y, double z) {
		return Math.round(Math.pow(10, 3) * (getBlockNBTNumber(world, BlockPos.containing(x, y, z), "bact_count") / 64000)) / Math.pow(10, 3) + " Colonies";
	}

	private static double getBlockNBTNumber(LevelAccessor world, BlockPos pos, String tag) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity != null)
			return blockEntity.getPersistentData().getDouble(tag);
		return -1;
	}
}