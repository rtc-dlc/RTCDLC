package net.crizo.rtcextras.procedures;

public class SporeRightclickedOnBlockProcedure {
	public static void execute(net.minecraft.world.level.LevelAccessor world, double x, double y, double z, net.minecraft.world.item.ItemStack itemstack) {
		if (itemstack == null || itemstack.isEmpty())
			return;
		final net.minecraft.core.BlockPos basePos = net.minecraft.core.BlockPos.containing(x, y, z);
		final net.minecraft.core.BlockPos placePos = basePos.above();
		// 1) Must click the top of a block in rtc_extras:innoculable and have a sturdy top face
		final net.minecraft.world.level.block.state.BlockState belowState = world.getBlockState(basePos);
		final boolean sturdyTop = belowState.isFaceSturdy(world, basePos, net.minecraft.core.Direction.UP);
		final boolean validBlock = belowState.is(net.minecraft.tags.BlockTags.create(net.minecraft.resources.ResourceLocation.parse("rtc_extras:innoculable")));
		// 2) Space above must be air or replaceable
		final net.minecraft.world.level.block.state.BlockState aboveState = world.getBlockState(placePos);
		final boolean spaceClear = world.isEmptyBlock(placePos) || aboveState.canBeReplaced();
		if (!sturdyTop || !validBlock || !spaceClear)
			return;
		// 3) Determine which shroomling to place based on spore color
		net.minecraft.world.level.block.state.BlockState toPlace = null;
		if (itemstack.getItem() == net.crizo.rtcextras.init.RtcExtrasModItems.BROWN_SPORE.get()) {
			toPlace = net.crizo.rtcextras.init.RtcExtrasModBlocks.BROWN_VELUTIPE_SHROOMLING.get().defaultBlockState();
		} else if (itemstack.getItem() == net.crizo.rtcextras.init.RtcExtrasModItems.RED_SPORE.get()) {
			toPlace = net.crizo.rtcextras.init.RtcExtrasModBlocks.RED_VELUTIPE_SHROOMLING.get().defaultBlockState();
		} else if (itemstack.getItem() == net.crizo.rtcextras.init.RtcExtrasModItems.MAGENTA_SPORE.get()) {
			toPlace = net.crizo.rtcextras.init.RtcExtrasModBlocks.MAGENTA_VELUTIPE_SHROOMLING.get().defaultBlockState();
		}
		if (toPlace == null)
			return;
		// 4) Final survival check (light level/valid floor/etc. if your block defines it)
		if (!toPlace.canSurvive(world, placePos))
			return;
		// 5) Place and consume one spore
		world.setBlock(placePos, toPlace, 3);
		itemstack.shrink(1);
	}
}