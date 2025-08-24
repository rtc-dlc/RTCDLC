package net.crizo.rtcextras.block;

import org.checkerframework.checker.units.qual.s;
import org.checkerframework.checker.units.qual.m;
import org.checkerframework.checker.units.qual.g;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayDeque;

public class BrownVelutipeMushroomBlock extends Block implements SimpleWaterloggedBlock {
	public static final int MAX_DISTANCE = 7;
	public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
	public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final TagKey<Block> MUSHROOM_LOGS = TagKey.create(Registries.BLOCK, ResourceLocation.parse("rtc_extras:mushroom_logs"));
	/** Put ALL your cap blocks (brown/red/magenta/variants) in this tag. */
	private static final TagKey<Block> MUSHROOM_CAPS = TagKey.create(Registries.BLOCK, ResourceLocation.parse("rtc_extras:mushroom_caps"));
	private static final int TICK_DELAY = 1;

	public BrownVelutipeMushroomBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.FUNGUS).strength(0.7f, 7.5f).randomTicks());
		this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, MAX_DISTANCE).setValue(PERSISTENT, Boolean.FALSE).setValue(WATERLOGGED, Boolean.FALSE));
	}

	/* visuals / lighting like leaves */
	@Override
	protected VoxelShape getBlockSupportShape(BlockState s, BlockGetter g, BlockPos p) {
		return Shapes.empty();
	}

	@Override
	protected int getLightBlock(BlockState s, BlockGetter g, BlockPos p) {
		return 1;
	}

	@Override
	protected FluidState getFluidState(BlockState s) {
		return s.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(s);
	}

	/* placement */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		FluidState fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
		int dist = computeDistanceBFS(ctx.getLevel(), ctx.getClickedPos());
		return this.defaultBlockState().setValue(PERSISTENT, Boolean.TRUE).setValue(WATERLOGGED, fluid.getType() == Fluids.WATER).setValue(DISTANCE, dist);
	}

	/* KEY CHANGE: always randomly tick when not persistent so diagonal log removals get caught */
	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return !state.getValue(PERSISTENT);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rnd) {
		int d = computeDistanceBFS(level, pos);
		if (state.getValue(DISTANCE) != d) {
			level.setBlock(pos, state.setValue(DISTANCE, d), 3);
			return;
		}
		if (!state.getValue(PERSISTENT) && d == MAX_DISTANCE) {
			onDecay(level, pos, state);
			dropResources(state, level, pos);
			level.removeBlock(pos, false);
		}
	}

	/* neighbor changes: recalc soon */
	@Override
	protected BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		if (state.getValue(WATERLOGGED)) {
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
		world.scheduleTick(pos, state.getBlock(), TICK_DELAY);
		return state;
	}

	/* when a cap is placed/removed, wake the 3x3x3 around it so diagonals react */
	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
		super.onPlace(state, level, pos, oldState, moving);
		scheduleAround3x3x3(level, pos);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (state.getBlock() != newState.getBlock()) {
			scheduleAround3x3x3(level, pos);
		}
		super.onRemove(state, level, pos, newState, moving);
	}

	private static void scheduleAround3x3x3(LevelAccessor world, BlockPos center) {
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (int dx = -1; dx <= 1; dx++)
			for (int dy = -1; dy <= 1; dy++)
				for (int dz = -1; dz <= 1; dz++) {
					if (dx == 0 && dy == 0 && dz == 0)
						continue;
					m.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
					BlockState s = world.getBlockState(m);
					if (s.is(MUSHROOM_CAPS)) {
						world.scheduleTick(m, s.getBlock(), TICK_DELAY);
					}
				}
	}

	/* ---------------- 26-neighbor BFS over caps, looking for logs ---------------- */
	private static int computeDistanceBFS(LevelAccessor level, BlockPos start) {
		// Direct 26-neighbor check first (fast path)
		for (int dx = -1; dx <= 1; dx++)
			for (int dy = -1; dy <= 1; dy++)
				for (int dz = -1; dz <= 1; dz++) {
					if (dx == 0 && dy == 0 && dz == 0)
						continue;
					BlockPos p = start.offset(dx, dy, dz);
					if (level.getBlockState(p).is(MUSHROOM_LOGS))
						return 1;
				}
		ArrayDeque<BlockPos> q = new ArrayDeque<>();
		ArrayDeque<Integer> dist = new ArrayDeque<>();
		Set<BlockPos> seen = new HashSet<>();
		q.add(start);
		dist.add(0);
		seen.add(start);
		while (!q.isEmpty()) {
			BlockPos cur = q.removeFirst();
			int d = dist.removeFirst();
			if (d >= MAX_DISTANCE - 1)
				continue; // next step would hit MAX
			for (int dx = -1; dx <= 1; dx++)
				for (int dy = -1; dy <= 1; dy++)
					for (int dz = -1; dz <= 1; dz++) {
						if (dx == 0 && dy == 0 && dz == 0)
							continue;
						BlockPos nxt = cur.offset(dx, dy, dz);
						if (!seen.add(nxt))
							continue;
						// bound by Chebyshev radius
						if (cheb(start, nxt) > MAX_DISTANCE)
							continue;
						BlockState s = level.getBlockState(nxt);
						if (s.is(MUSHROOM_LOGS)) {
							return Math.min(MAX_DISTANCE, d + 1);
						}
						// traverse through caps ONLY (don’t bridge via vanilla leaves/others)
						if (s.is(MUSHROOM_CAPS)) {
							q.addLast(nxt);
							dist.addLast(d + 1);
						}
					}
		}
		return MAX_DISTANCE;
	}

	private static int cheb(BlockPos a, BlockPos b) {
		int dx = Math.abs(a.getX() - b.getX());
		int dy = Math.abs(a.getY() - b.getY());
		int dz = Math.abs(a.getZ() - b.getZ());
		return Math.max(dx, Math.max(dy, dz));
	}

	/* ---------------- decay hook ---------------- */
	protected void onDecay(ServerLevel level, BlockPos pos, BlockState state) {
		Direction dir = clearestCardinal(level, pos, 16);
		// Dummy call you asked for
		try {
			net.crizo.rtcextras.procedures.ShroomCapDecayBurstProcedure.execute(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, dir.getStepX(), dir.getStepY(), dir.getStepZ(),
					net.crizo.rtcextras.procedures.ShroomCapDecayBurstProcedure.SporeColor.BROWN);
			//net.crizo.rtcextras.procedures.ShroomCapDecayBurstProcedure.execute();
		} catch (Throwable ignored) {
		}
	}

	private static Direction clearestCardinal(Level level, BlockPos pos, int max) {
		Direction best = Direction.UP;
		int bestLen = -1;
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (Direction d : Direction.values()) {
			int len = 0;
			m.set(pos);
			for (int i = 0; i < max; i++) {
				m.move(d);
				BlockState s = level.getBlockState(m);
				if (level.isEmptyBlock(m) || s.canBeReplaced())
					len++;
				else
					break;
			}
			if (len > bestLen) {
				bestLen = len;
				best = d;
			}
		}
		return best;
	}

	/* ---------------- state ---------------- */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
		b.add(DISTANCE, PERSISTENT, WATERLOGGED);
	}
}