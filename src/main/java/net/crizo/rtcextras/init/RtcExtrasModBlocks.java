/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.block.Block;

import net.crizo.rtcextras.block.YellowLayerrockBlock;
import net.crizo.rtcextras.block.TallLayerrockRootsBlock;
import net.crizo.rtcextras.block.ShortLayerrockRootsBlock;
import net.crizo.rtcextras.block.RedVelutipeShroomlingBlock;
import net.crizo.rtcextras.block.RedVelutipeMushroomStemBlock;
import net.crizo.rtcextras.block.RedVelutipeMushroomBlock;
import net.crizo.rtcextras.block.RedVelutideStairsBlock;
import net.crizo.rtcextras.block.RedVelutideSlabBlock;
import net.crizo.rtcextras.block.RedVelutidePressurePlateBlock;
import net.crizo.rtcextras.block.RedVelutidePlanksBlock;
import net.crizo.rtcextras.block.RedVelutideFenceGateBlock;
import net.crizo.rtcextras.block.RedVelutideFenceBlock;
import net.crizo.rtcextras.block.RedVelutideButtonBlock;
import net.crizo.rtcextras.block.RedLayerrockBlock;
import net.crizo.rtcextras.block.PolishedLayerrockBlock;
import net.crizo.rtcextras.block.OrangeLayerrockBlock;
import net.crizo.rtcextras.block.MagentaVelutipeShroomlingBlock;
import net.crizo.rtcextras.block.MagentaVelutipeMushroomBlock;
import net.crizo.rtcextras.block.LayerrockRootsBlock;
import net.crizo.rtcextras.block.LayerrockBricksBlock;
import net.crizo.rtcextras.block.LayerrockBlock;
import net.crizo.rtcextras.block.JellyBlockBlock;
import net.crizo.rtcextras.block.InnoculiteBlock;
import net.crizo.rtcextras.block.HemolithBlock;
import net.crizo.rtcextras.block.HelixSplicerBlock;
import net.crizo.rtcextras.block.GreenVelutipeMushroomStemBlock;
import net.crizo.rtcextras.block.GreenVelutideStairsBlock;
import net.crizo.rtcextras.block.GreenVelutideSlabBlock;
import net.crizo.rtcextras.block.GreenVelutidePressurePlateBlock;
import net.crizo.rtcextras.block.GreenVelutidePlanksBlock;
import net.crizo.rtcextras.block.GreenVelutideFenceGateBlock;
import net.crizo.rtcextras.block.GreenVelutideFenceBlock;
import net.crizo.rtcextras.block.GreenVelutideButtonBlock;
import net.crizo.rtcextras.block.GlieseFossilBlock;
import net.crizo.rtcextras.block.GeneSequencerBlock;
import net.crizo.rtcextras.block.DirtyShaleBlock;
import net.crizo.rtcextras.block.CyanostoneBlock;
import net.crizo.rtcextras.block.ChiseledLayerrockBlock;
import net.crizo.rtcextras.block.BulbushBlock;
import net.crizo.rtcextras.block.BrownVelutipeShroomlingBlock;
import net.crizo.rtcextras.block.BrownVelutipeMushroomBlock;
import net.crizo.rtcextras.block.BlueVelutipeMushroomStemBlock;
import net.crizo.rtcextras.block.BlueVelutideStairsBlock;
import net.crizo.rtcextras.block.BlueVelutideSlabBlock;
import net.crizo.rtcextras.block.BlueVelutidePressurePlateBlock;
import net.crizo.rtcextras.block.BlueVelutidePlanksBlock;
import net.crizo.rtcextras.block.BlueVelutideFenceGateBlock;
import net.crizo.rtcextras.block.BlueVelutideFenceBlock;
import net.crizo.rtcextras.block.BlueVelutideButtonBlock;
import net.crizo.rtcextras.block.BioreactorBlock;
import net.crizo.rtcextras.block.BiocultivatorBlock;
import net.crizo.rtcextras.RtcExtrasMod;

