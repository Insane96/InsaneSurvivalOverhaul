package insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ClientboundAnvilRecipeSyncPacket(List<AnvilRecipe> recipes) implements CustomPacketPayload {
    public static final Type<ClientboundAnvilRecipeSyncPacket> TYPE =
            new Type<>(InsaneSO.id("anvil_recipe_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAnvilRecipeSyncPacket> STREAM_CODEC = StreamCodec.composite(
            AnvilRecipe.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundAnvilRecipeSyncPacket::recipes,
            ClientboundAnvilRecipeSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final ClientboundAnvilRecipeSyncPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> AnvilRecipeReloadListener.RECIPES = payload.recipes());
    }

    public static void sync(List<AnvilRecipe> recipes, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ClientboundAnvilRecipeSyncPacket(recipes));
    }
}
