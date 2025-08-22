package net.crizo.rtcextras.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.Entity;

import net.crizo.rtcextras.network.RtcExtrasModVariables;
import net.crizo.rtcextras.RtcExtrasMod;

import javax.annotation.Nullable;

/* imports omitted */
@EventBusSubscriber
public class PlayerGeneticTickProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		execute(event, event.getEntity());
	}

	public static void execute(Entity entity) {
		execute(null, entity);
	}

	private static void execute(@Nullable Event event, Entity entity) {
		if (entity == null)
			return;
		var vars = entity.getData(RtcExtrasModVariables.PLAYER_VARIABLES);
		String gene = vars.geneContent; // could be "", could be null
		int countdown = (int) vars.geneReplacementCountdown; // you expect -1 to mean “armed”
		// Correct emptiness check (handles "", "   ", and null):
		boolean hasGene = gene != null && !gene.isBlank();
		if (hasGene && countdown == -1) {
			//System.out.println("Gene is working");
			// do stuff for gene expression here on the player. 
		}
		// Better logging so you can SEE what's in there
		//RtcExtrasMod.LOGGER.info("gene='{}' len={} countdown={}", gene == null ? "null" : gene.replace("\n", "\\n"), gene == null ? -1 : gene.length(), countdown);
	}
}