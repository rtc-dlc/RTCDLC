package net.crizo.rtcextras.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class GreenVelutideStairsBlock extends StairBlock {
	public GreenVelutideStairsBlock() {
		super(Blocks.AIR.defaultBlockState(), BlockBehaviour.Properties.of().sound(SoundType.FUNGUS).strength(3f, 2f).dynamicShape().ignitedByLava());
	}

	@Override
	public float getExplosionResistance() {
		return 2f;
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 0;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 5;
	}
}