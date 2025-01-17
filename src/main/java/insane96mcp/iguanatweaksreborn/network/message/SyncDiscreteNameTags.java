package insane96mcp.iguanatweaksreborn.network.message;

import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncDiscreteNameTags {
	boolean discreteNameTags;

	public SyncDiscreteNameTags(boolean discreteNameTags) {
		this.discreteNameTags = discreteNameTags;
	}

	public static void encode(SyncDiscreteNameTags pkt, FriendlyByteBuf buf) {
		buf.writeBoolean(pkt.discreteNameTags);
	}

	public static SyncDiscreteNameTags decode(FriendlyByteBuf buf) {
		return new SyncDiscreteNameTags(buf.readBoolean());
	}

	public static void handle(final SyncDiscreteNameTags message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> Tweaks.discreteNameTags = message.discreteNameTags);
		ctx.get().setPacketHandled(true);
	}

	public static void sync(boolean discreteNameTags, ServerPlayer player) {
		Object msg = new SyncDiscreteNameTags(discreteNameTags);
		NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}
}
