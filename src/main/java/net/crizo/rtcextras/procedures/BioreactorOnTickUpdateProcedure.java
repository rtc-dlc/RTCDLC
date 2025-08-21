package net.crizo.rtcextras.procedures;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import net.crizo.rtcextras.init.RtcExtrasModItems;

public class BioreactorOnTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (false) {
			if (world instanceof ServerLevel _level)
				_level.addFreshEntity(new ExperienceOrb(_level, x, y, z, 0));
		}
		if ((itemFromBlockInventory(world, BlockPos.containing(x, y, z), 0).copy()).getItem() == RtcExtrasModItems.BACTERIUM.get()) {// server-only
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
				// only care about bacterium items
				if (!(st.getItem() instanceof net.crizo.rtcextras.item.BacteriumItem))
					continue;
				// stabilize (your existing proc)
				StabilizeBacteriaProcedure.execute(world, x, y, z);
				// if this bacterium shouldn't live, stop here
				if (!BacteriumShouldLiveProcedure.execute(world, st)) {
					return;
				}
			}
			// do stuff here!!! like generating loot from RTC or whatever
		}
	}

	private static ItemStack itemFromBlockInventory(LevelAccessor world, BlockPos pos, int slot) {
		if (world instanceof ILevelExtension ext) {
			IItemHandler itemHandler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
			if (itemHandler != null)
				return itemHandler.getStackInSlot(slot);
		}
		return ItemStack.EMPTY;
	}
}