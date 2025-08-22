package net.crizo.rtcextras;

import org.checkerframework.checker.units.qual.g;

import java.util.Map;
import java.util.HashMap;

/**
 * Lightweight per-gene growth modifiers + merger.
 * Unknown gene IDs are neutral (no effect).
 */
public final class GeneGrowthData {
	/** Per-gene modifier record (relative to baseline). */
	public static final class GeneMod {
		public final double muMult; // growth rate multiplier
		public final double deathAdd; // +death per tick
		public final double yieldMult; // yield multiplier (cells per substrate unit)
		public final double wasteTolMult; // waste tolerance multiplier
		public final double wasteGenMult; // waste per grown cell multiplier
		public final double oxygenUseMult; // oxygen dependency multiplier
		public final double photoUnitsPerTick; // substrate units/tick if photosynthetic (will be scaled by light)
		public final double chemoUnitsPerTick; // substrate units/tick if chemoautotroph (no light required)
		public final boolean photosynthetic;
		public final boolean chemoautotroph;

		private GeneMod(Builder b) {
			this.muMult = b.muMult;
			this.deathAdd = b.deathAdd;
			this.yieldMult = b.yieldMult;
			this.wasteTolMult = b.wasteTolMult;
			this.wasteGenMult = b.wasteGenMult;
			this.oxygenUseMult = b.oxygenUseMult;
			this.photoUnitsPerTick = b.photoUnitsPerTick;
			this.chemoUnitsPerTick = b.chemoUnitsPerTick;
			this.photosynthetic = b.photosynthetic;
			this.chemoautotroph = b.chemoautotroph;
		}

		public static class Builder {
			double muMult = 1.0, deathAdd = 0.0, yieldMult = 1.0, wasteTolMult = 1.0, wasteGenMult = 1.0, oxygenUseMult = 1.0;
			double photoUnitsPerTick = 0.0, chemoUnitsPerTick = 0.0;
			boolean photosynthetic = false, chemoautotroph = false;

			public Builder mu(double v) {
				this.muMult = v;
				return this;
			}

			public Builder death(double v) {
				this.deathAdd = v;
				return this;
			}

			public Builder yield(double v) {
				this.yieldMult = v;
				return this;
			}

			public Builder wasteTol(double v) {
				this.wasteTolMult = v;
				return this;
			}

			public Builder wasteGen(double v) {
				this.wasteGenMult = v;
				return this;
			}

			public Builder oxygenUse(double v) {
				this.oxygenUseMult = v;
				return this;
			}

			public Builder photoUnits(double v) {
				this.photoUnitsPerTick = v;
				return this;
			}

			public Builder chemoUnits(double v) {
				this.chemoUnitsPerTick = v;
				return this;
			}

			public Builder photosynthetic() {
				this.photosynthetic = true;
				return this;
			}

			public Builder chemoautotroph() {
				this.chemoautotroph = true;
				return this;
			}

			public GeneMod build() {
				return new GeneMod(this);
			}
		}
	}

	/** Fully combined strain profile. */
	public static final class CombinedProfile {
		public double mu; // per-tick growth rate
		public double death; // per-tick death rate
		public double yield; // cells per substrate unit
		public double wasteTol; // higher = better
		public double wasteGen; // waste per cell grown
		public double oxygenUse; // 1=needs oxygen, 0=anaerobic
		public double photoUnitsPerTick; // substrate units/tick at full light
		public double chemoUnitsPerTick; // substrate units/tick regardless of light
	}

