package insane96mcp.iguanatweaksreborn.network.message;

import insane96mcp.iguanatweaksreborn.module.items.misc.ItemDefinition;
import insane96mcp.iguanatweaksreborn.module.items.misc.ItemDefinitionsReloadListener;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ItemDefinitionsSync {

    int count;
    List<ItemDefinition> itemStatistics;

    public ItemDefinitionsSync(List<ItemDefinition> itemStatistics) {
        this.itemStatistics = itemStatistics;
        this.count = itemStatistics.size();
    }

    public static void encode(ItemDefinitionsSync pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.count);
        for (ItemDefinition itemStat : pkt.itemStatistics) {
            itemStat.toNetwork(buf);
        }
    }

    public static ItemDefinitionsSync decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<ItemDefinition> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(ItemDefinition.fromNetwork(buf));
        }
        return new ItemDefinitionsSync(list);
    }

    public static void handle(final ItemDefinitionsSync message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemDefinitionsReloadListener.getDefinitions().addAll(message.itemStatistics);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(List<ItemDefinition> definitions, ServerPlayer player) {
        Object msg = new ItemDefinitionsSync(definitions);
        NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
