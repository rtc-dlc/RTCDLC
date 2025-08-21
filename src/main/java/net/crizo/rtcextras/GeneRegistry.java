package net.crizo.rtcextras;

import org.checkerframework.checker.units.qual.g;

import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;

import java.nio.charset.StandardCharsets;

import java.io.InputStreamReader;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

public final class GeneRegistry {
	private static final ResourceLocation GENE_JSON = net.minecraft.resources.ResourceLocation.parse("rtc_extras:genes/genes.json");
	private static boolean loaded = false;
	private static final Map<String, Integer> tierWeights = new HashMap<>();
	private static final Map<String, List<GeneData>> byTier = new HashMap<>();

	private GeneRegistry() {
	}

	public static synchronized void ensureLoaded(Level level) {
		if (loaded)
			return;
		MinecraftServer server = level.getServer();
		if (server == null)
			return; // should only happen client-side; try again later
		try {
			Optional<Resource> res = server.getResourceManager().getResource(GENE_JSON);
			if (res.isEmpty()) {
				// Optional fallback to classpath (dev env)
				try (var in = GeneRegistry.class.getClassLoader().getResourceAsStream("data/rtc_extras/genes/genes.json")) {
					if (in == null)
						throw new IllegalStateException("genes.json not found: " + GENE_JSON);
					parse(new InputStreamReader(in, StandardCharsets.UTF_8));
				}
			} else {
				try (var in = res.get().open()) {
					parse(new InputStreamReader(in, StandardCharsets.UTF_8));
				}
			}
			loaded = true;
		} catch (Exception e) {
			throw new RuntimeException("Failed to load " + GENE_JSON, e);
		}
	}

	private static void parse(InputStreamReader reader) {
		JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
		// weights
		JsonObject weights = root.has("rarity_weights") ? root.getAsJsonObject("rarity_weights") : new JsonObject();
		tierWeights.put("common", weights.has("common") ? weights.get("common").getAsInt() : 60);
		tierWeights.put("rare", weights.has("rare") ? weights.get("rare").getAsInt() : 33);
		tierWeights.put("exotic", weights.has("exotic") ? weights.get("exotic").getAsInt() : 7);
		// genes
		byTier.put("common", new ArrayList<>());
		byTier.put("rare", new ArrayList<>());
		byTier.put("exotic", new ArrayList<>());
		JsonArray arr = root.getAsJsonArray("genes");
		for (JsonElement el : arr) {
			JsonObject g = el.getAsJsonObject();
			String id = g.get("id").getAsString();
			String name = g.get("name").getAsString();
			String tier = g.get("tier").getAsString();
			float interference = g.get("interference").getAsFloat();
			String effect = g.get("effect").getAsString();
			GeneData data = new GeneData(id, name, tier, interference, effect);
			byTier.computeIfAbsent(tier, k -> new ArrayList<>()).add(data);
		}
		// safety: avoid empty lists
		byTier.forEach((k, v) -> {
			if (v.isEmpty())
				v.add(new GeneData("dummy", "Dummy", k, 0f, "No effect"));
		});
	}

	public static GeneData getRandomGene(Level level) {
		ensureLoaded(level);
		String tier = rollTier(level.getRandom());
		List<GeneData> pool = byTier.getOrDefault(tier, Collections.emptyList());
		if (pool.isEmpty())
			return null;
		return pool.get(level.getRandom().nextInt(pool.size()));
	}

	private static String rollTier(RandomSource rand) {
		int c = tierWeights.getOrDefault("common", 60);
		int r = tierWeights.getOrDefault("rare", 33);
		int e = tierWeights.getOrDefault("exotic", 7);
		int total = Math.max(1, c + r + e);
		int roll = rand.nextInt(total);
		if (roll < c)
			return "common";
		roll -= c;
		if (roll < r)
			return "rare";
		return "exotic";
	}
}