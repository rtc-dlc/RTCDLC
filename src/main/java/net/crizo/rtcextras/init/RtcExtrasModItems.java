/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import net.minecraft.world.item.Item;

import net.crizo.rtcextras.item.BacteriumItem;
import net.crizo.rtcextras.item.BacteriumDummyItem;
import net.crizo.rtcextras.RtcExtrasMod;

public class RtcExtrasModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(RtcExtrasMod.MODID);
	public static final DeferredItem<Item> BACTERIUM = REGISTRY.register("bacterium", BacteriumItem::new);
	public static final DeferredItem<Item> BACTERIUM_DUMMY = REGISTRY.register("bacterium_dummy", BacteriumDummyItem::new);
	// Start of user code block custom items
	// End of user code block custom items
}