/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.BlockItem;

import net.crizo.rtcextras.item.WasteBiomassItem;
import net.crizo.rtcextras.item.RedSporeSingletItem;
import net.crizo.rtcextras.item.RedSporeItem;
import net.crizo.rtcextras.item.MagentaSporeSingletItem;
import net.crizo.rtcextras.item.MagentaSporeItem;
import net.crizo.rtcextras.item.JellyItem;
import net.crizo.rtcextras.item.GeneSampleItem;
import net.crizo.rtcextras.item.BrownSporeSingletItem;
import net.crizo.rtcextras.item.BrownSporeItem;
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
	public static final DeferredItem<Item> GLIESE_FOSSIL = block(RtcExtrasModBlocks.GLIESE_FOSSIL);
	public static final DeferredItem<Item> LAYERROCK_ROOTS = block(RtcExtrasModBlocks.LAYERROCK_ROOTS);
	public static final DeferredItem<Item> SHORT_LAYERROCK_ROOTS = block(RtcExtrasModBlocks.SHORT_LAYERROCK_ROOTS);
	public static final DeferredItem<Item> TALL_LAYERROCK_ROOTS = doubleBlock(RtcExtrasModBlocks.TALL_LAYERROCK_ROOTS);
	public static final DeferredItem<Item> BULBUSH = block(RtcExtrasModBlocks.BULBUSH);
	public static final DeferredItem<Item> HELIX_SPLICER = block(RtcExtrasModBlocks.HELIX_SPLICER);
	public static final DeferredItem<Item> BIOCULTIVATOR = block(RtcExtrasModBlocks.BIOCULTIVATOR);
	public static final DeferredItem<Item> JELLY = REGISTRY.register("jelly", JellyItem::new);
	public static final DeferredItem<Item> CYANOSTONE = block(RtcExtrasModBlocks.CYANOSTONE);
	public static final DeferredItem<Item> LAYERROCK = block(RtcExtrasModBlocks.LAYERROCK);
	public static final DeferredItem<Item> POLISHED_LAYERROCK = block(RtcExtrasModBlocks.POLISHED_LAYERROCK);
	public static final DeferredItem<Item> CHISELED_LAYERROCK = block(RtcExtrasModBlocks.CHISELED_LAYERROCK);
	public static final DeferredItem<Item> LAYERROCK_BRICKS = block(RtcExtrasModBlocks.LAYERROCK_BRICKS);
	public static final DeferredItem<Item> INNOCULITE = block(RtcExtrasModBlocks.INNOCULITE);
	public static final DeferredItem<Item> HEMOLITH = block(RtcExtrasModBlocks.HEMOLITH);
	public static final DeferredItem<Item> BROWN_VELUTIPE_MUSHROOM = block(RtcExtrasModBlocks.BROWN_VELUTIPE_MUSHROOM);
	public static final DeferredItem<Item> RED_VELUTIPE_MUSHROOM_STEM = block(RtcExtrasModBlocks.RED_VELUTIPE_MUSHROOM_STEM);
	public static final DeferredItem<Item> GREEN_VELUTIPE_MUSHROOM_STEM = block(RtcExtrasModBlocks.GREEN_VELUTIPE_MUSHROOM_STEM);
	public static final DeferredItem<Item> RED_VELUTIPE_MUSHROOM = block(RtcExtrasModBlocks.RED_VELUTIPE_MUSHROOM);
	public static final DeferredItem<Item> BLUE_VELUTIPE_MUSHROOM_STEM = block(RtcExtrasModBlocks.BLUE_VELUTIPE_MUSHROOM_STEM);
	public static final DeferredItem<Item> MAGENTA_VELUTIPE_MUSHROOM = block(RtcExtrasModBlocks.MAGENTA_VELUTIPE_MUSHROOM);
	public static final DeferredItem<Item> RED_VELUTIPE_PLANKS = block(RtcExtrasModBlocks.RED_VELUTIPE_PLANKS);
	public static final DeferredItem<Item> RED_VELUTIPE_STAIRS = block(RtcExtrasModBlocks.RED_VELUTIPE_STAIRS);
	public static final DeferredItem<Item> RED_VELUTIPE_SLAB = block(RtcExtrasModBlocks.RED_VELUTIPE_SLAB);
	public static final DeferredItem<Item> RED_VELUTIPE_FENCE = block(RtcExtrasModBlocks.RED_VELUTIPE_FENCE);
	public static final DeferredItem<Item> RED_VELUTIPE_FENCE_GATE = block(RtcExtrasModBlocks.RED_VELUTIPE_FENCE_GATE);
	public static final DeferredItem<Item> RED_VELUTIPE_PRESSURE_PLATE = block(RtcExtrasModBlocks.RED_VELUTIPE_PRESSURE_PLATE);
	public static final DeferredItem<Item> RED_VELUTIPE_BUTTON = block(RtcExtrasModBlocks.RED_VELUTIPE_BUTTON);
	public static final DeferredItem<Item> BLUE_VELUTIPE_PLANKS = block(RtcExtrasModBlocks.BLUE_VELUTIPE_PLANKS);
	public static final DeferredItem<Item> BLUE_VELUTIPE_STAIRS = block(RtcExtrasModBlocks.BLUE_VELUTIPE_STAIRS);
	public static final DeferredItem<Item> BLUE_VELUTIPE_SLAB = block(RtcExtrasModBlocks.BLUE_VELUTIPE_SLAB);
	public static final DeferredItem<Item> BLUE_VELUTIPE_FENCE = block(RtcExtrasModBlocks.BLUE_VELUTIPE_FENCE);
	public static final DeferredItem<Item> BLUE_VELUTIPE_FENCE_GATE = block(RtcExtrasModBlocks.BLUE_VELUTIPE_FENCE_GATE);
	public static final DeferredItem<Item> BLUE_VELUTIPE_PRESSURE_PLATE = block(RtcExtrasModBlocks.BLUE_VELUTIPE_PRESSURE_PLATE);
	public static final DeferredItem<Item> BLUE_VELUTIPE_BUTTON = block(RtcExtrasModBlocks.BLUE_VELUTIPE_BUTTON);
	public static final DeferredItem<Item> GREEN_VELUTIDE_PLANKS = block(RtcExtrasModBlocks.GREEN_VELUTIDE_PLANKS);
	public static final DeferredItem<Item> GREEN_VELUTIDE_STAIRS = block(RtcExtrasModBlocks.GREEN_VELUTIDE_STAIRS);
	public static final DeferredItem<Item> GREEN_VELUTIDE_SLAB = block(RtcExtrasModBlocks.GREEN_VELUTIDE_SLAB);
	public static final DeferredItem<Item> GREEN_VELUTIDE_FENCE = block(RtcExtrasModBlocks.GREEN_VELUTIDE_FENCE);
	public static final DeferredItem<Item> GREEN_VELUTIDE_FENCE_GATE = block(RtcExtrasModBlocks.GREEN_VELUTIDE_FENCE_GATE);
	public static final DeferredItem<Item> GREEN_VELUTIDE_PRESSURE_PLATE = block(RtcExtrasModBlocks.GREEN_VELUTIDE_PRESSURE_PLATE);
	public static final DeferredItem<Item> GREEN_VELUTIDE_BUTTON = block(RtcExtrasModBlocks.GREEN_VELUTIDE_BUTTON);
	public static final DeferredItem<Item> RED_SPORE = REGISTRY.register("red_spore", RedSporeItem::new);
	public static final DeferredItem<Item> BROWN_SPORE = REGISTRY.register("brown_spore", BrownSporeItem::new);
	public static final DeferredItem<Item> MAGENTA_SPORE = REGISTRY.register("magenta_spore", MagentaSporeItem::new);
	public static final DeferredItem<Item> RED_VELUTIPE_SHROOMLING = block(RtcExtrasModBlocks.RED_VELUTIPE_SHROOMLING);
	public static final DeferredItem<Item> BROWN_VELUTIPE_SHROOMLING = block(RtcExtrasModBlocks.BROWN_VELUTIPE_SHROOMLING);
	public static final DeferredItem<Item> MAGENTA_VELUTIPE_SHROOMLING = block(RtcExtrasModBlocks.MAGENTA_VELUTIPE_SHROOMLING);
	public static final DeferredItem<Item> JELLY_BLOCK = block(RtcExtrasModBlocks.JELLY_BLOCK);
	public static final DeferredItem<Item> RED_SPORE_SINGLET = REGISTRY.register("red_spore_singlet", RedSporeSingletItem::new);
	public static final DeferredItem<Item> BROWN_SPORE_SINGLET = REGISTRY.register("brown_spore_singlet", BrownSporeSingletItem::new);
	public static final DeferredItem<Item> MAGENTA_SPORE_SINGLET = REGISTRY.register("magenta_spore_singlet", MagentaSporeSingletItem::new);
	public static final DeferredItem<Item> WASTE_BIOMASS = REGISTRY.register("waste_biomass", WasteBiomassItem::new);

	// Start of user code block custom items
	// End of user code block custom items
	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}

	private static DeferredItem<Item> doubleBlock(DeferredHolder<Block, Block> block) {
		return doubleBlock(block, new Item.Properties());
	}

	private static DeferredItem<Item> doubleBlock(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new DoubleHighBlockItem(block.get(), properties));
	}
}