package insane96mcp.iguanatweaksreborn.network.message;

import insane96mcp.iguanatweaksreborn.module.mining.blockdefinition.BlockDefinition;
import insane96mcp.iguanatweaksreborn.module.mining.blockdefinition.BlockDefinitionReloadListener;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BlockDefinitionSync {

    int count;
    List<BlockDefinition> blockDefinitionList;

    public BlockDefinitionSync(List<BlockDefinition> blockDefinitionList) {
        this.blockDefinitionList = blockDefinitionList;
        this.count = blockDefinitionList.size();
    }

    public static void encode(BlockDefinitionSync pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.count);
        for (BlockDefinition anvilRepair : pkt.blockDefinitionList) {
            anvilRepair.toNetwork(buf);
        }
    }

    public static BlockDefinitionSync decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<BlockDefinition> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(BlockDefinition.fromNetwork(buf));
        }
        return new BlockDefinitionSync(list);
    }

    public static void handle(final BlockDefinitionSync message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            BlockDefinitionReloadListener.restoreOriginalDefinitionsAndClear();
            BlockDefinitionReloadListener.DEFINITIONS.clear();
            BlockDefinitionReloadListener.DEFINITIONS.addAll(message.blockDefinitionList);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(List<BlockDefinition> definitions, ServerPlayer player) {
        Object msg = new BlockDefinitionSync(definitions);
        NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
