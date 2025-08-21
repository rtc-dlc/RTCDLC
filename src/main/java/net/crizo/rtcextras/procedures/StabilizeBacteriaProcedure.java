package net.crizo.rtcextras.procedures;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;

public class StabilizeBacteriaProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		// server-only
		if (world.isClientSide())
			return;
		if (!(world instanceof ILevelExtension ext))
			return;
		IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.containing(x, y, z), null);
		if (handler == null)
			return;
		int slots = handler.getSlots();
		for (int slot = 0; slot < slots; slot++) {
			ItemStack st = handler.getStackInSlot(slot);
			if (st.isEmpty())
				continue;
			// OPTIONAL: only stabilize your bacterium item
			if (st.getItem() instanceof net.crizo.rtcextras.item.BacteriumItem) {
				var tag = st.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
				// === Nudge created forward by 1 tick per server tick ===
				long created = tag.getLong("rtc_created");
				tag.putLong("rtc_created", created + 1);
				// optional: mark stabilized
				tag.putBoolean("rtc_stabilized", true);
				st.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
				if (handler instanceof IItemHandlerModifiable mod) {
					mod.setStackInSlot(slot, st);
				} else {
					int count = st.getCount();
					ItemStack extracted = handler.extractItem(slot, count, false);
					ItemStack toInsert = st.copy();
					toInsert.setCount(count);
					handler.insertItem(slot, toInsert, false);
				}
			}
		}
	}
}