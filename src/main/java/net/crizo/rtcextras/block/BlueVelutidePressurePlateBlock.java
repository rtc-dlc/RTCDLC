package net.crizo.rtcextras.block;

import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

public class BlueVelutidePressurePlateBlock extends PressurePlateBlock {
	public BlueVelutidePressurePlateBlock() {
		super(BlockSetType.OAK, BlockBehaviour.Properties.of().sound(SoundType.FUNGUS).strength(2f, 3f).pushReaction(PushReaction.DESTROY).dynamicShape().ignitedByLava().forceSolidOn());
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