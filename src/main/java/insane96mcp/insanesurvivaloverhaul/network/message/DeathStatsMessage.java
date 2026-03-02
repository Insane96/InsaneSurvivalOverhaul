package insane96mcp.insanesurvivaloverhaul.network.message;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.client.Death;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DeathStatsMessage(int timeSinceDeath, int deaths) implements CustomPacketPayload {
    public static final Type<DeathStatsMessage> TYPE =
            new Type<>(InsaneSO.location("death_stats"));

    public static final StreamCodec<ByteBuf, DeathStatsMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, DeathStatsMessage::timeSinceDeath,
            ByteBufCodecs.VAR_INT, DeathStatsMessage::deaths,
            DeathStatsMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final DeathStatsMessage payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Death.syncedTimeSinceDeath = payload.timeSinceDeath();
            Death.syncedDeaths = payload.deaths();
        });
    }

    public static void send(ServerPlayer player) {
        int time = player.getStats().getValue(Stats.CUSTOM, Stats.TIME_SINCE_DEATH);
        int deaths = player.getStats().getValue(Stats.CUSTOM, Stats.DEATHS);
        PacketDistributor.sendToPlayer(player, new DeathStatsMessage(time, deaths));
    }
}
