package net.crizo.rtcextras;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;

@EventBusSubscriber
public final class PerishableEvents {
	private PerishableEvents() {
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post e) {
		Player p = e.getEntity();
		if (p.level().isClientSide())
			return;
		// Main inventory
		for (ItemStack st : p.getInventory().items)
			tickIfPerishable(p, st);
		// Armor/offhand
		for (ItemStack st : p.getInventory().armor)
			tickIfPerishable(p, st);
		tickIfPerishable(p, p.getOffhandItem());
	}

	private static void tickIfPerishable(Player p, ItemStack st) {
		if (PerishableStacks.isPerishable(st)) {
			PerishableStacks.tick(p.level(), p, st, () -> {
				// default: nothing extra on expire (stack will be zeroed)
			});
		}
	}

	// Add tooltip for *non-subclassed* perishable items (vanilla, etc.)
	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent e) {
		var level = e.getEntity() == null ? null : e.getEntity().level();
		ItemStack st = e.getItemStack();
		if (level != null && PerishableStacks.isPerishable(st) && !(st.getItem() instanceof PerishableItemBase)) {
			long leftSec = PerishableStacks.timeLeftTicks(level, st) / 20L;
			e.getToolTip().add(net.minecraft.network.chat.Component.literal("Perishable • " + leftSec + "s"));
		}
	}
}