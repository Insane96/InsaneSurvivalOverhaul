package insane96mcp.insanesurvivaloverhaul.network.message;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record InvulnerableTimeSyncMessage(int entityId, int invulnerableTime) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InvulnerableTimeSyncMessage> TYPE =
            new CustomPacketPayload.Type<>(InsaneSO.location("invulnerable_time_sync"));

    public static final StreamCodec<ByteBuf, InvulnerableTimeSyncMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, InvulnerableTimeSyncMessage::entityId,
            ByteBufCodecs.VAR_INT, InvulnerableTimeSyncMessage::invulnerableTime,
            InvulnerableTimeSyncMessage::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final InvulnerableTimeSyncMessage payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var level = Minecraft.getInstance().level;
            if (level == null) return;
            Entity entity = level.getEntity(payload.entityId());
            if (!(entity instanceof LivingEntity living)) return;
            living.invulnerableTime = payload.invulnerableTime() + 10;
            living.hurtTime = payload.invulnerableTime();
            living.hurtDuration = payload.invulnerableTime();
        });
    }

    public static void sync(ServerLevel level, Entity entity, int invincibilityFrames) {
        var msg = new InvulnerableTimeSyncMessage(entity.getId(), invincibilityFrames);
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, msg);
        }
    }
}