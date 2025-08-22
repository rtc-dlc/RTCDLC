/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.Minecraft;

import net.crizo.rtcextras.world.inventory.GUISplicerMenu;
import net.crizo.rtcextras.world.inventory.GUIGeneSequencerMenu;
import net.crizo.rtcextras.world.inventory.GUICultivatorMenu;
import net.crizo.rtcextras.world.inventory.GUIBioreactorMenu;
import net.crizo.rtcextras.network.MenuStateUpdateMessage;
import net.crizo.rtcextras.RtcExtrasMod;

import java.util.Map;

public class RtcExtrasModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, RtcExtrasMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<GUIBioreactorMenu>> GUI_BIOREACTOR = REGISTRY.register("gui_bioreactor", () -> IMenuTypeExtension.create(GUIBioreactorMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<GUIGeneSequencerMenu>> GUI_GENE_SEQUENCER = REGISTRY.register("gui_gene_sequencer", () -> IMenuTypeExtension.create(GUIGeneSequencerMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<GUISplicerMenu>> GUI_SPLICER = REGISTRY.register("gui_splicer", () -> IMenuTypeExtension.create(GUISplicerMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<GUICultivatorMenu>> GUI_CULTIVATOR = REGISTRY.register("gui_cultivator", () -> IMenuTypeExtension.create(GUICultivatorMenu::new));

	public interface MenuAccessor {
		Map<String, Object> getMenuState();

		Map<Integer, Slot> getSlots();

		default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
			getMenuState().put(elementType + ":" + name, elementState);
			if (player instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new MenuStateUpdateMessage(elementType, name, elementState));
			} else if (player.level().isClientSide) {
				if (Minecraft.getInstance().screen instanceof RtcExtrasModScreens.ScreenAccessor accessor && needClientUpdate)
					accessor.updateMenuState(elementType, name, elementState);
				PacketDistributor.sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
			}
		}

		default <T> T getMenuState(int elementType, String name, T defaultValue) {
			try {
				return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
			} catch (ClassCastException e) {
				return defaultValue;
			}
		}
	}
}