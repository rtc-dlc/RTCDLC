package net.crizo.rtcextras;

public class GeneSpec {
	public String id;
	public String name;
	public String tier; // "common" | "rare" | "exotic"
	public double interference;
	public String effect; // flavor text
	public java.util.List<String> tags; // ["player","production",...]
	public java.util.List<String> production; // item ids OR bare names ("string", "halite", ...)
	// convenience

	public boolean isPlayerGene() {
		return tags != null && tags.contains("player");
	}

	public boolean isProductionGene() {
		return production != null && !production.isEmpty();
	}
}