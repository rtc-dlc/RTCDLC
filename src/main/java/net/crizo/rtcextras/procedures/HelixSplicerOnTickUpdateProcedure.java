package net.crizo.rtcextras.procedures;

import org.checkerframework.checker.units.qual.s;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.common.extensions.ILevelExtension;
import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.BlockPos;

import net.crizo.rtcextras.init.RtcExtrasModItems;

public class HelixSplicerOnTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		BlockPos pos = BlockPos.containing(x, y, z);
		// --- status text countdown (transient UI) ---
		int statusTicks = (int) getBlockNBTNumber(world, pos, "splice_status_ticks");
		if (statusTicks > 0) {
			setBlockNBTNumber(world, pos, "splice_status_ticks", statusTicks - 1);
			if (statusTicks - 1 <= 0)
				setBlockNBTString(world, pos, "splice_status_text", "");
		}
		// --- read inputs ---
		ItemStack s0 = itemFromBlockInventory(world, pos, 0).copy();
		ItemStack s1 = itemFromBlockInventory(world, pos, 1).copy();
		ItemStack s2 = itemFromBlockInventory(world, pos, 2).copy();
		ItemStack sOut = itemFromBlockInventory(world, pos, 3).copy();
		boolean isGene0 = s0.getItem() == RtcExtrasModItems.GENE_SAMPLE.get();
		boolean isGene1 = s1.getItem() == RtcExtrasModItems.GENE_SAMPLE.get();
		boolean isGene2 = s2.getItem() == RtcExtrasModItems.GENE_SAMPLE.get();
		int inputCount = (isGene0 ? 1 : 0) + (isGene1 ? 1 : 0) + (isGene2 ? 1 : 0);
		boolean outEmpty = sOut.isEmpty() || sOut.getItem() == Blocks.AIR.asItem();
		// --- extract tags (safe) ---
		CompoundTag t0 = isGene0 ? s0.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() : new CompoundTag();
		CompoundTag t1 = isGene1 ? s1.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() : new CompoundTag();
		CompoundTag t2 = isGene2 ? s2.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag() : new CompoundTag();
		double i0 = t0.contains("rtc_gene_interference") ? t0.getDouble("rtc_gene_interference") : 0.0;
		double i1 = t1.contains("rtc_gene_interference") ? t1.getDouble("rtc_gene_interference") : 0.0;
		double i2 = t2.contains("rtc_gene_interference") ? t2.getDouble("rtc_gene_interference") : 0.0;
		String id0 = t0.getString("rtc_gene_id");
		String id1 = t1.getString("rtc_gene_id");
		String id2 = t2.getString("rtc_gene_id");
		String name0 = t0.getString("rtc_gene_name");
		String name1 = t1.getString("rtc_gene_name");
		String name2 = t2.getString("rtc_gene_name");
		String tier0 = t0.getString("rtc_gene_tier");
		String tier1 = t1.getString("rtc_gene_tier");
		String tier2 = t2.getString("rtc_gene_tier");
		boolean composite0 = isGene0 && t0.getBoolean("composite");
		boolean composite1 = isGene1 && t1.getBoolean("composite");
		boolean composite2 = isGene2 && t2.getBoolean("composite");
		boolean compositeInput = composite0 || composite1 || composite2;
		// --- preview + HUD values ---
		double totInterf = clamp01(i0 + i1 + i2);
		double maxInterf = Math.max(i0, Math.max(i1, i2));
		setBlockNBTNumber(world, pos, "interf1", i0);
		setBlockNBTNumber(world, pos, "interf2", i1);
		setBlockNBTNumber(world, pos, "interf3", i2);
		setBlockNBTNumber(world, pos, "totInterf", totInterf);
		setBlockNBTNumber(world, pos, "chance", Math.round(totInterf * 100.0)); // failure %
		setBlockNBTString(world, pos, "gene1", id0);
		setBlockNBTString(world, pos, "gene2", id1);
		setBlockNBTString(world, pos, "gene3", id2);
		String comboPretty = joinNonEmpty(" + ", name0, name1, name2);
		setBlockNBTString(world, pos, "comboString", comboPretty);
		// --- timer semantics ---
		int spliceTimer = (int) Math.round(getBlockNBTNumber(world, pos, "splice")); // -1 idle, >=0 ticking
		// precondition message (also used during countdown)
		String warn;
		if (compositeInput)
			warn = "Composite input not allowed";
		else if (!outEmpty)
			warn = "Output slot occupied";
		else if (inputCount == 0)
			warn = "Insert gene samples";
		else
			warn = (spliceTimer >= 0) ? ("Splicing... " + spliceTimer + "t") : "Ready";
		setBlockNBTString(world, pos, "warning", warn);
		// --- if idle, nothing to do ---
		if (spliceTimer < 0)
			return;
		// --- ticking: only decrement when ready; pause if not ready ---
		boolean readyToWork = (inputCount > 0) && outEmpty && !compositeInput;
		if (!readyToWork) {
			// paused; keep timer value as-is
			return;
		}
		// decrement
		int newTimer = spliceTimer - 1;
		setBlockNBTNumber(world, pos, "splice", newTimer);
		setBlockNBTString(world, pos, "warning", "Splicing... " + newTimer + "t");
		// if still > 0, wait
		if (newTimer > 0)
			return;
		// --- time to execute the splice ---
		RandomSource rng = (world instanceof Level l) ? l.getRandom() : RandomSource.create();
		boolean primaryFail = rng.nextDouble() < totInterf;
		int inertIndex = indexOfMax(i0, i1, i2);
		if (primaryFail) {
			boolean catastrophic = rng.nextDouble() < maxInterf; // second roll
			if (catastrophic) {
				// Junk DNA
				ItemStack junk = new ItemStack(RtcExtrasModItems.GENE_SAMPLE.get());
				CompoundTag j = new CompoundTag();
				j.putString("rtc_gene_id", "junk_dna");
				j.putString("rtc_gene_name", "Junk DNA");
				j.putString("rtc_gene_tier", "common");
				j.putString("rtc_gene_effect", "Noncoding fragment. Inert.");
				j.putFloat("rtc_gene_interference", 0.0F);
				j.putBoolean("composite", true);
				junk.set(DataComponents.CUSTOM_DATA, CustomData.of(j));
				if (setOutputAndConsumeInputs(world, pos, junk)) {
					setBlockNBTString(world, pos, "splice_status_text", "FAILED (Junk DNA)");
					setBlockNBTNumber(world, pos, "splice_status_ticks", 80);
				}
				setBlockNBTNumber(world, pos, "splice", -1); // back to idle
				return;
			} else {
				// Partial fail: remove the highest-interference gene
				ItemStack composite = makeCompositeSample(id0, id1, id2, name0, name1, name2, tier0, tier1, tier2, i0, i1, i2, inertIndex, true);
				if (setOutputAndConsumeInputs(world, pos, composite)) {
					setBlockNBTString(world, pos, "splice_status_text", "PARTIAL FAIL");
					setBlockNBTNumber(world, pos, "splice_status_ticks", 80);
				}
				setBlockNBTNumber(world, pos, "splice", -1);
				return;
			}
		}
		// Success: keep all genes
		ItemStack composite = makeCompositeSample(id0, id1, id2, name0, name1, name2, tier0, tier1, tier2, i0, i1, i2, -1, false);
		if (setOutputAndConsumeInputs(world, pos, composite)) {
			setBlockNBTString(world, pos, "splice_status_text", "SUCCESS");
			setBlockNBTNumber(world, pos, "splice_status_ticks", 60);
		}
		setBlockNBTNumber(world, pos, "splice", -1); // back to idle
	}

	// ===== Helpers (unchanged except where noted) =====
	private static ItemStack makeCompositeSample(String id0, String id1, String id2, String name0, String name1, String name2, String tier0, String tier1, String tier2, double i0, double i1, double i2, int inertIndex, boolean markFailed) {
		String[] ids = new String[]{id0, id1, id2};
		String[] names = new String[]{name0, name1, name2};
		String[] tiers = new String[]{tier0, tier1, tier2};
		double[] inter = new double[]{i0, i1, i2};
		boolean[] include = new boolean[]{true, true, true};
		if (markFailed && inertIndex >= 0 && inertIndex < 3)
			include[inertIndex] = false;
		StringBuilder idSB = new StringBuilder();
		StringBuilder nameSB = new StringBuilder();
		double sumInterf = 0.0;
		boolean hasRare = false, hasExotic = false;
		int survivors = 0;
		for (int k = 0; k < 3; k++) {
			if (!include[k])
				continue;
			String id = ids[k];
			String nm = names[k];
			String tr = tiers[k];
			double ii = inter[k];
			if (id != null && !id.isEmpty()) {
				if (idSB.length() > 0)
					idSB.append("+");
				idSB.append(id);
			}
			if (nm != null && !nm.isEmpty()) {
				if (nameSB.length() > 0)
					nameSB.append(" + ");
				nameSB.append(nm);
			}
			if ("exotic".equalsIgnoreCase(tr))
				hasExotic = true;
			else if ("rare".equalsIgnoreCase(tr))
				hasRare = true;
			sumInterf += ii;
			survivors++;
		}
		if (survivors == 0) {
			ItemStack junk = new ItemStack(RtcExtrasModItems.GENE_SAMPLE.get());
			CompoundTag j = new CompoundTag();
			j.putString("rtc_gene_id", "junk_dna");
			j.putString("rtc_gene_name", "Junk DNA");
			j.putString("rtc_gene_tier", "common");
			j.putString("rtc_gene_effect", "Noncoding fragment. Inert.");
			j.putFloat("rtc_gene_interference", 0.0F);
			j.putBoolean("composite", true);
			if (markFailed) {
				j.putBoolean("rtc_composite_partial_fail", true);
				j.putInt("rtc_inert_index", inertIndex);
				j.putString("rtc_inert_gene_id", inertIndex == 0 ? id0 : inertIndex == 1 ? id1 : inertIndex == 2 ? id2 : "");
			}
			junk.set(DataComponents.CUSTOM_DATA, CustomData.of(j));
			return junk;
		}
		String outTier = hasExotic ? "exotic" : (hasRare ? "rare" : "common");
		ItemStack out = new ItemStack(RtcExtrasModItems.GENE_SAMPLE.get());
		CompoundTag tag = new CompoundTag();
		String comboIds = idSB.toString();
		String comboNames = nameSB.toString();
		double finalInterf = clamp01(sumInterf);
		tag.putString("rtc_gene_id", comboIds.isEmpty() ? "composite" : "composite(" + comboIds + ")");
		tag.putString("rtc_gene_name", "Composite Genome");
		tag.putString("rtc_gene_tier", outTier);
		tag.putString("rtc_gene_effect", comboNames.isEmpty() ? "Merged traits." : ("Combined: " + comboNames));
		tag.putFloat("rtc_gene_interference", (float) finalInterf);
		tag.putString("rtc_gene_combo", comboNames);
		tag.putString("rtc_gene_ids_combo", comboIds);
		tag.putInt("rtc_gene_survivors", survivors);
		if (markFailed) {
			tag.putBoolean("rtc_composite_partial_fail", true);
			tag.putInt("rtc_inert_index", Math.max(-1, inertIndex));
			String inertId = inertIndex == 0 ? id0 : inertIndex == 1 ? id1 : inertIndex == 2 ? id2 : "";
			tag.putString("rtc_inert_gene_id", inertId);
			tag.putInt("rtc_removed_gene_count", 1);
		}
		tag.putBoolean("composite", true);
		out.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return out;
	}

	private static int indexOfMax(double a, double b, double c) {
		if (a >= b && a >= c)
			return 0;
		if (b >= a && b >= c)
			return 1;
		return 2;
	}

	private static double clamp01(double v) {
		return v < 0 ? 0 : (v > 1 ? 1 : v);
	}

	private static void previewClear(LevelAccessor world, BlockPos pos) {
		setBlockNBTNumber(world, pos, "interf1", 0);
		setBlockNBTNumber(world, pos, "interf2", 0);
		setBlockNBTNumber(world, pos, "interf3", 0);
		setBlockNBTNumber(world, pos, "totInterf", 0);
		setBlockNBTString(world, pos, "gene1", "");
		setBlockNBTString(world, pos, "gene2", "");
		setBlockNBTString(world, pos, "gene3", "");
		setBlockNBTString(world, pos, "comboString", "");
	}

	private static boolean setOutputAndConsumeInputs(LevelAccessor world, BlockPos pos, ItemStack result) {
		if (!(world instanceof ILevelExtension ext))
			return false;
		IItemHandler handler = ext.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
		if (!(handler instanceof IItemHandlerModifiable mod))
			return false;
		ItemStack out = handler.getStackInSlot(3);
		if (!out.isEmpty() && out.getItem() != Blocks.AIR.asItem())
			return false;
		mod.setStackInSlot(3, result);
		for (int i = 0; i <= 2; i++) {
			ItemStack s = handler.getStackInSlot(i);
			if (s.getItem() == RtcExtrasModItems.GENE_SAMPLE.get())
				mod.setStackInSlot(i, ItemStack.EMPTY);
		}
		if (world instanceof Level lvl) {
			BlockState bs = lvl.getBlockState(pos);
			lvl.sendBlockUpdated(pos, bs, bs, 3);
		}
		return true;
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
		BlockEntity be = world.getBlockEntity(pos);
		if (be != null)
			return be.getPersistentData().getDouble(tag);
		return -1; // default to -1 (idle) if missing
	}

	private static void setBlockNBTNumber(LevelAccessor world, BlockPos pos, String tag, double val) {
		if (world.isClientSide())
			return;
		BlockEntity be = world.getBlockEntity(pos);
		BlockState bs = world.getBlockState(pos);
		if (be != null) {
			be.getPersistentData().putDouble(tag, val);
			if (world instanceof Level lvl)
				lvl.sendBlockUpdated(pos, bs, bs, 3);
		}
	}

	private static void setBlockNBTString(LevelAccessor world, BlockPos pos, String tag, String val) {
		if (world.isClientSide())
			return;
		BlockEntity be = world.getBlockEntity(pos);
		BlockState bs = world.getBlockState(pos);
		if (be != null) {
			be.getPersistentData().putString(tag, val);
			if (world instanceof Level lvl)
				lvl.sendBlockUpdated(pos, bs, bs, 3);
		}
	}

	private static String joinNonEmpty(String sep, String a, String b, String c) {
		StringBuilder sb = new StringBuilder();
		if (a != null && !a.isEmpty())
			sb.append(a);
		if (b != null && !b.isEmpty()) {
			if (sb.length() > 0)
				sb.append(sep);
			sb.append(b);
		}
		if (c != null && !c.isEmpty()) {
			if (sb.length() > 0)
				sb.append(sep);
			sb.append(c);
		}
		return sb.toString();
	}
}