public class RtcExtrasModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(RtcExtrasMod.MODID);
	public static final DeferredBlock<Block> GENE_SEQUENCER = REGISTRY.register("gene_sequencer", GeneSequencerBlock::new);
	public static final DeferredBlock<Block> BIOREACTOR = REGISTRY.register("bioreactor", BioreactorBlock::new);
	public static final DeferredBlock<Block> ORANGE_LAYERROCK = REGISTRY.register("orange_layerrock", OrangeLayerrockBlock::new);
	public static final DeferredBlock<Block> RED_LAYERROCK = REGISTRY.register("red_layerrock", RedLayerrockBlock::new);
	public static final DeferredBlock<Block> DIRTY_SHALE = REGISTRY.register("dirty_shale", DirtyShaleBlock::new);
	public static final DeferredBlock<Block> YELLOW_LAYERROCK = REGISTRY.register("yellow_layerrock", YellowLayerrockBlock::new);
	public static final DeferredBlock<Block> GLIESE_FOSSIL = REGISTRY.register("gliese_fossil", GlieseFossilBlock::new);
	public static final DeferredBlock<Block> LAYERROCK_ROOTS = REGISTRY.register("layerrock_roots", LayerrockRootsBlock::new);
	public static final DeferredBlock<Block> SHORT_LAYERROCK_ROOTS = REGISTRY.register("short_layerrock_roots", ShortLayerrockRootsBlock::new);
	public static final DeferredBlock<Block> TALL_LAYERROCK_ROOTS = REGISTRY.register("tall_layerrock_roots", TallLayerrockRootsBlock::new);
	public static final DeferredBlock<Block> BULBUSH = REGISTRY.register("bulbush", BulbushBlock::new);
	public static final DeferredBlock<Block> HELIX_SPLICER = REGISTRY.register("helix_splicer", HelixSplicerBlock::new);
	public static final DeferredBlock<Block> BIOCULTIVATOR = REGISTRY.register("biocultivator", BiocultivatorBlock::new);
	public static final DeferredBlock<Block> CYANOSTONE = REGISTRY.register("cyanostone", CyanostoneBlock::new);
	public static final DeferredBlock<Block> LAYERROCK = REGISTRY.register("layerrock", LayerrockBlock::new);
	public static final DeferredBlock<Block> POLISHED_LAYERROCK = REGISTRY.register("polished_layerrock", PolishedLayerrockBlock::new);
	public static final DeferredBlock<Block> CHISELED_LAYERROCK = REGISTRY.register("chiseled_layerrock", ChiseledLayerrockBlock::new);
	public static final DeferredBlock<Block> LAYERROCK_BRICKS = REGISTRY.register("layerrock_bricks", LayerrockBricksBlock::new);
	public static final DeferredBlock<Block> INNOCULITE = REGISTRY.register("innoculite", InnoculiteBlock::new);
	public static final DeferredBlock<Block> HEMOLITH = REGISTRY.register("hemolith", HemolithBlock::new);
	public static final DeferredBlock<Block> BROWN_VELUTIPE_MUSHROOM = REGISTRY.register("brown_velutipe_mushroom", BrownVelutipeMushroomBlock::new);
	public static final DeferredBlock<Block> RED_VELUTIPE_MUSHROOM_STEM = REGISTRY.register("red_velutipe_mushroom_stem", RedVelutipeMushroomStemBlock::new);
	public static final DeferredBlock<Block> GREEN_VELUTIPE_MUSHROOM_STEM = REGISTRY.register("green_velutipe_mushroom_stem", GreenVelutipeMushroomStemBlock::new);
	public static final DeferredBlock<Block> RED_VELUTIPE_MUSHROOM = REGISTRY.register("red_velutipe_mushroom", RedVelutipeMushroomBlock::new);
	public static final DeferredBlock<Block> BLUE_VELUTIPE_MUSHROOM_STEM = REGISTRY.register("blue_velutipe_mushroom_stem", BlueVelutipeMushroomStemBlock::new);
	public static final DeferredBlock<Block> MAGENTA_VELUTIPE_MUSHROOM = REGISTRY.register("magenta_velutipe_mushroom", MagentaVelutipeMushroomBlock::new);
	public static final DeferredBlock<Block> RED_VELUTIPE_PLANKS = REGISTRY.register("red_velutipe_planks", RedVelutidePlanksBlock::new);
	public static final DeferredBlock<Block> RED_VELUTIPE_STAIRS = REGISTRY.register("red_velutipe_stairs", RedVelutideStairsBlock::new);
	public static final DeferredBlock<Block> RED_VELUTIPE_SLAB = REGISTRY.register("red_velutipe_slab", RedVelutideSlabBlock::new);
	public static final DeferredBlock<Block> RED_VELUTIPE_FENCE = REGISTRY.register("red_velutipe_fence", RedVelutideFenceBlock::new);
	public static final DeferredBlock<Block> RED_VELUTIPE_FENCE_GATE = REGISTRY.register("red_velutipe_fence_gate", RedVelutideFenceGateBlock::new);
	public static final DeferredBlock<Block> RED_VELUTIPE_PRESSURE_PLATE = REGISTRY.register("red_velutipe_pressure_plate", RedVelutidePressurePlateBlock::new);
	public static final DeferredBlock<Block> RED_VELUTIPE_BUTTON = REGISTRY.register("red_velutipe_button", RedVelutideButtonBlock::new);
	public static final DeferredBlock<Block> BLUE_VELUTIPE_PLANKS = REGISTRY.register("blue_velutipe_planks", BlueVelutidePlanksBlock::new);
	public static final DeferredBlock<Block> BLUE_VELUTIPE_STAIRS = REGISTRY.register("blue_velutipe_stairs", BlueVelutideStairsBlock::new);
	public static final DeferredBlock<Block> BLUE_VELUTIPE_SLAB = REGISTRY.register("blue_velutipe_slab", BlueVelutideSlabBlock::new);
	public static final DeferredBlock<Block> BLUE_VELUTIPE_FENCE = REGISTRY.register("blue_velutipe_fence", BlueVelutideFenceBlock::new);
	public static final DeferredBlock<Block> BLUE_VELUTIPE_FENCE_GATE = REGISTRY.register("blue_velutipe_fence_gate", BlueVelutideFenceGateBlock::new);
	public static final DeferredBlock<Block> BLUE_VELUTIPE_PRESSURE_PLATE = REGISTRY.register("blue_velutipe_pressure_plate", BlueVelutidePressurePlateBlock::new);
	public static final DeferredBlock<Block> BLUE_VELUTIPE_BUTTON = REGISTRY.register("blue_velutipe_button", BlueVelutideButtonBlock::new);
	public static final DeferredBlock<Block> GREEN_VELUTIDE_PLANKS = REGISTRY.register("green_velutide_planks", GreenVelutidePlanksBlock::new);
	public static final DeferredBlock<Block> GREEN_VELUTIDE_STAIRS = REGISTRY.register("green_velutide_stairs", GreenVelutideStairsBlock::new);
	public static final DeferredBlock<Block> GREEN_VELUTIDE_SLAB = REGISTRY.register("green_velutide_slab", GreenVelutideSlabBlock::new);
	public static final DeferredBlock<Block> GREEN_VELUTIDE_FENCE = REGISTRY.register("green_velutide_fence", GreenVelutideFenceBlock::new);
	public static final DeferredBlock<Block> GREEN_VELUTIDE_FENCE_GATE = REGISTRY.register("green_velutide_fence_gate", GreenVelutideFenceGateBlock::new);
	public static final DeferredBlock<Block> GREEN_VELUTIDE_PRESSURE_PLATE = REGISTRY.register("green_velutide_pressure_plate", GreenVelutidePressurePlateBlock::new);
	public static final DeferredBlock<Block> GREEN_VELUTIDE_BUTTON = REGISTRY.register("green_velutide_button", GreenVelutideButtonBlock::new);
	public static final DeferredBlock<Block> RED_VELUTIPE_SHROOMLING = REGISTRY.register("red_velutipe_shroomling", RedVelutipeShroomlingBlock::new);
	public static final DeferredBlock<Block> BROWN_VELUTIPE_SHROOMLING = REGISTRY.register("brown_velutipe_shroomling", BrownVelutipeShroomlingBlock::new);
	public static final DeferredBlock<Block> MAGENTA_VELUTIPE_SHROOMLING = REGISTRY.register("magenta_velutipe_shroomling", MagentaVelutipeShroomlingBlock::new);
	public static final DeferredBlock<Block> JELLY_BLOCK = REGISTRY.register("jelly_block", JellyBlockBlock::new);

	// Start of user code block custom blocks
	// End of user code block custom blocks
	@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class BlocksClientSideHandler {
		@SubscribeEvent
		public static void blockColorLoad(RegisterColorHandlersEvent.Block event) {
			LayerrockRootsBlock.blockColorLoad(event);
			ShortLayerrockRootsBlock.blockColorLoad(event);
			TallLayerrockRootsBlock.blockColorLoad(event);
		}
	}
}