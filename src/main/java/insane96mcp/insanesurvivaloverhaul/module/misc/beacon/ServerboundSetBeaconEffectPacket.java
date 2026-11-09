package insane96mcp.insanesurvivaloverhaul.module.misc.beacon;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSetBeaconEffectPacket(Holder<MobEffect> effect, int amplifier) implements CustomPacketPayload {
    public static final Type<ServerboundSetBeaconEffectPacket> TYPE =
            new Type<>(InsaneSO.id("set_beacon_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetBeaconEffectPacket> STREAM_CODEC = StreamCodec.composite(
            MobEffect.STREAM_CODEC, ServerboundSetBeaconEffectPacket::effect,
            ByteBufCodecs.VAR_INT, ServerboundSetBeaconEffectPacket::amplifier,
            ServerboundSetBeaconEffectPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ServerboundSetBeaconEffectPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu instanceof ISOBeaconMenu menu && menu.stillValid(context.player()))
                menu.updateEffect(payload.effect(), payload.amplifier());
        });
    }

    public static void send(Holder<MobEffect> effect, int amplifier) {
        PacketDistributor.sendToServer(new ServerboundSetBeaconEffectPacket(effect, amplifier));
    }
}
