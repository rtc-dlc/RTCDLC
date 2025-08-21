package net.crizo.rtcextras.world.dimension;

public class GlieseDimension {
	@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class GlieseSpecialEffectsHandler {
		@SubscribeEvent
		public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
			DimensionSpecialEffects customEffect = new DimensionSpecialEffects(192f, true, DimensionSpecialEffects.SkyType.NORMAL, false, false) {
				@Override
				public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
					return new Vec3(1, 0.6, 0.6);
				}

				@Override
				public boolean isFoggyAt(int x, int y) {
					return false;
				}
			};
			event.register(ResourceLocation.parse("rtc_extras:gliese"), customEffect);
		}
	}
}