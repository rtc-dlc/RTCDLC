package net.crizo.rtcextras.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.BlockPos;

import net.crizo.rtcextras.procedures.CultivatorButtonRequestBacteriaProcedure;
import net.crizo.rtcextras.procedures.CultivatorButtonPurgeProcedure;
import net.crizo.rtcextras.RtcExtrasMod;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record GUICultivatorButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {

	public static final Type<GUICultivatorButtonMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(RtcExtrasMod.MODID, "gui_cultivator_buttons"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GUICultivatorButtonMessage> STREAM_CODEC = StreamCodec.of((RegistryFriendlyByteBuf buffer, GUICultivatorButtonMessage message) -> {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}, (RegistryFriendlyByteBuf buffer) -> new GUICultivatorButtonMessage(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readInt()));
	@Override
	public Type<GUICultivatorButtonMessage> type() {
		return TYPE;
	}

	public static void handleData(final GUICultivatorButtonMessage message, final IPayloadContext context) {
		if (context.flow() == PacketFlow.SERVERBOUND) {
			context.enqueueWork(() -> handleButtonAction(context.player(), message.buttonID, message.x, message.y, message.z)).exceptionally(e -> {
				context.connection().disconnect(Component.literal(e.getMessage()));
				return null;
			});
		}
	}

	public static void handleButtonAction(Player entity, int buttonID, int x, int y, int z) {
		Level world = entity.level();
		// security measure to prevent arbitrary chunk generation
		if (!world.hasChunkAt(new BlockPos(x, y, z)))
			return;
		if (buttonID == 0) {

			CultivatorButtonRequestBacteriaProcedure.execute(world, x, y, z);
		}
		if (buttonID == 1) {

			CultivatorButtonPurgeProcedure.execute(world, x, y, z);
		}
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		RtcExtrasMod.addNetworkMessage(GUICultivatorButtonMessage.TYPE, GUICultivatorButtonMessage.STREAM_CODEC, GUICultivatorButtonMessage::handleData);
	}
}