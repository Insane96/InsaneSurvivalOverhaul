package insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.ModNBTData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundRegenAbsorptionPacket(float regenAbsorption) implements CustomPacketPayload {
    public static final Type<ClientboundRegenAbsorptionPacket> TYPE =
            new Type<>(InsaneLib.location("regen_absorption"));

    public static final StreamCodec<ByteBuf, ClientboundRegenAbsorptionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ClientboundRegenAbsorptionPacket::regenAbsorption,
            ClientboundRegenAbsorptionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundRegenAbsorptionPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ModNBTData.put(player, RegeneratingAbsorption.REGEN_ABSORPTION_TAG, payload.regenAbsorption);
        });
    }

    public static void sync(ServerPlayer player, float currentAbsorption) {
        PacketDistributor.sendToPlayer(player, new ClientboundRegenAbsorptionPacket(currentAbsorption));
    }
}
