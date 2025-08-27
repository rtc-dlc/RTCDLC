package net.crizo.rtcextras;

public final class GeneEngine {
	public enum Mode {
		DECAY, PRODUCTION, IN_BODY
	}

	public record Ctx(net.minecraft.world.level.Level level, double x, double y, double z, net.minecraft.util.RandomSource rng, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.Entity source,
			@org.jetbrains.annotations.Nullable net.minecraft.world.level.block.entity.BlockEntity be, @org.jetbrains.annotations.Nullable net.minecraft.world.item.ItemStack stack, int stackCount) {
	}

	// Tuning knobs in one place
	public static double DECAY_P_PER_ITEM = 0.05; // 5%/item stochastic drop chance (if not overridden later)
	public static double PRODUCTION_RATE_PER_ITEM = 0.02; // per-tick deterministic average in machines
	public static boolean DROP_WASTE_WHEN_EMPTY = true;

	public static void apply(String geneIdRaw, Mode mode, Ctx c) {
		if (geneIdRaw == null || geneIdRaw.isBlank())
			return;
		net.minecraft.resources.ResourceLocation id;
		try {
			id = net.minecraft.resources.ResourceLocation.parse(geneIdRaw.contains(":") ? geneIdRaw : "rtc_extras:" + geneIdRaw);
		} catch (Exception e) {
			return;
		}
		var spec = GeneDatabase.get(id);
		if (spec == null)
			return;
		switch (mode) {
			case DECAY -> runDecay(spec, c);
			case PRODUCTION -> runProduction(spec, c);
			case IN_BODY -> GeneEffects.dispatchInBody(spec, c);
		}
	}

	private static void runDecay(GeneSpec spec, Ctx c) {
		if (c.level().isClientSide())
			return;
		// If JSON defines products, do stochastic per-item yields; otherwise optional waste fallback
		var produced = 0;
		if (spec.production != null && !spec.production.isEmpty()) {
			for (var pid : spec.production) {
				var item = ItemIO.resolveItem(pid);
				if (item == null)
					continue;
				int drops = 0;
				for (int i = 0; i < c.stackCount(); i++)
					if (c.rng().nextDouble() < DECAY_P_PER_ITEM)
						drops++;
				if (drops > 0) {
					ItemIO.spawnStacked(c.level(), c.x(), c.y(), c.z(), item, drops);
					produced += drops;
				}
			}
		}
		if (produced == 0 && DROP_WASTE_WHEN_EMPTY) {
			// drop 1 waste per 5 items (rounded)
			int waste = Math.max(0, (int) Math.floor(c.stackCount() * 0.2));
			ItemIO.spawnWasteIfEmpty(c.level(), c.x(), c.y(), c.z(), waste);
		}
		// Side-effects on decay can be layered here by tag/name:
		GeneEffects.dispatchOnDecay(spec, c);
	}

	private static void runProduction(GeneSpec spec, Ctx c) {
		if (c.level().isClientSide())
			return;
		int totalProduced = 0;
		if (spec.production != null && !spec.production.isEmpty()) {
			for (var pid : spec.production) {
				var item = ItemIO.resolveItem(pid);
				if (item == null)
					continue;
				double expect = c.stackCount() * PRODUCTION_RATE_PER_ITEM;
				int base = (int) Math.floor(expect);
				if (c.rng().nextDouble() < (expect - base))
					base++;
				if (base > 0) {
					ItemIO.spawnStacked(c.level(), c.x(), c.y(), c.z(), item, base);
					totalProduced += base;
				}
			}
		}
		// Machine-side special NBT flags or counters:
		GeneEffects.dispatchOnProduction(spec, c, totalProduced);
	}
}