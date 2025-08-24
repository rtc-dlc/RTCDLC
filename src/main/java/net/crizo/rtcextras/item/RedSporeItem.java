package net.crizo.rtcextras.item;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResult;

import net.crizo.rtcextras.procedures.SporeRightclickedOnBlockProcedure;

public class RedSporeItem extends Item {
	public RedSporeItem() {
		super(new Item.Properties());
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		super.useOn(context);
		SporeRightclickedOnBlockProcedure.execute(context.getLevel(), context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ(), context.getItemInHand());
		return InteractionResult.SUCCESS;
	}
}