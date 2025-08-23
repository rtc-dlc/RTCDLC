package net.crizo.rtcextras.block;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class MagentaVelutipeMushroomBlock extends Block {
	public MagentaVelutipeMushroomBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.WET_SPONGE).strength(0.7f, 7.5f));
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 15;
	}
}