package net.crizo.rtcextras.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.Minecraft;

@EventBusSubscriber
public class ProGlieseConditionsProcedure {
    private static final ResourceLocation GLIESE_GRAVITY_ID = ResourceLocation.parse("rtc_extras:gliese_gravity");
    private static final ResourceLocation GLIESE_STEP_ID    = ResourceLocation.parse("rtc_extras:gliese_step_height");

    // Tuning knobs
    private static final int   JUMP_TICKS     = 10;  // refresh often
    private static final int   JUMP_AMPLIFIER = 0;   // Jump Boost I
    // Step target ~1.125 from base 0.6 -> multiplier +0.875 with ADD_MULTIPLIED_BASE
    private static final double STEP_MULTIPLIER = (1.125 / 0.6) - 1.0; // 0.875

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        boolean isGliese = "rtc_extras:gliese".equals(level.dimension().location().toString());

        // ---- Physics for items & falling blocks (client + server) ----
        if (isGliese) {
            if (entity instanceof ItemEntity item) {
                Vec3 v = item.getDeltaMovement();
                if (v.y < 0.0) {
                    item.setDeltaMovement(v.x * 0.98, v.y * 1.20, v.z * 0.98);
                    item.hasImpulse = true;
                }
            } else if (entity instanceof FallingBlockEntity falling) {
                Vec3 v = falling.getDeltaMovement();
                if (v.y < 0.0) {
                    falling.setDeltaMovement(v.x * 0.99, v.y * 1.20, v.z * 0.99);
                    falling.hasImpulse = true;
                }
            }
        }

        // ---- Living entity handling (server only) ----
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            // Gravity 1.5g
            AttributeInstance gravity = living.getAttribute(Attributes.GRAVITY);
            if (gravity != null) {
                if (isGliese) {
                    if (gravity.getModifier(GLIESE_GRAVITY_ID) == null) {
                        double multiplier = 0.12 / 0.08 - 1.0; // 1.5g -> +0.5 base
                        gravity.addTransientModifier(new AttributeModifier(
                                GLIESE_GRAVITY_ID, multiplier, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                    }
                } else {
                    if (gravity.getModifier(GLIESE_GRAVITY_ID) != null) {
                        gravity.removeModifier(GLIESE_GRAVITY_ID);
                    }
                }
            }

            // Step height via attribute (syncs to client)
            AttributeInstance step = living.getAttribute(Attributes.STEP_HEIGHT);
            if (step != null) {
                if (isGliese) {
                    if (step.getModifier(GLIESE_STEP_ID) == null) {
                        step.addTransientModifier(new AttributeModifier(
                                GLIESE_STEP_ID, STEP_MULTIPLIER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                    }
                } else {
                    if (step.getModifier(GLIESE_STEP_ID) != null) {
                        step.removeModifier(GLIESE_STEP_ID);
                    }
                }
            }

            if (!isGliese) return;

            // Small jump assist to counter heavier gravity
            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.JUMP, JUMP_TICKS, JUMP_AMPLIFIER, true, false));

            if (entity instanceof Player player) {
                GameType gt = getEntityGameType(player);
                if (gt == GameType.CREATIVE || gt == GameType.SPECTATOR) return;
                // (hooks for flare storms / biome breathability can go here)
            }
        }
    }

    private static GameType getEntityGameType(Entity entity) {
        if (entity instanceof ServerPlayer sp) {
            return sp.gameMode.getGameModeForPlayer();
        } else if (entity instanceof Player p && p.level().isClientSide()) {
            PlayerInfo info = Minecraft.getInstance().getConnection().getPlayerInfo(p.getGameProfile().getId());
            if (info != null) return info.getGameMode();
        }
        return null;
    }
}
