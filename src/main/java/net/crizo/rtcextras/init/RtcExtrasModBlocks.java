/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.Block;

import net.crizo.rtcextras.block.GeneSequencerBlock;
import net.crizo.rtcextras.block.BioreactorBlock;
import net.crizo.rtcextras.RtcExtrasMod;

public class RtcExtrasModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(RtcExtrasMod.MODID);
	public static final DeferredBlock<Block> GENE_SEQUENCER = REGISTRY.register("gene_sequencer", GeneSequencerBlock::new);
	public static final DeferredBlock<Block> BIOREACTOR = REGISTRY.register("bioreactor", BioreactorBlock::new);
	// Start of user code block custom blocks
	// End of user code block custom blocks
}