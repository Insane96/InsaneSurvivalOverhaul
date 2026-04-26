package insane96mcp.insanesurvivaloverhaul.module.combat.attackspeedbasedinvincibility;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundInvulnerableTimePacket(int entityId, int invulnerableTime) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientboundInvulnerableTimePacket> TYPE =
            new CustomPacketPayload.Type<>(InsaneSO.id("invulnerable_time"));

    public static final StreamCodec<ByteBuf, ClientboundInvulnerableTimePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClientboundInvulnerableTimePacket::entityId,
            ByteBufCodecs.VAR_INT, ClientboundInvulnerableTimePacket::invulnerableTime,
            ClientboundInvulnerableTimePacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundInvulnerableTimePacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(payload.entityId());
            if (!(entity instanceof LivingEntity living)) return;
            living.invulnerableTime = payload.invulnerableTime() + 10;
            living.hurtTime = payload.invulnerableTime();
            living.hurtDuration = payload.invulnerableTime();
        });
    }

    public static void sync(ServerLevel level, Entity entity, int invincibilityFrames) {
        var msg = new ClientboundInvulnerableTimePacket(entity.getId(), invincibilityFrames);
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer(player, msg);
        }
    }
}