package net.crizo.rtcextras.block;

import org.checkerframework.checker.units.qual.s;
import org.checkerframework.checker.units.qual.g;

public class JellyBlockBlock extends net.minecraft.world.level.block.HalfTransparentBlock {
	private static final net.minecraft.world.phys.shapes.VoxelShape SHAPE = net.minecraft.world.level.block.Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
	// Tuning
	private static final double SLIDE_TRIGGER_DY = -0.08; // when “real” sliding (downward)
	private static final double SLIDE_SPEED_DY = -0.05; // gentle downward slide speed
	private static final double WALK_STICKINESS = 0.55; // horizontal damping when walking on top
	private static final double CLIMB_UP_SPEED = 0.12; // elevator climb speed while sneaking
	private static final int FX_TICK_CHANCE = 5; // 1/n chance for tick FX

	public JellyBlockBlock() {
		super(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().sound(net.minecraft.world.level.block.SoundType.HONEY_BLOCK).strength(1f, 10f).noOcclusion().isSuffocating((s, g, p) -> false).isViewBlocking((s, g, p) -> false)
				.instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.DIDGERIDOO));
	}

	@Override
	public int getLightBlock(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockGetter worldIn, net.minecraft.core.BlockPos pos) {
		return 15;
	}

	@Override
	protected net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos,
			net.minecraft.world.phys.shapes.CollisionContext ctx) {
		return SHAPE;
	}

	// --- Top landing: behave like SLIME (bounce; sneak cancels) ---
	@Override
	public void fallOn(net.minecraft.world.level.Level level, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.Entity entity, float fallDistance) {
		if (entity.isSuppressingBounce()) {
			// normal fall damage if sneaking while landing
			super.fallOn(level, state, pos, entity, fallDistance);
		} else {
			// slime-style: no damage on jelly
			entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
			if (level.isClientSide)
				showBlockParticles(level, state, entity, 10);
		}
	}

	@Override
	public void updateEntityAfterFallOn(net.minecraft.world.level.BlockGetter getter, net.minecraft.world.entity.Entity entity) {
		if (entity.isSuppressingBounce()) {
			super.updateEntityAfterFallOn(getter, entity);
			return;
		}
		// Bounce like slime
		net.minecraft.world.phys.Vec3 v = entity.getDeltaMovement();
		if (v.y < 0.0) {
			double mult = (entity instanceof net.minecraft.world.entity.LivingEntity) ? 1.0 : 0.8;
			entity.setDeltaMovement(v.x, -v.y * mult, v.z);
		}
	}

	// --- Sides: elevator + slide ---
	@Override
	protected void entityInside(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.Entity entity) {
		boolean touchingSide = isTouchingSide(pos, entity) || entity.horizontalCollision;
		if (touchingSide) {
			if (entity.isShiftKeyDown()) {
				// Elevator climb while sneaking
				applyClimb(entity);
				if (level.isClientSide && level.random.nextInt(FX_TICK_CHANCE) == 0)
					showBlockParticles(level, state, entity, 5);
			} else {
				// Gentle slide if you're not crouching
				applySlide(entity);
				if (level.isClientSide && level.random.nextInt(FX_TICK_CHANCE) == 0)
					showBlockParticles(level, state, entity, 5);
			}
		}
		super.entityInside(state, level, pos, entity);
	}

	@Override
	public void stepOn(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.entity.Entity entity) {
		// Sticky footing on top
		if (!entity.isSteppingCarefully()) {
			net.minecraft.world.phys.Vec3 v = entity.getDeltaMovement();
			entity.setDeltaMovement(v.x * WALK_STICKINESS, v.y, v.z * WALK_STICKINESS);
		}
		super.stepOn(level, pos, state, entity);
	}

	// ---- Helpers ----
	// Check “hugging the inset” similar to honey, but re-usable for climb/slide
	private static boolean isTouchingSide(net.minecraft.core.BlockPos here, net.minecraft.world.entity.Entity e) {
		double dx = Math.abs((double) here.getX() + 0.5 - e.getX());
		double dz = Math.abs((double) here.getZ() + 0.5 - e.getZ());
		double side = 0.4375 + (double) (e.getBbWidth() / 2.0F);
		boolean nearSide = dx + 1.0E-7 > side || dz + 1.0E-7 > side;
		boolean withinY = e.getY() <= (double) here.getY() + 0.9375 + 0.25;
		return nearSide && withinY;
	}

	// Honey-like downward slide throttle
	private static void applySlide(net.minecraft.world.entity.Entity e) {
		net.minecraft.world.phys.Vec3 v = e.getDeltaMovement();
		// clamp vertical speed to gentle downward slide
		double ny = (v.y < SLIDE_SPEED_DY) ? SLIDE_SPEED_DY : (v.y > SLIDE_SPEED_DY ? SLIDE_SPEED_DY : v.y);
		e.setDeltaMovement(v.x * 0.9, ny, v.z * 0.9); // add a little horizontal “stick”
		e.resetFallDistance();
	}

	// Elevator climb while sneaking
	private static void applyClimb(net.minecraft.world.entity.Entity e) {
		net.minecraft.world.phys.Vec3 v = e.getDeltaMovement();
		double ny = (v.y < CLIMB_UP_SPEED) ? CLIMB_UP_SPEED : v.y; // ensure at least climb speed
		e.setDeltaMovement(v.x * 0.85, ny, v.z * 0.85); // slight horizontal stick to the wall
		e.resetFallDistance();
	}

	private static void showBlockParticles(net.minecraft.world.level.Level level, net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.entity.Entity e, int count) {
		for (int i = 0; i < count; i++) {
			level.addParticle(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.BLOCK, state), e.getX(), e.getY(), e.getZ(), 0.0, 0.0, 0.0);
		}
	}
}