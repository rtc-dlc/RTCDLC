package net.crizo.rtcextras.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;

public class MagentaVelutipeMushroomBlock extends Block {
	public MagentaVelutipeMushroomBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.WET_SPONGE).strength(0.7f, 7.5f));
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 15;
	}
}