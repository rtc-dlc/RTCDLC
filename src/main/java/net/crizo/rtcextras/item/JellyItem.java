package net.crizo.rtcextras.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;

import net.crizo.rtcextras.procedures.JellyPlayerFinishesUsingItemProcedure;
import net.crizo.rtcextras.PerishableItemBase;

public class JellyItem extends PerishableItemBase {
	public JellyItem() {
		super(new Item.Properties().food((new FoodProperties.Builder()).nutrition(3).saturationModifier(0.6f).alwaysEdible().build()));
	}

	@Override
	public int getUseDuration(ItemStack itemstack, LivingEntity livingEntity) {
		return 20;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		ItemStack retval = super.finishUsingItem(itemstack, world, entity);
		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		JellyPlayerFinishesUsingItemProcedure.execute(world, x, y, z, entity);
		return retval;
	}
}