package net.crizo.rtcextras;

public final class GeneAPI {
	private static final String KEY_ACTIVE_GENES_CSV = "rtc_active_genes_csv";

	private static String fq(String id) {
		return id.indexOf(':') >= 0 ? id : "rtc_extras:" + id;
	}

	public static java.util.Map<String, Integer> getLevels(net.minecraft.world.entity.player.Player p) {
		// same logic as GeneEffects.parseActiveGeneLevelsFromCsv but public
		var out = new java.util.HashMap<String, Integer>();
		String csv = null;
		try {
			var vars = p.getData(net.crizo.rtcextras.network.RtcExtrasModVariables.PLAYER_VARIABLES);
			csv = vars != null ? vars.geneContent : null; // <-- your player-var field
		} catch (Throwable ignored) {
		}
		if (csv == null || csv.isBlank()) {
			csv = p.getPersistentData().getString(KEY_ACTIVE_GENES_CSV);
		}
		if (csv != null && !csv.isBlank()) {
			for (String raw : csv.split(",")) {
				var tok = raw.trim();
				if (!tok.isEmpty())
					out.merge(fq(tok), 1, Integer::sum);
			}
		}
		return out;
	}

	public static void setCsv(net.minecraft.world.entity.player.Player p, String csv) {
		boolean wrote = false;
		try {
			var vars = p.getData(net.crizo.rtcextras.network.RtcExtrasModVariables.PLAYER_VARIABLES);
			if (vars != null) {
				vars.geneContent = csv == null ? "" : csv;
				try {
					vars.syncPlayerVariables(p);
				} catch (Throwable ignored) {
				}
				wrote = true;
			}
		} catch (Throwable ignored) {
		}
		if (!wrote)
			p.getPersistentData().putString(KEY_ACTIVE_GENES_CSV, csv == null ? "" : csv);
		// optional: run cleanup for attribute-based genes when levels may have dropped to 0
		net.crizo.rtcextras.GeneEffects.postSetCsvCleanup(p);
	}

	public static void add(net.minecraft.world.entity.player.Player p, String id, int count) {
		count = Math.max(1, count);
		var levels = new java.util.LinkedHashMap<>(getLevels(p));
		levels.merge(fq(id), count, Integer::sum);
		setCsv(p, toCsv(levels));
	}

	public static void remove(net.minecraft.world.entity.player.Player p, String id, int count) {
		count = Math.max(1, count);
		var fqid = fq(id);
		var levels = new java.util.LinkedHashMap<>(getLevels(p));
		int cur = levels.getOrDefault(fqid, 0);
		if (cur <= count)
			levels.remove(fqid);
		else
			levels.put(fqid, cur - count);
		setCsv(p, toCsv(levels));
	}

	public static void clear(net.minecraft.world.entity.player.Player p) {
		setCsv(p, "");
	}

	private static String toCsv(java.util.Map<String, Integer> levels) {
		var sb = new StringBuilder();
		boolean first = true;
		for (var e : levels.entrySet()) {
			for (int i = 0; i < e.getValue(); i++) {
				if (!first)
					sb.append(", ");
				sb.append(e.getKey()); // store fq ids; GeneEffects accepts bare or fq
				first = false;
			}
		}
		return sb.toString();
	}
}