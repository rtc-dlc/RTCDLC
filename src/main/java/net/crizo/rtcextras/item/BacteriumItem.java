package net.crizo.rtcextras.item;

import org.checkerframework.checker.units.qual.t;
import org.checkerframework.checker.units.qual.s;
import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.h;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;

import java.util.Properties;
import java.util.List;

public class BacteriumItem extends Item {
	public static final String TAG_CREATED = "rtc_created"; // long ticks
	private static final String TAG_RESEARCHED = "researched"; // existing
	private static final String TAG_STABILIZED = "rtc_stabilized"; // NEW: bool
	private static final String TAG_FROZEN_LEFT = "rtc_frozen_left"; // NEW: long remaining ticks while stabilized
	// ===== Scaling lifetime by stack size =====
	private static final int STACK_UNIT = 64; // full stack size
	private static final long FULL_STACK_LIFETIME_TICKS = 2L * 2400L; // 2 MC days = 48,000 ticks
	private static final long PER_ITEM_TICKS = FULL_STACK_LIFETIME_TICKS / STACK_UNIT; // 750 ticks (~37.5s)

	public BacteriumItem(ResourceLocation ignored) {
		this(new Properties().stacksTo(64));
	}

	public BacteriumItem(Properties props) {
		super(props);
	}

	// ---- Data helpers ----
	public static CompoundTag getTag(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	public static void setTag(ItemStack stack, CompoundTag tag) {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static void ensureCreated(Level level, ItemStack stack) {
		var t = getTag(stack);
		if (!t.contains(TAG_CREATED)) {
			t.putLong(TAG_CREATED, level.getGameTime());
			setTag(stack, t);
		}
	}

	public static void setCreated(ItemStack stack, long createdTicks) {
		var t = getTag(stack);
		t.putLong(TAG_CREATED, createdTicks);
		setTag(stack, t);
	}

	// ---- Lifetime math (depends only on created + current count) ----
	private static long expiryTicksFor(ItemStack stack, long created) {
		int count = Math.max(1, stack.getCount());
		return created + PER_ITEM_TICKS * count;
	}

	/*
	private static long timeLeftTicks(Level level, ItemStack stack) {
		long now = level.getGameTime();
		long created = getTag(stack).getLong(TAG_CREATED);
		if (created == 0L)
			return PER_ITEM_TICKS * Math.max(1, stack.getCount());
		long expiry = expiryTicksFor(stack, created);
		return Math.max(0L, expiry - now);
	}
	*/
	private static long timeLeftTicks(Level level, ItemStack stack) {
		var tag = getTag(stack);
		if (tag.getBoolean(TAG_STABILIZED) && tag.contains(TAG_FROZEN_LEFT)) {
			return Math.max(0L, tag.getLong(TAG_FROZEN_LEFT)); // frozen amount, doesn’t tick down
		}
		// normal, unfrozen countdown
		return timeLeftTicksUnfrozen(level, stack);
	}

	/** Time left if the item were NOT stabilized (purely created+count math). */
	private static long timeLeftTicksUnfrozen(Level level, ItemStack stack) {
		long now = level.getGameTime();
		long created = getTag(stack).getLong(TAG_CREATED);
		if (created == 0L)
			return PER_ITEM_TICKS * Math.max(1, stack.getCount());
		long expiry = expiryTicksFor(stack, created);
		return Math.max(0L, expiry - now);
	}

	public static boolean isExpired(Level level, ItemStack stack) {
		long created = getTag(stack).getLong(TAG_CREATED);
		if (created == 0L)
			return false;
		long now = level.getGameTime();
		return now >= expiryTicksFor(stack, created);
	}

	// ---- Kill whole stack when it expires (inventory only) ----
	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
		if (!level.isClientSide()) {
			ensureCreated(level, stack);
			var tag = getTag(stack);
			// ==== Decide stabilized vs destabilized ====
			// If in a Player's inventory, we force DESTABILIZED; your special block should set TAG_STABILIZED=true on its own.
			boolean wasStabilized = tag.getBoolean(TAG_STABILIZED);
			boolean isPlayerInventory = (entity instanceof Player);
			if (isPlayerInventory) {
				// force off in player inventory
				if (wasStabilized) {
					tag.putBoolean(TAG_STABILIZED, false);
					setTag(stack, tag);
				}
			}
			boolean stabilized = getTag(stack).getBoolean(TAG_STABILIZED); // re-read in case we modified it
			// ==== Transition handling ====
			if (stabilized) {
				// If we just became stabilized (no frozen value recorded yet), capture remaining time once
				if (!tag.contains(TAG_FROZEN_LEFT)) {
					long remaining = timeLeftTicksUnfrozen(level, stack);
					tag.putLong(TAG_FROZEN_LEFT, remaining);
					setTag(stack, tag);
				}
				// While stabilized, do NOT tick down; skip expiry.
			} else {
				// If we were stabilized previously and have a frozen amount, resume from there
				if (tag.contains(TAG_FROZEN_LEFT)) {
					long frozenLeft = Math.max(0L, tag.getLong(TAG_FROZEN_LEFT));
					// Re-anchor TAG_CREATED so that: now + frozenLeft = created + PER_ITEM_TICKS * count
					long now = level.getGameTime();
					long per = PER_ITEM_TICKS * Math.max(1, stack.getCount());
					long newCreated = now + frozenLeft - per;
					tag.remove(TAG_FROZEN_LEFT);
					tag.putLong(TAG_CREATED, newCreated);
					setTag(stack, tag);
				}
				// Normal decay & death (only when not stabilized)
				if (entity instanceof Player) {
					if (isExpired(level, stack)) {
						// spawn decay loot first
						net.crizo.rtcextras.procedures.GeneDecayLootProcedure.onBacteriumDecay(level, entity, stack);
						// then kill the cohort
						stack.setCount(0);
					}
				}
			}
		}
		super.inventoryTick(stack, level, entity, slotId, selected);
	}

	// ===== Durability-style bar on the item =====
	@Override
	public boolean isBarVisible(ItemStack stack) {
		return true; // always show
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		// width 0..13
		Level level = net.minecraft.client.Minecraft.getInstance().level; // safe client only; server never calls this
		if (level == null)
			return 13;
		long left = timeLeftTicks(level, stack);
		long total = PER_ITEM_TICKS * Math.max(1, stack.getCount());
		float frac = total <= 0 ? 0f : (left / (float) total);
		frac = Mth.clamp(frac, 0f, 1f);
		return Math.round(frac * 13f);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		// green (good) -> red (bad)
		Level level = net.minecraft.client.Minecraft.getInstance().level;
		if (level == null)
			return 0x00FF00;
		long left = timeLeftTicks(level, stack);
		long total = PER_ITEM_TICKS * Math.max(1, stack.getCount());
		float frac = total <= 0 ? 0f : (left / (float) total);
		frac = Mth.clamp(frac, 0f, 1f);
		// hue: 0.33 (green) -> 0.0 (red)
		float hue = 0.33f * frac; // 0.33 ~ green, 0 ~ red
		return Mth.hsvToRgb(hue, 1.0f, 1.0f);
	}

	// Build a [||||||||||||||||||||] style bar, colored.
	// frac: 0..1 fraction remaining ; width: segment count (e.g., 20)
	private static MutableComponent coloredBarComponent(float frac, int width) {
		frac = Mth.clamp(frac, 0f, 1f);
		int filled = Mth.clamp(Math.round(frac * width), 0, width);
		// Colors
		final int GREY = 0x777777; // for empty segments + brackets
		MutableComponent line = Component.literal("").append(Component.literal("[").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(GREY))));
		// Left→right gradient for filled segments: green(120°) → yellow(60°) → red(0°)
		for (int i = 0; i < width; i++) {
			if (i < filled) {
				float pos = (float) i / (float) (width - 1); // 0..1 across the bar
				float hue = 0.33f * (1f - pos); // 0.33≈120°(green) → 0(red)
				int rgb = Mth.hsvToRgb(hue, 1f, 1f);
				line.append(Component.literal("|").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb))));
			} else {
				line.append(Component.literal("|").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(GREY))));
			}
		}
		line.append(Component.literal("]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(GREY))));
		return line;
	}

	// ===== Tooltip health bar + conditional genes =====
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
		//tip.add(Component.literal("[Bacterium]"));
		var tag = getTag(stack);
		boolean stabilized = tag.getBoolean(TAG_STABILIZED);
		tip.add(Component.literal("Status: " + (stabilized ? "Stabilized" : "Destabilized")).withStyle(stabilized ? net.minecraft.ChatFormatting.AQUA : net.minecraft.ChatFormatting.YELLOW));
		// Time bar & time left (always shown)
		if (ctx.level() != null) {
			long left = timeLeftTicks(ctx.level(), stack);
			long total = PER_ITEM_TICKS * Math.max(1, stack.getCount());
			float frac = total <= 0 ? 0f : (left / (float) total);
			long leftSec = left / 20L;
			tip.add(Component.literal("Time left: " + formatTime(leftSec)));
			tip.add(coloredBarComponent(frac, 20));
		} else {
			tip.add(Component.literal("Time left: ~unknown (client world null)"));
		}
		// Special info (genes etc.) is gated by a hidden boolean "researched"
		boolean researched = tag.getBoolean(TAG_RESEARCHED);
		if (researched) {
			// Gene lines (debug-friendly)
			if (tag.contains("rtc_gene_id")) {
				String tier = tag.getString("rtc_gene_tier");
				String name = tag.getString("rtc_gene_name");
				String effect = tag.getString("rtc_gene_effect");
				float interf = tag.getFloat("rtc_gene_interference");
				String id = tag.getString("rtc_gene_id");
				int color = switch (tier) {
					case "common" -> 0xFFFF55; // yellow
					case "rare" -> 0xFF8888; // light red
					case "exotic" -> 0xAA00FF; // purple
					default -> 0xFFFFFF;
				};
				tip.add(Component.literal("Gene: " + name).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
				tip.add(Component.literal("Tier: " + tier).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
				tip.add(Component.literal(effect).withStyle(net.minecraft.ChatFormatting.GRAY));
				// Debug lines:
				tip.add(Component.literal("ID: " + id).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
				tip.add(Component.literal("Interference: " + interf).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
			}
		}
		// else: do not show special gene info when unresearched
		super.appendHoverText(stack, ctx, tip, flag);
	}

	private static String formatTime(long sec) {
		if (sec < 60)
			return sec + "s";
		long m = sec / 60;
		long s = sec % 60;
		if (m < 60)
			return m + "m " + s + "s";
		long h = m / 60;
		m = m % 60;
		return h + "h " + m + "m";
	}
}