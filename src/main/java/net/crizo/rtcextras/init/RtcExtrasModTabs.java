/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.crizo.rtcextras.RtcExtrasMod;

public class RtcExtrasModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RtcExtrasMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RTC_EXTRAS = REGISTRY.register("rtc_extras",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.rtc_extras.rtc_extras")).icon(() -> new ItemStack(Blocks.POPPY)).displayItems((parameters, tabData) -> {
				tabData.accept(RtcExtrasModItems.BACTERIUM_DUMMY.get());
				tabData.accept(RtcExtrasModBlocks.GENE_SEQUENCER.get().asItem());
				tabData.accept(RtcExtrasModItems.BACTERIUM.get());
				tabData.accept(RtcExtrasModBlocks.BIOREACTOR.get().asItem());
				tabData.accept(RtcExtrasModItems.GENE_SAMPLE.get());
			}).withSearchBar().build());
}