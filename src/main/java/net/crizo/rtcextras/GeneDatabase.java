package net.crizo.rtcextras;

import org.checkerframework.checker.units.qual.g;

import org.apache.logging.log4j.core.util.Loader;

@net.neoforged.fml.common.EventBusSubscriber(bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public final class GeneDatabase {
	private static final java.util.Map<net.minecraft.resources.ResourceLocation, GeneSpec> MAP = new java.util.HashMap<>();
	private static GenePack lastPack;

	public static void clear() {
		MAP.clear();
		lastPack = null;
	}

	public static void put(GeneSpec spec) {
		if (spec == null || spec.id == null || spec.id.isBlank())
			return;
		try {
			var rl = net.minecraft.resources.ResourceLocation.parse(spec.id.contains(":") ? spec.id : "rtc_extras:" + spec.id);
			MAP.put(rl, spec);
		} catch (Exception ignored) {
		}
	}

	public static GeneSpec get(net.minecraft.resources.ResourceLocation id) {
		return MAP.get(id);
	}

	public static java.util.Collection<GeneSpec> all() {
		return MAP.values();
	}

	public static GenePack lastLoadedPack() {
		return lastPack;
	}

	// --- Loaders ---
	public static void loadFromJsonString(String json) {
		try {
			var gson = new com.google.gson.GsonBuilder().create();
			var pack = gson.fromJson(json, GenePack.class);
			ingestPack(pack);
		} catch (Exception e) {
			// log if you want
		}
	}

	public static void loadFromResources(net.minecraft.server.packs.resources.ResourceManager rm) {
		clear();
		var gson = new com.google.gson.GsonBuilder().create();
		// Support: either one file at rtc_extras:genes/genes.json, or multiple under rtc_extras:genes/*.json
		try {
			var res = rm.getResource(net.minecraft.resources.ResourceLocation.parse("rtc_extras:genes/genes.json"));
			if (res.isPresent()) {
				try (var is = res.get().open(); var r = new java.io.InputStreamReader(is)) {
					ingestPack(gson.fromJson(r, GenePack.class));
					return;
				}
			}
		} catch (Exception ignored) {
		}
		// fallback: scan folder
		try {
			var all = rm.listResources("genes", p -> p.getPath().endsWith(".json"));
			for (var entry : all.entrySet()) {
				try (var is = entry.getValue().open(); var r = new java.io.InputStreamReader(is)) {
					var pack = gson.fromJson(r, GenePack.class);
					ingestPack(pack);
				} catch (Exception ignored) {
				}
			}
		} catch (Exception ignored) {
		}
	}

	private static void ingestPack(GenePack pack) {
		if (pack == null)
			return;
		lastPack = pack;
		MAP.clear();
		if (pack.genes != null)
			for (var g : pack.genes)
				put(g);
	}

	// Hook into data pack reloads
	@net.neoforged.bus.api.SubscribeEvent
	public static void onReload(net.neoforged.neoforge.event.AddReloadListenerEvent e) {
		e.addListener(new Loader());
	}

	private static class Loader extends net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener {
		Loader() {
			super(new com.google.gson.GsonBuilder().create(), "genes");
		}

		@Override
		protected void apply(java.util.Map<net.minecraft.resources.ResourceLocation, com.google.gson.JsonElement> map, net.minecraft.server.packs.resources.ResourceManager resourceManager, net.minecraft.util.profiling.ProfilerFiller profiler) {
			// Let our own helper scan either one file (genes/genes.json) or many under genes/*.json
			loadFromResources(resourceManager);
		}
	}
}