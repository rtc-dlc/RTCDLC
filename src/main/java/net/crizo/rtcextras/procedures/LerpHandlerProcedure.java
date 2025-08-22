package net.crizo.rtcextras.procedures;

import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.client.Minecraft;

import net.crizo.rtcextras.world.inventory.GUIGeneSequencerMenu;

@EventBusSubscriber
public class LerpHandlerProcedure {
	@SubscribeEvent
	public static void onRenderGui(RenderGuiEvent.Post event) {
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			execute(player);
		}
	}

	private static void execute(Player player) {
		if (player.containerMenu instanceof GUIGeneSequencerMenu) {
			player.getPersistentData().putDouble("BannerXTarget", 100);
		} else {
			player.getPersistentData().putDouble("BannerXTarget", -100);
		}
		double current = player.getPersistentData().getDouble("BannerX");
		double target = player.getPersistentData().getDouble("BannerXTarget");
		player.getPersistentData().putDouble("BannerX", Mth.lerp(0.01, current, target));
	}
}