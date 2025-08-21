package net.crizo.rtcextras.procedures;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.server.level.ServerPlayer;

@EventBusSubscriber
public class ContainerOpenDecayHandlerProcedure {
	// Apply lazy decay to every slot when any container GUI opens
	@SubscribeEvent
	public static void onOpen(net.neoforged.neoforge.event.entity.player.PlayerContainerEvent.Open event) {
		if (!(event.getEntity() instanceof ServerPlayer sp))
			return;
		var menu = event.getContainer();
		if (menu == null)
			return;
		var level = sp.level();
		if (level == null || level.isClientSide())
			return;
		for (var slot : menu.slots) {
			var stack = slot.getItem();
			if (!stack.isEmpty() && stack.getItem() instanceof net.crizo.rtcextras.item.BacteriumItem) {
				//net.crizo.rtcextras.item.BacteriumItem.applyDecay(level, stack);
				slot.set(stack); // write back in case damage changed
			}
		}
	}
}