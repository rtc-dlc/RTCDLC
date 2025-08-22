package net.crizo.rtcextras;

import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;

@EventBusSubscriber(modid = "rtc_extras")
public final class PerishablePickupMerge {
	/** Only fold the *incoming* ground stack; never compact the whole inv. */
	@SubscribeEvent
	public static void onPickupPre(ItemEntityPickupEvent.Pre e) {
		final Player player = e.getPlayer();
		if (player == null)
			return;
		final Level level = player.level();
		if (level.isClientSide())
			return; // server only
		final ItemEntity itemEnt = e.getItemEntity();
		// Skip freshly-dropped items so we don't "restack on drop"
		if (itemEnt.hasPickUpDelay())
			return; // 1.21 method
		if (itemEnt.tickCount < 8)
			return; // extra safety
		final ItemStack incoming = itemEnt.getItem();
		if (incoming.isEmpty() || !PerishableMerge.isPerishable(incoming))
			return;
		foldIncomingIntoInventory(level, player, incoming);
		// don't cancel; vanilla will pick up the remainder
	}

	/** Cursor→slot stacking, perishable-aware. Cancel ONLY on the server. */
	@SubscribeEvent
	public static void onStackedOnOther(ItemStackedOnOtherEvent e) {
		final Player player = e.getPlayer();
		if (player == null)
			return;
		final Level level = player.level();
		final ItemStack cursor = e.getCarriedItem(); // mouse
		final ItemStack target = e.getStackedOnItem(); // slot
		// cheap guards
		if (cursor.isEmpty() || target.isEmpty())
			return;
		if (!PerishableMerge.isPerishable(cursor) || !PerishableMerge.isPerishable(target))
			return;
		if (!PerishableMerge.stacksMergeable(target, cursor))
			return;
		if (!level.isClientSide()) {
			// SERVER: do merge, cancel vanilla
			e.setCanceled(true);
			final boolean rightClickOne = e.getClickAction() == net.minecraft.world.inventory.ClickAction.SECONDARY;
			if (rightClickOne && cursor.getCount() > 1) {
				ItemStack one = cursor.copy();
				one.setCount(1);
				PerishableMerge.mergeIntoTarget(level, target, one);
				if (one.isEmpty())
					cursor.shrink(1);
			} else {
				PerishableMerge.mergeIntoTarget(level, target, cursor);
			}
			// write back & sync
			e.getSlot().set(target);
			e.getCarriedSlotAccess().set(cursor.copy());
			player.containerMenu.broadcastChanges();
			return;
		}
		// CLIENT: do NOT cancel; let the click go to the server.
	}

	// --- unchanged helper ---
	private static void foldIncomingIntoInventory(Level level, Player player, ItemStack incoming) {
		var inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize() && !incoming.isEmpty(); i++) {
			final ItemStack target = inv.getItem(i);
			if (target.isEmpty())
				continue;
			if (!PerishableMerge.isPerishable(target))
				continue;
			if (!PerishableMerge.stacksMergeable(target, incoming))
				continue;
			PerishableMerge.mergeIntoTarget(level, target, incoming);
		}
	}

	private PerishablePickupMerge() {
	}
}