	private static final Map<String, GeneMod> MODS = new HashMap<>();
	public static final GeneMod NEUTRAL = new GeneMod.Builder().build();
	static {
		// Examples mapped from your genes list (expand anytime):
		MODS.put("photosynthesis", new GeneMod.Builder().photosynthetic().photoUnits(5.0).yield(1.05).build());
		MODS.put("chloroplast_optimization", new GeneMod.Builder().photosynthetic().photoUnits(3.5).mu(1.10).yield(1.10).build());
		MODS.put("fermentation_pathway", new GeneMod.Builder().mu(0.95).oxygenUse(0.6).wasteGen(1.25).build());
		MODS.put("detox_enzyme", new GeneMod.Builder().wasteTol(1.4).build());
		MODS.put("acetobacter_lineage", new GeneMod.Builder().mu(0.9).yield(1.1).wasteGen(1.2).build());
		MODS.put("lactate_cycle", new GeneMod.Builder().yield(1.05).wasteGen(1.15).build());
		MODS.put("methylotrophy", new GeneMod.Builder().mu(1.05).yield(0.95).build());
		MODS.put("basic_healing_factor", new GeneMod.Builder().death(-0.0002).build());
		MODS.put("heat_shock_protein", new GeneMod.Builder().death(-0.00015).build());
		MODS.put("cold_tolerance", new GeneMod.Builder().death(-0.00015).build());
		MODS.put("chemoautotrophy_pack", new GeneMod.Builder().chemoautotroph().chemoUnits(1.0).oxygenUse(0.8).build());
		// Add more when you want; unknown IDs are neutral.
	}

	public static GeneMod get(String id) {
		if (id == null || id.isEmpty())
			return NEUTRAL;
		return MODS.getOrDefault(id, NEUTRAL);
	}

	/** Merge up to 3 genes onto a baseline (mild diminishing returns). */
	public static CombinedProfile combine(Baseline base, String... geneIds) {
		CombinedProfile out = new CombinedProfile();
		out.mu = base.mu;
		out.death = base.death;
		out.yield = base.yield;
		out.wasteTol = base.wasteTol;
		out.wasteGen = base.wasteGen;
		out.oxygenUse = base.oxygenUse;
		out.photoUnitsPerTick = 0.0;
		out.chemoUnitsPerTick = 0.0;
		final double DR = 0.85; // diminishing exponent
		for (String id : geneIds) {
			GeneMod g = get(id);
			out.mu *= Math.pow(g.muMult, DR);
			out.yield *= Math.pow(g.yieldMult, DR);
			out.wasteTol *= Math.pow(g.wasteTolMult, DR);
			out.wasteGen *= Math.pow(g.wasteGenMult, DR);
			out.oxygenUse *= Math.pow(g.oxygenUseMult, DR);
			out.death += g.deathAdd;
			out.photoUnitsPerTick += g.photoUnitsPerTick;
			out.chemoUnitsPerTick += g.chemoUnitsPerTick;
		}
		// Sanity clamps
		out.mu = Math.max(0.0, out.mu);
		out.death = Math.max(0.0, out.death);
		out.yield = clamp(out.yield, 1.0, 500.0); // cells per unit
		out.wasteTol = clamp(out.wasteTol, 10.0, 1_000_000.0);
		out.wasteGen = clamp(out.wasteGen, 0.0, 1.0);
		out.oxygenUse = clamp(out.oxygenUse, 0.0, 2.0);
		return out;
	}

	private static double clamp(double v, double lo, double hi) {
		return v < lo ? lo : (v > hi ? hi : v);
	}

	/** Baseline species defaults. */
	public static final class Baseline {
		public final double mu; // per-tick
		public final double death; // per-tick
		public final double yield; // cells per substrate unit
		public final double wasteTol; // arbitrary units
		public final double wasteGen; // waste per grown cell
		public final double oxygenUse; // 1=needs oxygen

		public Baseline(double mu, double death, double yield, double wasteTol, double wasteGen, double oxygenUse) {
			this.mu = mu;
			this.death = death;
			this.yield = yield;
			this.wasteTol = wasteTol;
			this.wasteGen = wasteGen;
			this.oxygenUse = oxygenUse;
		}

		/** Balanced default tuned for slow, growable colonies. */
		public static Baseline balanced() {
			return new Baseline(0.0005, // mu ≈ +1.8×/min if unconstrained
					0.0002, // death
					50.0, // 50 cells per substrate unit
					200.0, // waste tolerance
					0.00005, // waste per grown cell (very gentle)
					1.0 // fully oxygen dependent
			);
		}
	}

	private GeneGrowthData() {
	}
}