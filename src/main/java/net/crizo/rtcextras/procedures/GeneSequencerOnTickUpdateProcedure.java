package net.crizo.rtcextras.procedures;

import net.neoforged.bus.api.Event;

public class GeneSequencerOnTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (false) {
			if (world instanceof ServerLevel _level)
				_level.addFreshEntity(new ExperienceOrb(_level, x, y, z, 0));
		}
		if ((itemFromBlockInventory(world, BlockPos.containing(x, y, z), 0).copy()).getItem() == RtcExtrasModItems.DELETED_MOD_ELEMENT.get() && itemFromBlockInventory(world, BlockPos.containing(x, y, z), 0).getCount() == 64
				&& (itemFromBlockInventory(world, BlockPos.containing(x, y, z), 1).copy()).getItem() == RtcExtrasModItems.DELETED_MOD_ELEMENT.get()
				&& ((itemFromBlockInventory(world, BlockPos.containing(x, y, z), 1).copy()).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("rtc_gene_id")).equals("")) {// server-only
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
			if (!world.isClientSide()) {
				BlockPos _bp = BlockPos.containing(x, y, z);
				BlockEntity _blockEntity = world.getBlockEntity(_bp);
				BlockState _bs = world.getBlockState(_bp);
				if (_blockEntity != null)
					_blockEntity.getPersistentData().putDouble("timer", (getBlockNBTNumber(world, BlockPos.containing(x, y, z), "timer") + 1));
				if (world instanceof Level _level)
					_level.sendBlockUpdated(_bp, _bs, _bs, 3);
			}
			if (getBlockNBTNumber(world, BlockPos.containing(x, y, z), "timer") >= 200) {
				AnalyzeGeneSampleProcedure.execute(world, x, y, z);
				if (world instanceof ILevelExtension _ext && _ext.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.containing(x, y, z), null) instanceof IItemHandlerModifiable _itemHandlerModifiable) {
					int _slotid = 0;
					ItemStack _stk = _itemHandlerModifiable.getStackInSlot(_slotid).copy();
					_stk.shrink(64);
					_itemHandlerModifiable.setStackInSlot(_slotid, _stk);
				}
				if (world instanceof ILevelExtension _ext && _ext.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.containing(x, y, z), null) instanceof IItemHandlerModifiable _itemHandlerModifiable) {
					int _slotid = 1;
					ItemStack _stk = _itemHandlerModifiable.getStackInSlot(_slotid).copy();
					_stk.shrink(64);
					_itemHandlerModifiable.setStackInSlot(_slotid, _stk);
				}
				if (!world.isClientSide()) {
					BlockPos _bp = BlockPos.containing(x, y, z);
					BlockEntity _blockEntity = world.getBlockEntity(_bp);
					BlockState _bs = world.getBlockState(_bp);
					if (_blockEntity != null)
						_blockEntity.getPersistentData().putDouble("timer", 0);
					if (world instanceof Level _level)
						_level.sendBlockUpdated(_bp, _bs, _bs, 3);
				}
			}
		} else {
			if (!world.isClientSide()) {
				BlockPos _bp = BlockPos.containing(x, y, z);
				BlockEntity _blockEntity = world.getBlockEntity(_bp);
				BlockState _bs = world.getBlockState(_bp);
				if (_blockEntity != null)
					_blockEntity.getPersistentData().putDouble("timer", 0);
				if (world instanceof Level _level)
					_level.sendBlockUpdated(_bp, _bs, _bs, 3);
			}
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

	private static double getBlockNBTNumber(LevelAccessor world, BlockPos pos, String tag) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity != null)
			return blockEntity.getPersistentData().getDouble(tag);
		return -1;
	}
}