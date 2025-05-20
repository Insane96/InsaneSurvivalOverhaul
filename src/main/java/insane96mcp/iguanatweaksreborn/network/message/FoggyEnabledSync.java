package insane96mcp.iguanatweaksreborn.network.message;

import insane96mcp.iguanatweaksreborn.module.world.weather.ClientWeather;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FoggyEnabledSync {
	private boolean enabled;

	public FoggyEnabledSync(boolean enabled) {
		this.enabled = enabled;
	}

	public static void encode(FoggyEnabledSync pkt, FriendlyByteBuf buf) {
		buf.writeBoolean(pkt.enabled);
	}

	public static FoggyEnabledSync decode(FriendlyByteBuf buf) {
		return new FoggyEnabledSync(buf.readBoolean());
	}

	public static void handle(final FoggyEnabledSync message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Feature.get(ClientWeather.class).setEnabled(message.enabled);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void sync(ServerPlayer player, boolean enabled) {
		Object msg = new FoggyEnabledSync(enabled);
		NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}
}
