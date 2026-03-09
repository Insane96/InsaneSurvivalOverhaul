package insane96mcp.insanesurvivaloverhaul.module.hungerhealth.exhaustion;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.FoodDataAccessor;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundExhaustionPacket(float exhaustionLevel) implements CustomPacketPayload {
    public static final Type<ClientboundExhaustionPacket> TYPE =
            new Type<>(InsaneSO.location("exhaustion"));

    public static final StreamCodec<ByteBuf, ClientboundExhaustionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ClientboundExhaustionPacket::exhaustionLevel,
            ClientboundExhaustionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundExhaustionPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> ((FoodDataAccessor) context.player().getFoodData()).setExhaustionLevel(payload.exhaustionLevel()));
    }

    public static void sync(ServerPlayer player, float exhaustionLevel) {
        PacketDistributor.sendToPlayer(player, new ClientboundExhaustionPacket(exhaustionLevel));
    }
}
