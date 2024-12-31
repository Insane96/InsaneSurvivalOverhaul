package insane96mcp.iguanatweaksreborn.network.message;

import insane96mcp.iguanatweaksreborn.module.experience.DroppedExperience;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncExperienceFeature {
	boolean disableExperience;

	public SyncExperienceFeature(boolean disableExperience) {
		this.disableExperience = disableExperience;
	}

	public static void encode(SyncExperienceFeature pkt, FriendlyByteBuf buf) {
		buf.writeBoolean(pkt.disableExperience);
	}

	public static SyncExperienceFeature decode(FriendlyByteBuf buf) {
		return new SyncExperienceFeature(buf.readBoolean());
	}

	public static void handle(final SyncExperienceFeature message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> DroppedExperience.disableExperience = message.disableExperience);
		ctx.get().setPacketHandled(true);
	}

	public static void sync(boolean noExperience, ServerPlayer player) {
		Object msg = new SyncExperienceFeature(noExperience);
		NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}
}
