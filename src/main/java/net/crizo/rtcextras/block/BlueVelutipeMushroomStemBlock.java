package net.crizo.rtcextras.block;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BlueVelutipeMushroomStemBlock extends Block {
	public BlueVelutipeMushroomStemBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1.55f, 7.5f));
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 15;
	}
}