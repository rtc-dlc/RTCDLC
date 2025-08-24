/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RtcExtrasModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(RtcExtrasModEntities.PROJ_RED_SPORE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(RtcExtrasModEntities.PROJ_BROWN_SPORE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(RtcExtrasModEntities.PROJ_MAGENTA_SPORE.get(), ThrownItemRenderer::new);
	}
}