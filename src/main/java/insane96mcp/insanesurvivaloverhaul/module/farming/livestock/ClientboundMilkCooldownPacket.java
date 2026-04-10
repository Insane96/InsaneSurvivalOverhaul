package insane96mcp.insanesurvivaloverhaul.module.farming.livestock;

import insane96mcp.insanelib.core.ModNBTData;
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

public record ClientboundMilkCooldownPacket(int entityId, int cooldown) implements CustomPacketPayload {
    public static final Type<ClientboundMilkCooldownPacket> TYPE =
            new Type<>(InsaneSO.location("milk_cooldown"));

    public static final StreamCodec<ByteBuf, ClientboundMilkCooldownPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundMilkCooldownPacket::entityId,
            ByteBufCodecs.INT, ClientboundMilkCooldownPacket::cooldown,
            ClientboundMilkCooldownPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundMilkCooldownPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(payload.entityId());
            if (!(entity instanceof LivingEntity livingEntity))
                return;
            ModNBTData.put(livingEntity, Livestock.MILK_COOLDOWN, payload.cooldown());
        });
    }

    /**
     * Syncs the current milk cooldown of the given entity to all players in its level.
     */
    public static void sync(LivingEntity entity, int cooldown) {
        if (!(entity.level() instanceof ServerLevel serverLevel))
            return;
        serverLevel.players().forEach(player ->
                PacketDistributor.sendToPlayer(player, new ClientboundMilkCooldownPacket(entity.getId(), cooldown)));
    }

    /**
     * Syncs the current milk cooldown of the given entity to a specific player.
     */
    public static void syncToPlayer(ServerPlayer player, LivingEntity entity) {
        int cooldown = ModNBTData.get(entity, Livestock.MILK_COOLDOWN, Integer.class);
        PacketDistributor.sendToPlayer(player, new ClientboundMilkCooldownPacket(entity.getId(), cooldown));
    }
}
