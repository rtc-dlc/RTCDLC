package net.crizo.rtcextras;

import org.checkerframework.checker.units.qual.t;
import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;

import java.util.Objects;

import org.checkerframework.checker.units.qual.s;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;

import net.crizo.rtcextras.PerishableMerge;

import java.util.Objects;

/**
 * Utilities to identify perishable stacks and to MERGE their timers on demand.
 * Works with any item: your own classes or tagged vanilla/mod items.
 */
public final class PerishableMerge {
	// Perishable keys (match your BacteriumItem)
	public static final String TAG_CREATED = "rtc_created";
	public static final String TAG_STABILIZED = "rtc_stabilized";
	public static final String TAG_FROZEN_LEFT = "rtc_frozen_left";
	// Same lifetime math as your BacteriumItem
	private static final int STACK_UNIT = 64;
	private static final long FULL_STACK_LIFETIME_TICKS = 2L * 2400L; // 2 MC days
	private static final long PER_ITEM_TICKS = FULL_STACK_LIFETIME_TICKS / STACK_UNIT; // 750
	// Optional: a datapack tag to mark non-subclass items as perishable
	private static final TagKey<Item> TAG_PERISHABLE = TagKey.create(Registries.ITEM, ResourceLocation.parse("rtc_extras:perishable"));

	private PerishableMerge() {
	}

	/** Is this stack perishable? (subclass OR tag OR already has our component keys) */
	public static boolean isPerishable(ItemStack s) {
		if (s.isEmpty())
			return false;
		// already has our perishable component data?
		CompoundTag t = getTagCopy(s);
		if (t.contains(TAG_CREATED) || t.contains(TAG_FROZEN_LEFT) || t.contains(TAG_STABILIZED))
			return true;
		// tagged via datapack?
		if (s.is(TAG_PERISHABLE))
			return true;
		// or your custom class hierarchy could be checked here if you prefer:
		// return (s.getItem() instanceof PerishableItemBase);
		return false;
	}

	/** Return a *copy* of the CustomData tag (safe to mutate). */
	public static CompoundTag getTagCopy(ItemStack s) {
		return s.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	/** Write a tag back to the stack’s CustomData. */
	public static void setTag(ItemStack s, CompoundTag t) {
		s.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
	}

	/** Remove the perishable fields so we can compare the rest for “same-ness”. */
	private static CompoundTag stripPerishableKeys(CompoundTag t) {
		if (t == null)
			return new CompoundTag();
		CompoundTag c = t.copy();
		c.remove(TAG_CREATED);
		c.remove(TAG_FROZEN_LEFT);
		c.remove(TAG_STABILIZED);
		return c;
	}

	/**
	 * Are these two stacks allowed to merge (same item + same *non-perishable* NBT)?
	 * This naturally enforces “same gene/variant” because those fields remain in NBT.
	 */
	public static boolean stacksMergeable(ItemStack a, ItemStack b) {
		if (a.isEmpty() || b.isEmpty())
			return false;
		if (!ItemStack.isSameItem(a, b))
			return false;
		CompoundTag ta = stripPerishableKeys(getTagCopy(a));
		CompoundTag tb = stripPerishableKeys(getTagCopy(b));
		return Objects.equals(ta, tb);
	}

	/** “Freshness credit” for the whole stack, in ticks (conserved when merging). */
	private static long freshnessUnits(Level lvl, ItemStack s) {
		CompoundTag t = getTagCopy(s);
		long left;
		if (t.getBoolean(TAG_STABILIZED) && t.contains(TAG_FROZEN_LEFT)) {
			left = Math.max(0L, t.getLong(TAG_FROZEN_LEFT));
		} else {
			long now = lvl.getGameTime();
			long created = t.getLong(TAG_CREATED);
			if (created == 0L) {
				return PER_ITEM_TICKS * Math.max(1, s.getCount());
			}
			long expiry = created + PER_ITEM_TICKS * Math.max(1, s.getCount());
			left = Math.max(0L, expiry - now);
		}
		return left;
	}

	/**
	 * Merge as much of `incoming` into `target` as fits, *conserving* freshness.
	 * Ensures perishable fields on the combined stack are normalized & destabilized.
	 */
	public static void mergeIntoTarget(Level lvl, ItemStack target, ItemStack incoming) {
		if (incoming.isEmpty() || target.isEmpty())
			return;
		int room = Math.min(incoming.getCount(), target.getMaxStackSize() - target.getCount());
		if (room <= 0)
			return;
		long now = lvl.getGameTime();
		// Total freshness units = F1 + proportional part of F2 (if we can’t take all)
		long F1 = freshnessUnits(lvl, target);
		long F2 = freshnessUnits(lvl, incoming);
		long F2Scaled = F2 * room / Math.max(1, incoming.getCount());
		long Ftotal = Math.max(0L, F1 + F2Scaled);
		int newCount = target.getCount() + room;
		long createdNew = now + Ftotal - PER_ITEM_TICKS * newCount;
		// Normalize perishable fields on the target
		CompoundTag tt = getTagCopy(target);
		tt.putLong(TAG_CREATED, createdNew);
		tt.remove(TAG_FROZEN_LEFT);
		tt.putBoolean(TAG_STABILIZED, false); // player inventory should be “destabilized”
		setTag(target, tt);
		target.grow(room);
		incoming.shrink(room);
	}
}