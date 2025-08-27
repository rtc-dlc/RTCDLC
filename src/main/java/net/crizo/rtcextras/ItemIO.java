package net.crizo.rtcextras;

import org.checkerframework.checker.units.qual.s;

public final class ItemIO {
	public static final net.minecraft.resources.ResourceLocation WASTE_ITEM_ID = net.minecraft.resources.ResourceLocation.parse("rtc_extras:waste_biomass");
	// Optional aliases so your JSON can stay short & pretty
	private static final java.util.Map<String, String> ALIASES = new java.util.HashMap<>();
	static {
		// vanilla
		ALIASES.put("string", "minecraft:string");
		ALIASES.put("sugar", "minecraft:sugar");
		ALIASES.put("slime_ball", "minecraft:slime_ball");
		// rtc / rtc_extras common ids from your JSON
		ALIASES.put("halite", "rtc_extras:halite");
		ALIASES.put("tumor_mass", "rtc_extras:tumor_mass");
		ALIASES.put("luciferin_basic", "rtc_extras:luciferin_basic");
		ALIASES.put("lactic_acid", "rtc_extras:lactic_acid");
		ALIASES.put("acetic_acid", "rtc_extras:acetic_acid");
		ALIASES.put("methanol", "rtc_extras:frozen_methanol");
		ALIASES.put("antibiotic", "rtc_extras:antibiotic");
		ALIASES.put("chitin_flakes", "rtc_extras:chitin_flakes");
		ALIASES.put("coagulation_factor", "rtc_extras:coagulation_factor");
		ALIASES.put("dopamine_precursor", "rtc_extras:dopamine_precursor");
		ALIASES.put("oxygen_binding_protein", "rtc_extras:oxygen_binding_protein");
		ALIASES.put("adrenaline_extract", "rtc_extras:adrenaline_extract");
		ALIASES.put("venom_peptide", "rtc_extras:venom_peptide");
		ALIASES.put("polycarbonate_pellet", "rtc_extras:polycarbonate_pellet");
		// You had both “carbon_nanotube(s)” historically—map both to whichever you ship:
		ALIASES.put("carbon_nanotube", "rtc:item_carbon_nanotubes"); // change to rtc_extras:carbon_nanotube if that's your id
		ALIASES.put("hydrazine", "rtc_extras:hydrazine");
		ALIASES.put("hexamine", "rtc_extras:hexamine");
		ALIASES.put("tetracycline_like", "rtc_extras:tetracycline_like");
		ALIASES.put("streptomycin_like", "rtc_extras:streptomycin_like");
		ALIASES.put("styrene", "rtc:styrene"); // or rtc_extras:styrene
		ALIASES.put("lubricant", "rtc_extras:lubricant");
		ALIASES.put("antifungal", "rtc_extras:antifungal");
		ALIASES.put("niter", "rtc:niter"); // or rtc_extras:niter if that's the id
		ALIASES.put("symbiotic_root_mat", "rtc_extras:symbiotic_root_mat");
		ALIASES.put("brain_matter", "rtc_extras:brain_matter");
		ALIASES.put("xenoneurotoxin", "rtc_extras:xenoneurotoxin");
		ALIASES.put("prion", "rtc_extras:prion");
	}

	public static net.minecraft.world.item.Item resolveItem(String idOrBare) {
		if (idOrBare == null || idOrBare.isBlank())
			return null;
		// 1) alias match
		String aliased = ALIASES.getOrDefault(idOrBare, idOrBare);
		// 2) try as fully qualified, else fall back through your common modids
		java.util.List<String> tries = new java.util.ArrayList<>();
		if (aliased.indexOf(':') >= 0)
			tries.add(aliased);
		else {
			tries.add("rtc_extras:" + aliased);
			tries.add("rtc:" + aliased);
			tries.add("minecraft:" + aliased);
		}
		for (var s : tries) {
			try {
				var rl = net.minecraft.resources.ResourceLocation.parse(s);
				var it = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
				if (it != null)
					return it;
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	public static void spawnStacked(net.minecraft.world.level.Level level, double x, double y, double z, net.minecraft.world.item.Item item, int total) {
		if (item == null || total <= 0)
			return;
		int max = item.getDefaultMaxStackSize();
		while (total > 0) {
			int n = Math.min(max, total);
			level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, x, y, z, new net.minecraft.world.item.ItemStack(item, n)));
			total -= n;
		}
	}

	public static void spawnWasteIfEmpty(net.minecraft.world.level.Level level, double x, double y, double z, int total) {
		if (total <= 0)
			return;
		var waste = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(WASTE_ITEM_ID).orElse(null);
		spawnStacked(level, x, y, z, waste, total);
	}
}