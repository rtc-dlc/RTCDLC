// PerishableStacks.java
package net.crizo.rtcextras;

import org.checkerframework.checker.units.qual.t;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;

public final class PerishableStacks {
	// Keys (same as your bacterium)
	public static final String TAG_CREATED = "rtc_created"; // long ticks
	public static final String TAG_STABILIZED = "rtc_stabilized"; // boolean
	public static final String TAG_FROZEN_LEFT = "rtc_frozen_left"; // long ticks left (while stabilized)
	public static final String TAG_IS_PERISHABLE = "rtc_perishable"; // optional bool switch on a stack
	// Global tag so you can mark ANY item in data packs: data/rtc_extras/tags/items/perishable.json
	public static final TagKey<net.minecraft.world.item.Item> PERISHABLE_TAG = TagKey.create(Registries.ITEM, ResourceLocation.parse("rtc_extras:perishable"));
	// Lifetime: full stack (64) survives this long (2 MC days). You can override per-stack if you want.
	public static final int STACK_UNIT = 64;
	public static final long FULL_STACK_LIFETIME_TICKS = 2L * 24000L; // 2 MC days
	public static final String TAG_PER_ITEM_TICKS = "rtc_per_item_ticks"; // optional long override

	private PerishableStacks() {
	}

	// ----- CustomData helpers -----
	public static net.minecraft.nbt.CompoundTag getTag(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	public static void setTag(ItemStack stack, net.minecraft.nbt.CompoundTag tag) {
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	// Mark a particular stack perishable at runtime (optional—usually use the item tag).
	public static void markPerishable(ItemStack stack, boolean v) {
		var t = getTag(stack);
		t.putBoolean(TAG_IS_PERISHABLE, v);
		setTag(stack, t);
	}

	// Optional: per-stack override for lifetime speed (ticks contributed by 1 item in the stack).
	public static void setPerItemLifetimeTicks(ItemStack stack, long ticks) {
		var t = getTag(stack);
		t.putLong(TAG_PER_ITEM_TICKS, Math.max(1L, ticks));
		setTag(stack, t);
	}

	public static boolean isPerishable(ItemStack stack) {
		if (stack.isEmpty())
			return false;
		// Either in datapack tag OR explicitly flagged on the stack
		if (stack.is(PERISHABLE_TAG))
			return true;
		return getTag(stack).getBoolean(TAG_IS_PERISHABLE);
	}

	public static void ensureCreated(Level level, ItemStack stack) {
		var t = getTag(stack);
		if (!t.contains(TAG_CREATED)) {
			t.putLong(TAG_CREATED, level.getGameTime());
			setTag(stack, t);
		}
	}

	public static long perItemTicks(ItemStack stack) {
		var t = getTag(stack);
		long override = t.getLong(TAG_PER_ITEM_TICKS);
		if (override > 0)
			return override;
		// default scales the same as your bacterium item
		return FULL_STACK_LIFETIME_TICKS / STACK_UNIT;
	}

	private static long expiryTicksFor(ItemStack stack, long created, long perItem) {
		int count = Math.max(1, stack.getCount());
		return created + perItem * count;
	}

	private static long timeLeftTicksUnfrozen(Level level, ItemStack stack) {
		long now = level.getGameTime();
		var t = getTag(stack);
		long created = t.getLong(TAG_CREATED);
		long per = perItemTicks(stack);
		if (created == 0L)
			return per * Math.max(1, stack.getCount());
		long expiry = expiryTicksFor(stack, created, per);
		return Math.max(0L, expiry - now);
	}

	public static long timeLeftTicks(Level level, ItemStack stack) {
		var t = getTag(stack);
		if (t.getBoolean(TAG_STABILIZED) && t.contains(TAG_FROZEN_LEFT)) {
			return Math.max(0L, t.getLong(TAG_FROZEN_LEFT));
		}
		return timeLeftTicksUnfrozen(level, stack);
	}

	public static boolean isExpired(Level level, ItemStack stack) {
		var t = getTag(stack);
		long created = t.getLong(TAG_CREATED);
		if (created == 0L)
			return false;
		long now = level.getGameTime();
		return now >= expiryTicksFor(stack, created, perItemTicks(stack));
	}

	/** Call each tick to progress decay for this stack. */
	public static void tick(Level level, Entity holder, ItemStack stack, Runnable onExpire) {
		if (level.isClientSide() || stack.isEmpty())
			return;
		ensureCreated(level, stack);
		var t = getTag(stack);
		// Stabilization policy: disable while in a player's inventory
		boolean isPlayerInv = holder instanceof Player;
		if (isPlayerInv && t.getBoolean(TAG_STABILIZED)) {
			t.putBoolean(TAG_STABILIZED, false);
			setTag(stack, t);
		}
		boolean stabilized = getTag(stack).getBoolean(TAG_STABILIZED);
		if (stabilized) {
			// First time becoming stabilized? capture remaining time.
			if (!t.contains(TAG_FROZEN_LEFT)) {
				long remaining = timeLeftTicksUnfrozen(level, stack);
				t.putLong(TAG_FROZEN_LEFT, remaining);
				setTag(stack, t);
			}
			// While stabilized, skip ticking/expiry.
			return;
		} else {
			// If coming FROM stabilized, re-anchor creation so countdown resumes correctly.
			if (t.contains(TAG_FROZEN_LEFT)) {
				long frozenLeft = Math.max(0L, t.getLong(TAG_FROZEN_LEFT));
				long now = level.getGameTime();
				long per = perItemTicks(stack) * Math.max(1, stack.getCount());
				long newCreated = now + frozenLeft - per;
				t.remove(TAG_FROZEN_LEFT);
				t.putLong(TAG_CREATED, newCreated);
				setTag(stack, t);
			}
		}
		// Expire?
		if (isExpired(level, stack)) {
			if (onExpire != null)
				onExpire.run();
			stack.setCount(0);
		}
	}

	// Helpers for machines
	public static void stabilizeInMachine(Level level, ItemStack stack) {
		var t = getTag(stack);
		if (!t.getBoolean(TAG_STABILIZED)) {
			t.putBoolean(TAG_STABILIZED, true);
			// capture remaining if not present yet
			if (!t.contains(TAG_FROZEN_LEFT)) {
				long left = timeLeftTicksUnfrozen(level, stack);
				t.putLong(TAG_FROZEN_LEFT, left);
			}
			setTag(stack, t);
		}
	}

	public static void destablizeFromMachine(Level level, ItemStack stack) {
		var t = getTag(stack);
		if (t.getBoolean(TAG_STABILIZED)) {
			t.putBoolean(TAG_STABILIZED, false);
			setTag(stack, t);
		}
		// countdown will resume on next tick() call
	}
}