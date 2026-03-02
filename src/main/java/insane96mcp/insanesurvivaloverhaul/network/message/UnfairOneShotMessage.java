package insane96mcp.insanesurvivaloverhaul.network.message;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanesurvivaloverhaul.module.combat.unfaironeshot.UnfairOneShotClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UnfairOneShotMessage() implements CustomPacketPayload {
    public static final Type<UnfairOneShotMessage> TYPE =
            new Type<>(InsaneLib.location("unfair_one_shot"));

    public static final StreamCodec<ByteBuf, UnfairOneShotMessage> STREAM_CODEC = StreamCodec.unit(
            new UnfairOneShotMessage()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final UnfairOneShotMessage payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            UnfairOneShotClient.activationTicks = 30;
        });
    }

    public static void send(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new UnfairOneShotMessage());
    }
}
