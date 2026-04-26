package insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundTirednessPacket(float tiredness) implements CustomPacketPayload {
    public static final Type<ClientboundTirednessPacket> TYPE =
            new Type<>(InsaneSO.id("tiredness_sync"));

    public static final StreamCodec<ByteBuf, ClientboundTirednessPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ClientboundTirednessPacket::tiredness,
            ClientboundTirednessPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundTirednessPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> TirednessHandler.set(context.player(), payload.tiredness()));
    }

    public static void sync(ServerPlayer player, float tiredness) {
        PacketDistributor.sendToPlayer(player, new ClientboundTirednessPacket(tiredness));
    }
}
