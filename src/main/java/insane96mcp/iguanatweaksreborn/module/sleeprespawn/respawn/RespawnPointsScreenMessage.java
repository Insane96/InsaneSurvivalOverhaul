package insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn;

import insane96mcp.iguanatweaksreborn.network.ClientNetworkHandler;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class RespawnPointsScreenMessage {
    List<RespawnPoint> respawnPoints;

    public RespawnPointsScreenMessage(List<RespawnPoint> respawnPoints) {
        this.respawnPoints = respawnPoints;
    }

    public static void encode(RespawnPointsScreenMessage pkt, FriendlyByteBuf buf) {
        buf.writeCollection(pkt.respawnPoints, (friendlyByteBuf, respawnPoint) -> {
            friendlyByteBuf.writeComponent(respawnPoint.name());
            friendlyByteBuf.writeBlockPos(respawnPoint.pos());
        });
    }

    public static RespawnPointsScreenMessage decode(FriendlyByteBuf buf) {
        return new RespawnPointsScreenMessage(buf.readList(friendlyByteBuf -> new RespawnPoint(friendlyByteBuf.readComponent(), friendlyByteBuf.readBlockPos())));
    }

    public static void handle(final RespawnPointsScreenMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientNetworkHandler.handleRespawnPointsScreenMessage(message.respawnPoints));
        ctx.get().setPacketHandled(true);
    }

    public static void send(ServerPlayer player) {
        Object msg = new RespawnPointsScreenMessage(Respawn.getRespawnPoints(player));
        NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
