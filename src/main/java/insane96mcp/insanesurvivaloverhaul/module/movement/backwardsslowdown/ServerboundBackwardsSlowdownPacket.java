package insane96mcp.insanesurvivaloverhaul.module.movement.backwardsslowdown;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundBackwardsSlowdownPacket(boolean backwards) implements CustomPacketPayload {
    public static final Type<ServerboundBackwardsSlowdownPacket> TYPE =
            new Type<>(InsaneSO.id("backwards_slowdown"));

    public static final StreamCodec<ByteBuf, ServerboundBackwardsSlowdownPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerboundBackwardsSlowdownPacket::backwards,
            ServerboundBackwardsSlowdownPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ServerboundBackwardsSlowdownPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> BackwardsSlowdown.applyModifier(context.player(), payload.backwards()));
    }

    public static void sync(boolean backwards) {
        PacketDistributor.sendToServer(new ServerboundBackwardsSlowdownPacket(backwards));
    }
}
