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

// rtc / rtc_extras
ALIASES.put("halite", "rtc_extras:halite");
ALIASES.put("tumor_mass", "rtc_extras:tumor_mass");

// LIQUIDS → frozen_ under rtc_extras
ALIASES.put("acetic_acid", "rtc_extras:frozen_acetic_acid");
ALIASES.put("methanol", "rtc_extras:frozen_methanol");
ALIASES.put("styrene", "rtc_extras:frozen_styrene");
ALIASES.put("lubricant", "rtc_extras:frozen_lubricant");
ALIASES.put("antifungal", "rtc_extras:frozen_antifungal");
ALIASES.put("xenoneurotoxin", "rtc_extras:frozen_xenoneurotoxin");

// solids / misc (keep as-is unless you decide otherwise)
ALIASES.put("antibiotic", "rtc_extras:antibiotic");
ALIASES.put("dopamine", "rtc_extras:frozen_dopamine");
ALIASES.put("oxygen_binding_protein", "rtc_extras:oxygen_binding_protein");
ALIASES.put("adrenaline_extract", "rtc_extras:adrenaline_extract");
ALIASES.put("polycarbonate_pellet", "rtc_extras:polycarbonate_pellet");

// pick one canonical id for nanotubes
ALIASES.put("carbon_nanotube", "rtc:item_carbon_nanotubes");

// if you want hexamine treated as “frozen”, keep this:
ALIASES.put("hexamine", "rtc_extras:frozen_hexamine");

// niter is a salt (solid) -> not frozen
ALIASES.put("niter", "rtc:niter");

// bio-ish
ALIASES.put("brain_matter", "rtc_extras:brain_matter");
ALIASES.put("prion", "rtc_extras:frozen_prion");

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