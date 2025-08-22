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
import net.crizo.rtcextras.block.RedLayerrockBlock;
import net.crizo.rtcextras.block.OrangeLayerrockBlock;
import net.crizo.rtcextras.block.LayerrockRootsBlock;
import net.crizo.rtcextras.block.HelixSplicerBlock;
import net.crizo.rtcextras.block.GlieseFossilBlock;
import net.crizo.rtcextras.block.GeneSequencerBlock;
import net.crizo.rtcextras.block.DirtyShaleBlock;
import net.crizo.rtcextras.block.BulbushBlock;
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