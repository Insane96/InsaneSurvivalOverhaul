package insane96mcp.insanesurvivaloverhaul.module.hungerhealth.exhaustion;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundSaturationPacket(float saturationLevel) implements CustomPacketPayload {
    public static final Type<ClientboundSaturationPacket> TYPE =
            new Type<>(InsaneSO.location("saturation"));

    public static final StreamCodec<ByteBuf, ClientboundSaturationPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ClientboundSaturationPacket::saturationLevel,
            ClientboundSaturationPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundSaturationPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> context.player().getFoodData().setSaturation(payload.saturationLevel()));
    }

    public static void sync(ServerPlayer player, float saturationLevel) {
        PacketDistributor.sendToPlayer(player, new ClientboundSaturationPacket(saturationLevel));
    }
}
