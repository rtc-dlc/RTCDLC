/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.crizo.rtcextras.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.Registries;

import net.crizo.rtcextras.entity.ProjRedSporeEntity;
import net.crizo.rtcextras.entity.ProjMagentaSporeEntity;
import net.crizo.rtcextras.entity.ProjBrownSporeEntity;
import net.crizo.rtcextras.RtcExtrasMod;

public class RtcExtrasModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, RtcExtrasMod.MODID);
	public static final DeferredHolder<EntityType<?>, EntityType<ProjRedSporeEntity>> PROJ_RED_SPORE = register("proj_red_spore",
			EntityType.Builder.<ProjRedSporeEntity>of(ProjRedSporeEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.2f, 0.2f));
	public static final DeferredHolder<EntityType<?>, EntityType<ProjBrownSporeEntity>> PROJ_BROWN_SPORE = register("proj_brown_spore",
			EntityType.Builder.<ProjBrownSporeEntity>of(ProjBrownSporeEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.2f, 0.2f));
	public static final DeferredHolder<EntityType<?>, EntityType<ProjMagentaSporeEntity>> PROJ_MAGENTA_SPORE = register("proj_magenta_spore",
			EntityType.Builder.<ProjMagentaSporeEntity>of(ProjMagentaSporeEntity::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.2f, 0.2f));

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}
}