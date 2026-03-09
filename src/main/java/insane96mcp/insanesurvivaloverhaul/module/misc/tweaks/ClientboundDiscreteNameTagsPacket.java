package insane96mcp.insanesurvivaloverhaul.module.misc.tweaks;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundDiscreteNameTagsPacket(boolean discreteNameTags) implements CustomPacketPayload {
	public static final Type<ClientboundDiscreteNameTagsPacket> TYPE =
			new Type<>(InsaneSO.location("discrete_name_tags"));

	public static final StreamCodec<ByteBuf, ClientboundDiscreteNameTagsPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, ClientboundDiscreteNameTagsPacket::discreteNameTags,
			ClientboundDiscreteNameTagsPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(final ClientboundDiscreteNameTagsPacket payload, final IPayloadContext context) {
		context.enqueueWork(() -> Tweaks.discreteNameTags = payload.discreteNameTags());
	}

	public static void sync(boolean discreteNameTags, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, new ClientboundDiscreteNameTagsPacket(discreteNameTags));
	}
}