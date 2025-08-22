// PerishableItemBase.java
package net.crizo.rtcextras;

import org.checkerframework.checker.units.qual.s;
import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.h;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;

import java.util.Properties;
import java.util.List;

public class PerishableItemBase extends Item {
	public PerishableItemBase(Properties props) {
		super(props);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
		if (!level.isClientSide() && PerishableStacks.isPerishable(stack)) {
			PerishableStacks.tick(level, entity, stack, () -> onExpire(level, entity, stack));
		}
		super.inventoryTick(stack, level, entity, slotId, selected);
	}

	/** Override in subclasses (e.g., bacterium loot drop) */
	protected void onExpire(Level level, Entity entity, ItemStack stack) {
		// default: nothing special
	}

	// --- Optional bar/tooltip for items that extend this base ---
	@Override
	public boolean isBarVisible(ItemStack stack) {
		return PerishableStacks.isPerishable(stack);
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		Level level = net.minecraft.client.Minecraft.getInstance().level;
		if (level == null)
			return 13;
		long left = PerishableStacks.timeLeftTicks(level, stack);
		long total = PerishableStacks.perItemTicks(stack) * Math.max(1, stack.getCount());
		float frac = total <= 0 ? 0f : (left / (float) total);
		return Math.round(Mth.clamp(frac, 0f, 1f) * 13f);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		Level level = net.minecraft.client.Minecraft.getInstance().level;
		if (level == null)
			return 0x00FF00;
		long left = PerishableStacks.timeLeftTicks(level, stack);
		long total = PerishableStacks.perItemTicks(stack) * Math.max(1, stack.getCount());
		float frac = total <= 0 ? 0f : (left / (float) total);
		float hue = 0.33f * Mth.clamp(frac, 0f, 1f);
		return Mth.hsvToRgb(hue, 1f, 1f);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
		if (ctx.level() != null && PerishableStacks.isPerishable(stack)) {
			long leftSec = PerishableStacks.timeLeftTicks(ctx.level(), stack) / 20L;
			tip.add(Component.literal("Perishable • " + prettyTime(leftSec)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x88FF88))));
		}
		super.appendHoverText(stack, ctx, tip, flag);
	}

	protected static String prettyTime(long s) {
		if (s < 60)
			return s + "s";
		long m = s / 60, sec = s % 60;
		if (m < 60)
			return m + "m " + sec + "s";
		long h = m / 60;
		m %= 60;
		return h + "h " + m + "m";
	}
}