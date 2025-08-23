/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import net.crizo.rtcextras.RtcExtrasMod;

public class RtcExtrasModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, RtcExtrasMod.MODID);
	public static final DeferredHolder<SoundEvent, SoundEvent> SHROOM_EXPLODE = REGISTRY.register("shroom_explode", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("rtc_extras", "shroom_explode")));
}