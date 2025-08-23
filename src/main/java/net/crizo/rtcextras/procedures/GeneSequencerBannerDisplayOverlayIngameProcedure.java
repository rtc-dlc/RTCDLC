package net.crizo.rtcextras.procedures;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import net.crizo.rtcextras.world.inventory.GUIGeneSequencerMenu;

public class GeneSequencerBannerDisplayOverlayIngameProcedure {
	public static boolean execute(Entity entity) {
		if (entity == null)
			return false;
		return entity instanceof Player _plr0 && _plr0.containerMenu instanceof GUIGeneSequencerMenu;
	}
}