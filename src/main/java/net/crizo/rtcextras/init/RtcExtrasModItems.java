/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.crizo.rtcextras.item.GeneSampleItem;
import net.crizo.rtcextras.item.BacteriumItem;
import net.crizo.rtcextras.item.BacteriumDummyItem;
import net.crizo.rtcextras.RtcExtrasMod;

public class RtcExtrasModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(RtcExtrasMod.MODID);
	public static final DeferredItem<Item> BACTERIUM_DUMMY = REGISTRY.register("bacterium_dummy", BacteriumDummyItem::new);
	public static final DeferredItem<Item> GENE_SEQUENCER = block(RtcExtrasModBlocks.GENE_SEQUENCER);
	public static final DeferredItem<Item> BACTERIUM = REGISTRY.register("bacterium", BacteriumItem::new);
	public static final DeferredItem<Item> BIOREACTOR = block(RtcExtrasModBlocks.BIOREACTOR);
	public static final DeferredItem<Item> GENE_SAMPLE = REGISTRY.register("gene_sample", GeneSampleItem::new);
	public static final DeferredItem<Item> ORANGE_LAYERROCK = block(RtcExtrasModBlocks.ORANGE_LAYERROCK);
	public static final DeferredItem<Item> RED_LAYERROCK = block(RtcExtrasModBlocks.RED_LAYERROCK);
	public static final DeferredItem<Item> DIRTY_SHALE = block(RtcExtrasModBlocks.DIRTY_SHALE);
	public static final DeferredItem<Item> YELLOW_LAYERROCK = block(RtcExtrasModBlocks.YELLOW_LAYERROCK);

	// Start of user code block custom items
	// End of user code block custom items
	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}