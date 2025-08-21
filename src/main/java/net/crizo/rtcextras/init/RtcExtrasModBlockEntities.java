/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import net.crizo.rtcextras.block.entity.GeneSequencerBlockEntity;
import net.crizo.rtcextras.block.entity.BioreactorBlockEntity;
import net.crizo.rtcextras.RtcExtrasMod;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class RtcExtrasModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, RtcExtrasMod.MODID);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BioreactorBlockEntity>> BIOREACTOR = register("bioreactor", RtcExtrasModBlocks.BIOREACTOR, BioreactorBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneSequencerBlockEntity>> GENE_SEQUENCER = register("gene_sequencer", RtcExtrasModBlocks.GENE_SEQUENCER, GeneSequencerBlockEntity::new);

	// Start of user code block custom block entities
	// End of user code block custom block entities
	private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String registryname, DeferredHolder<Block, Block> block, BlockEntityType.BlockEntitySupplier<T> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BIOREACTOR.get(), SidedInvWrapper::new);
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, GENE_SEQUENCER.get(), SidedInvWrapper::new);
	}
}