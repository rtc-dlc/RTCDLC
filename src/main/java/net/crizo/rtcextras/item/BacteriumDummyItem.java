package net.crizo.rtcextras.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import java.util.Properties;

public class BacteriumDummyItem extends Item {
	public BacteriumDummyItem() {
		super(new Properties().stacksTo(64));
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (level.isClientSide() || !(entity instanceof Player p)) {
			super.inventoryTick(stack, level, entity, slot, selected);
			return;
		}
		if (stack.isEmpty()) {
			super.inventoryTick(stack, level, entity, slot, selected);
			return;
		}
		// convert a few per tick to avoid bursts
		int convert = Math.min(4, stack.getCount());
		for (int i = 0; i < convert; i++) {
			if (!feedOne(level, p))
				break;
			stack.shrink(1);
			if (stack.isEmpty())
				break;
		}
		super.inventoryTick(stack, level, entity, slot, selected);
	}

	private static void assignGeneIfAbsent(Level level, ItemStack stack) {
		var tag = net.crizo.rtcextras.item.BacteriumItem.getTag(stack);
		if (tag.contains("rtc_gene_id"))
			return;
		net.crizo.rtcextras.GeneRegistry.ensureLoaded(level);
		var gene = net.crizo.rtcextras.GeneRegistry.getRandomGene(level);
		if (gene == null)
			return;
		tag.putString("rtc_gene_id", gene.id());
		tag.putString("rtc_gene_name", gene.name());
		tag.putString("rtc_gene_tier", gene.tier());
		tag.putFloat("rtc_gene_interference", gene.interference());
		tag.putString("rtc_gene_effect", gene.effect());
		// mark it researched by default
		tag.putBoolean("researched", false);
		net.crizo.rtcextras.item.BacteriumItem.setTag(stack, tag);
	}

	private static boolean feedOne(Level level, Player p) {
		int size = p.getInventory().getContainerSize();
		int bestIdx = -1;
		long oldestCreated = Long.MAX_VALUE;
		// find an existing master with room; choose oldest created so all new adds share oldest cohort
		for (int i = 0; i < size; i++) {
			ItemStack st = p.getInventory().getItem(i);
			if (st.isEmpty() || !(st.getItem() instanceof BacteriumItem))
				continue;
			BacteriumItem.ensureCreated(level, st);
			long created = BacteriumItem.getTag(st).getLong(BacteriumItem.TAG_CREATED);
			if (st.getCount() < st.getMaxStackSize() && created < oldestCreated) {
				oldestCreated = created;
				bestIdx = i;
			}
		}
		if (bestIdx >= 0) {
			ItemStack master = p.getInventory().getItem(bestIdx);
			// ensure this cohort uses the oldest created timestamp
			BacteriumItem.setCreated(master, oldestCreated);
			// ensure it has a gene (but never change an existing one)
			assignGeneIfAbsent(level, master);
			master.grow(1);
			return true;
		}
		// no space → make a new master; inherit the oldestCreated if we had any bacterium, else now
		long createdForNew = (oldestCreated != Long.MAX_VALUE) ? oldestCreated : level.getGameTime();
		for (int i = 0; i < size; i++) {
			if (!p.getInventory().getItem(i).isEmpty())
				continue;
			ItemStack newMaster = new ItemStack(net.crizo.rtcextras.init.RtcExtrasModItems.BACTERIUM.get());
			BacteriumItem.setCreated(newMaster, createdForNew);
			newMaster.setCount(1);
			// assign its secret gene data + researched flag
			assignGeneIfAbsent(level, newMaster);
			p.getInventory().setItem(i, newMaster);
			return true;
		}
		return false; // no space
	}
}