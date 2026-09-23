package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundToggleShulkerAutoPickupPacket(int containerId, int slotIndex) implements CustomPacketPayload {
    public static final Type<ServerboundToggleShulkerAutoPickupPacket> TYPE =
            new Type<>(InsaneSO.id("toggle_shulker_auto_pickup"));

    public static final StreamCodec<ByteBuf, ServerboundToggleShulkerAutoPickupPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ServerboundToggleShulkerAutoPickupPacket::containerId,
            ByteBufCodecs.VAR_INT, ServerboundToggleShulkerAutoPickupPacket::slotIndex,
            ServerboundToggleShulkerAutoPickupPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ServerboundToggleShulkerAutoPickupPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> ShulkerBoxes.toggleAutoPickup(context.player(), payload.containerId(), payload.slotIndex()));
    }

    public static void send(int containerId, int slotIndex) {
        PacketDistributor.sendToServer(new ServerboundToggleShulkerAutoPickupPacket(containerId, slotIndex));
    }
}
