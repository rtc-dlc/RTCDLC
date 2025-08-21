/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.Block;

import net.crizo.rtcextras.block.YellowLayerrockBlock;
import net.crizo.rtcextras.block.RedLayerrockBlock;
import net.crizo.rtcextras.block.OrangeLayerrockBlock;
import net.crizo.rtcextras.block.GeneSequencerBlock;
import net.crizo.rtcextras.block.DirtyShaleBlock;
import net.crizo.rtcextras.block.BioreactorBlock;
import net.crizo.rtcextras.RtcExtrasMod;

public class RtcExtrasModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(RtcExtrasMod.MODID);
	public static final DeferredBlock<Block> GENE_SEQUENCER = REGISTRY.register("gene_sequencer", GeneSequencerBlock::new);
	public static final DeferredBlock<Block> BIOREACTOR = REGISTRY.register("bioreactor", BioreactorBlock::new);
	public static final DeferredBlock<Block> DIRTY_SHALE = REGISTRY.register("dirty_shale", DirtyShaleBlock::new);
	public static final DeferredBlock<Block> YELLOW_LAYERROCK = REGISTRY.register("yellow_layerrock", YellowLayerrockBlock::new);
	public static final DeferredBlock<Block> RED_LAYERROCK = REGISTRY.register("red_layerrock", RedLayerrockBlock::new);
	public static final DeferredBlock<Block> ORANGE_LAYERROCK = REGISTRY.register("orange_layerrock", OrangeLayerrockBlock::new);
	// Start of user code block custom blocks
	// End of user code block custom blocks
}