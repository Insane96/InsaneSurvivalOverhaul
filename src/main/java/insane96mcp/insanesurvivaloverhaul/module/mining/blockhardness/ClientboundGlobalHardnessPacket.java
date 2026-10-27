package insane96mcp.insanesurvivaloverhaul.module.mining.blockhardness;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundGlobalHardnessPacket(float globalMultiplier) implements CustomPacketPayload {
	public static final Type<ClientboundGlobalHardnessPacket> TYPE =
			new Type<>(InsaneSO.id("global_hardness"));

	public static final StreamCodec<ByteBuf, ClientboundGlobalHardnessPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, ClientboundGlobalHardnessPacket::globalMultiplier,
			ClientboundGlobalHardnessPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(final ClientboundGlobalHardnessPacket payload, final IPayloadContext context) {
		context.enqueueWork(() -> BlockHardness.hardnessMultiplier = (double) payload.globalMultiplier());
	}

	public static void sync(float globalMultiplier, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new ClientboundGlobalHardnessPacket(globalMultiplier));
	}
}
