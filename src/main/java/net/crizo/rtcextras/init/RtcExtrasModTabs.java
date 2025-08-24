/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.crizo.rtcextras.RtcExtrasMod;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class RtcExtrasModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RtcExtrasMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RTC_EXTRAS = REGISTRY.register("rtc_extras",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.rtc_extras.rtc_extras")).icon(() -> new ItemStack(Blocks.POPPY)).displayItems((parameters, tabData) -> {
				tabData.accept(RtcExtrasModItems.BACTERIUM_DUMMY.get());
				tabData.accept(RtcExtrasModBlocks.GENE_SEQUENCER.get().asItem());
				tabData.accept(RtcExtrasModItems.BACTERIUM.get());
				tabData.accept(RtcExtrasModBlocks.BIOREACTOR.get().asItem());
				tabData.accept(RtcExtrasModItems.GENE_SAMPLE.get());
				tabData.accept(RtcExtrasModBlocks.ORANGE_LAYERROCK.get().asItem());
				tabData.accept(RtcExtrasModBlocks.RED_LAYERROCK.get().asItem());
				tabData.accept(RtcExtrasModBlocks.DIRTY_SHALE.get().asItem());
				tabData.accept(RtcExtrasModBlocks.YELLOW_LAYERROCK.get().asItem());
				tabData.accept(RtcExtrasModBlocks.HELIX_SPLICER.get().asItem());
				tabData.accept(RtcExtrasModBlocks.BIOCULTIVATOR.get().asItem());
				tabData.accept(RtcExtrasModItems.JELLY.get());
				tabData.accept(RtcExtrasModBlocks.CYANOSTONE.get().asItem());
				tabData.accept(RtcExtrasModBlocks.LAYERROCK.get().asItem());
				tabData.accept(RtcExtrasModBlocks.POLISHED_LAYERROCK.get().asItem());
				tabData.accept(RtcExtrasModBlocks.CHISELED_LAYERROCK.get().asItem());
				tabData.accept(RtcExtrasModBlocks.LAYERROCK_BRICKS.get().asItem());
				tabData.accept(RtcExtrasModBlocks.BROWN_VELUTIPE_MUSHROOM.get().asItem());
				tabData.accept(RtcExtrasModBlocks.RED_VELUTIPE_MUSHROOM_STEM.get().asItem());
				tabData.accept(RtcExtrasModBlocks.GREEN_VELUTIPE_MUSHROOM_STEM.get().asItem());
				tabData.accept(RtcExtrasModBlocks.RED_VELUTIPE_MUSHROOM.get().asItem());
				tabData.accept(RtcExtrasModBlocks.BLUE_VELUTIPE_MUSHROOM_STEM.get().asItem());
				tabData.accept(RtcExtrasModBlocks.MAGENTA_VELUTIPE_MUSHROOM.get().asItem());
				tabData.accept(RtcExtrasModBlocks.RED_VELUTIPE_PLANKS.get().asItem());
				tabData.accept(RtcExtrasModBlocks.RED_VELUTIPE_STAIRS.get().asItem());
				tabData.accept(RtcExtrasModBlocks.RED_VELUTIPE_SLAB.get().asItem());
				tabData.accept(RtcExtrasModBlocks.RED_VELUTIPE_FENCE.get().asItem());
				tabData.accept(RtcExtrasModBlocks.RED_VELUTIPE_FENCE_GATE.get().asItem());
				tabData.accept(RtcExtrasModBlocks.RED_VELUTIPE_PRESSURE_PLATE.get().asItem());
				tabData.accept(RtcExtrasModBlocks.RED_VELUTIPE_BUTTON.get().asItem());
				tabData.accept(RtcExtrasModBlocks.BLUE_VELUTIPE_PLANKS.get().asItem());
				tabData.accept(RtcExtrasModBlocks.BLUE_VELUTIPE_STAIRS.get().asItem());
				tabData.accept(RtcExtrasModBlocks.BLUE_VELUTIPE_SLAB.get().asItem());
				tabData.accept(RtcExtrasModBlocks.BLUE_VELUTIPE_FENCE.get().asItem());
				tabData.accept(RtcExtrasModBlocks.BLUE_VELUTIPE_FENCE_GATE.get().asItem());
				tabData.accept(RtcExtrasModBlocks.BLUE_VELUTIPE_PRESSURE_PLATE.get().asItem());
				tabData.accept(RtcExtrasModBlocks.BLUE_VELUTIPE_BUTTON.get().asItem());
				tabData.accept(RtcExtrasModBlocks.GREEN_VELUTIDE_PLANKS.get().asItem());
				tabData.accept(RtcExtrasModBlocks.GREEN_VELUTIDE_STAIRS.get().asItem());
				tabData.accept(RtcExtrasModBlocks.GREEN_VELUTIDE_SLAB.get().asItem());
				tabData.accept(RtcExtrasModBlocks.GREEN_VELUTIDE_FENCE.get().asItem());
				tabData.accept(RtcExtrasModBlocks.GREEN_VELUTIDE_FENCE_GATE.get().asItem());
				tabData.accept(RtcExtrasModBlocks.GREEN_VELUTIDE_PRESSURE_PLATE.get().asItem());
				tabData.accept(RtcExtrasModBlocks.GREEN_VELUTIDE_BUTTON.get().asItem());
				tabData.accept(RtcExtrasModItems.RED_SPORE.get());
				tabData.accept(RtcExtrasModItems.BROWN_SPORE.get());
				tabData.accept(RtcExtrasModItems.MAGENTA_SPORE.get());
				tabData.accept(RtcExtrasModBlocks.JELLY_BLOCK.get().asItem());
			}).withSearchBar().build());

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
			tabData.accept(RtcExtrasModBlocks.LAYERROCK_ROOTS.get().asItem());
			tabData.accept(RtcExtrasModBlocks.SHORT_LAYERROCK_ROOTS.get().asItem());
			tabData.accept(RtcExtrasModBlocks.TALL_LAYERROCK_ROOTS.get().asItem());
			tabData.accept(RtcExtrasModBlocks.BULBUSH.get().asItem());
		} else if (tabData.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
			tabData.accept(RtcExtrasModItems.JELLY.get());
		}
	}
}