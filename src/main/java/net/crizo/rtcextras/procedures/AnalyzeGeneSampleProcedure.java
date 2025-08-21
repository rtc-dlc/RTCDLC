package net.crizo.rtcextras.procedures;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;

public class AnalyzeGeneSampleProcedure {
	// Slots: 0 = bacterium input, 1 = empty gene vial, 2 = filled output
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if (!(world instanceof ILevelExtension ext))
			return;
		IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, BlockPos.containing(x, y, z), null);
		if (handler == null)
			return;
		ItemStack bacterium = handler.getStackInSlot(0);
		if (bacterium.isEmpty() || !(bacterium.getItem() instanceof net.crizo.rtcextras.item.BacteriumItem))
			return;
		// Must have a gene on the bacterium
		CompoundTag bTag = bacterium.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!bTag.contains("rtc_gene_id"))
			return;
		// Slot 1 must be the empty Gene Sample item, and truly "empty" (no gene NBT)
		ItemStack vial = handler.getStackInSlot(1);
		if (vial.isEmpty() || !(vial.getItem() instanceof net.crizo.rtcextras.item.GeneSampleItem))
			return;
		CompoundTag vTag = vial.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (vTag.contains("rtc_gene_id"))
			return; // not empty → reject
		// Slot 2 must be empty for output (simple variant)
		ItemStack out = handler.getStackInSlot(2);
		if (!out.isEmpty())
			return;
		// Build the filled sample (copy gene fields from bacterium)
		ItemStack sample = new ItemStack(net.crizo.rtcextras.init.RtcExtrasModItems.GENE_SAMPLE.get());
		CompoundTag sTag = new CompoundTag();
		sTag.putString("rtc_gene_id", bTag.getString("rtc_gene_id"));
		sTag.putString("rtc_gene_name", bTag.getString("rtc_gene_name"));
		sTag.putString("rtc_gene_tier", bTag.getString("rtc_gene_tier"));
		sTag.putFloat("rtc_gene_interference", bTag.getFloat("rtc_gene_interference"));
		sTag.putString("rtc_gene_effect", bTag.getString("rtc_gene_effect"));
		// Optional provenance/debug
		sTag.putInt("rtc_sampled_count", bacterium.getCount());
		if (world instanceof Level lvl) {
			sTag.putLong("rtc_sampled_at", lvl.getGameTime());
		}
		sample.set(DataComponents.CUSTOM_DATA, CustomData.of(sTag));
		// Place output in slot 2 (no input consumption here; you can add it externally)
		if (handler instanceof IItemHandlerModifiable mod) {
			mod.setStackInSlot(2, sample);
		} else {
			handler.insertItem(2, sample, false);
		}
	}
}