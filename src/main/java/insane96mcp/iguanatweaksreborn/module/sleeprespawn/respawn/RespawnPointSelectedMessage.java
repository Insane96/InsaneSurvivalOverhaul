package insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn;

import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RespawnPointSelectedMessage {
    BlockPos pos;

    public RespawnPointSelectedMessage(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(RespawnPointSelectedMessage pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static RespawnPointSelectedMessage decode(FriendlyByteBuf buf) {
        return new RespawnPointSelectedMessage(buf.readBlockPos());
    }

    public static void handle(final RespawnPointSelectedMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() == null)
                return;
            Respawn.respawnAt(ctx.get().getSender(), message.pos);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void send(LocalPlayer player, BlockPos pos) {
        Object msg = new RespawnPointSelectedMessage(pos);
        NetworkHandler.CHANNEL.sendTo(msg, player.connection.getConnection(), NetworkDirection.PLAY_TO_SERVER);
    }
}
