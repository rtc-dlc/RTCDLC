package net.crizo.rtcextras.procedures;

import org.checkerframework.checker.units.qual.h;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;

public class SplicerShowGene2Procedure {
	public static String execute(LevelAccessor world, double x, double y, double z) {
		com.google.gson.JsonArray h = new com.google.gson.JsonArray();
		return getBlockNBTString(world, BlockPos.containing(x, y, z), "gene2");
	}

	private static String getBlockNBTString(LevelAccessor world, BlockPos pos, String tag) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity != null)
			return blockEntity.getPersistentData().getString(tag);
		return "";
	}
}