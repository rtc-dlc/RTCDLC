package net.crizo.rtcextras.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;

/**
 * Returns true if the bacterium stack should LIVE (is valid to process),
 * false if it should be considered DEAD/expired.
 *
 * NBT used:
 *  - "rtc_created" (long, ticks)
 *  - "rtc_stabilized" (boolean, optional)  // NOTE: does not resurrect expired items
 */
public final class BacteriumShouldLiveProcedure {
	// Match BacteriumItem lifetime math
	private static final long FULL_STACK_LIFETIME_TICKS = 2L * 24000L; // 48,000 ticks = 2 MC days
	private static final int STACK_UNIT = 64;
	private static final long PER_ITEM_TICKS = FULL_STACK_LIFETIME_TICKS / STACK_UNIT; // 750

	private BacteriumShouldLiveProcedure() {
	}

	/** MCreator-friendly entry: LevelAccessor + ItemStack */
	public static boolean execute(LevelAccessor world, ItemStack stack) {
		if (!(world instanceof Level level))
			return true; // no ticking context → treat as alive
		return shouldLive(level, stack, level.getGameTime());
	}

	/** Direct entry: Level + ItemStack */
	public static boolean shouldLive(Level level, ItemStack stack) {
		return shouldLive(level, stack, level.getGameTime());
	}

	/** Pure function (handy for tests): pass "now" explicitly */
	public static boolean shouldLive(Level level, ItemStack stack, long nowTicks) {
		if (stack.isEmpty())
			return false;
		if (!(stack.getItem() instanceof net.crizo.rtcextras.item.BacteriumItem))
			return true;
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		long created = tag.getLong("rtc_created");
		if (created <= 0L)
			return true; // no timestamp yet → treat as fresh/alive
		int count = Math.max(1, stack.getCount());
		long expiry = created + (PER_ITEM_TICKS * count);
		// If expired, it's dead regardless of stabilization
		if (nowTicks >= expiry)
			return false;
		// Not expired → lives (stabilization is handled elsewhere by nudging `rtc_created`)
		return true;
	}
}