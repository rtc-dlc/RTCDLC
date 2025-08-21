/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.crizo.rtcextras.client.gui.GUIGeneSequencerScreen;
import net.crizo.rtcextras.client.gui.GUIBioreactorScreen;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RtcExtrasModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(RtcExtrasModMenus.GUI_BIOREACTOR.get(), GUIBioreactorScreen::new);
		event.register(RtcExtrasModMenus.GUI_GENE_SEQUENCER.get(), GUIGeneSequencerScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}