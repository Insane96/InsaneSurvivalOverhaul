package insane96mcp.insanesurvivaloverhaul.module.client.death;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundDeathStatsPacket(int timeSinceDeath, int deaths) implements CustomPacketPayload {
    public static final Type<ClientboundDeathStatsPacket> TYPE =
            new Type<>(InsaneSO.id("death_stats"));

    public static final StreamCodec<ByteBuf, ClientboundDeathStatsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClientboundDeathStatsPacket::timeSinceDeath,
            ByteBufCodecs.VAR_INT, ClientboundDeathStatsPacket::deaths,
            ClientboundDeathStatsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundDeathStatsPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Death.syncedTimeSinceDeath = payload.timeSinceDeath();
            Death.syncedDeaths = payload.deaths();
        });
    }

    public static void send(ServerPlayer player) {
        int time = player.getStats().getValue(Stats.CUSTOM, Stats.TIME_SINCE_DEATH);
        int deaths = player.getStats().getValue(Stats.CUSTOM, Stats.DEATHS);
        PacketDistributor.sendToPlayer(player, new ClientboundDeathStatsPacket(time, deaths));
    }
}
