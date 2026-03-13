package insane96mcp.insanesurvivaloverhaul.module.mining.blockdefinition;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ClientboundBlockDefinitionPacket(List<BlockDefinition> definitions) implements CustomPacketPayload {
    public static final Type<ClientboundBlockDefinitionPacket> TYPE =
            new Type<>(InsaneSO.location("block_definition_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBlockDefinitionPacket> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.definitions().size());
                for (BlockDefinition def : payload.definitions())
                    def.toNetwork(buf);
            },
            buf -> {
                int count = buf.readInt();
                List<BlockDefinition> list = new ArrayList<>(count);
                for (int i = 0; i < count; i++)
                    list.add(BlockDefinition.fromNetwork(buf));
                return new ClientboundBlockDefinitionPacket(list);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundBlockDefinitionPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            BlockDefinitionReloadListener.restoreOriginalDefinitionsAndClear();
            BlockDefinitionReloadListener.DEFINITIONS.clear();
            BlockDefinitionReloadListener.DEFINITIONS.addAll(payload.definitions());
        });
    }

    public static void sync(List<BlockDefinition> definitions, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ClientboundBlockDefinitionPacket(definitions));
    }
}
