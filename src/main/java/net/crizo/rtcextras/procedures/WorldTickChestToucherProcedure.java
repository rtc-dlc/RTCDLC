package net.crizo.rtcextras.procedures;

import org.checkerframework.checker.units.qual.s;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

//@EventBusSubscriber
public class WorldTickChestToucherProcedure {
	// OPTIONAL: touch bacterium in *loaded* block entities once in a while without being opened.
	// This is throttled to 1 chunk per tick to avoid lag. Safe to remove if you prefer pure "on-access" decay.
	/*
	private static int chunkCursor = 0;

	@SubscribeEvent
	public static void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
		var level = event.getLevel();
		if (level.isClientSide())
			return;
		var chunks = level.getChunkSource().chunkMap.trackedChunks.values(); // loaded chunks
		if (chunks.isEmpty())
			return;
		int idx = chunkCursor % chunks.size();
		int i = 0;
		for (var holder : chunks) {
			if (i++ != idx)
				continue;
			var chunk = holder.getTickingChunk();
			if (chunk == null)
				break;
			for (var be : chunk.getBlockEntities().values()) {
				if (be instanceof net.minecraft.world.Container cont) {
					for (int s = 0; s < cont.getContainerSize(); s++) {
						var stack = cont.getItem(s);
						if (!stack.isEmpty() && stack.getItem() instanceof net.crizo.rtcextras.item.BacteriumItem) {
							net.crizo.rtcextras.item.BacteriumItem.applyDecay(level, stack);
							cont.setItem(s, stack);
						}
					}
				}
			}
			break;
		}
		chunkCursor++;
	}
	*/ // YOU HAVE BEEN SILENCED!
}