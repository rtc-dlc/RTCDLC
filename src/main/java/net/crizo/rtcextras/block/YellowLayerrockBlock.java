package net.crizo.rtcextras.block;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class YellowLayerrockBlock extends Block {
	public YellowLayerrockBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.ANCIENT_DEBRIS).strength(3f, 25f).requiresCorrectToolForDrops().instrument(NoteBlockInstrument.BASEDRUM));
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 15;
	}
}