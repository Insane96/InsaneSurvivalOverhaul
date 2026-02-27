package insane96mcp.insanesurvivaloverhaul.network.message;

import insane96mcp.insanelib.InsaneLib;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanesurvivaloverhaul.module.combat.RegeneratingAbsorption;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RegenAbsorptionSyncMessage(float regenAbsorption) implements CustomPacketPayload {
    public static final Type<RegenAbsorptionSyncMessage> TYPE =
            new Type<>(InsaneLib.location("regen_absorption_sync"));

    public static final StreamCodec<ByteBuf, RegenAbsorptionSyncMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, RegenAbsorptionSyncMessage::regenAbsorption,
            RegenAbsorptionSyncMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final RegenAbsorptionSyncMessage payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null)
                ModNBTData.put(player, RegeneratingAbsorption.REGEN_ABSORPTION_TAG, payload.regenAbsorption);
        });
    }

    public static void sync(ServerPlayer player, float currentAbsorption) {
        PacketDistributor.sendToPlayer(player, new RegenAbsorptionSyncMessage(currentAbsorption));
    }
}
