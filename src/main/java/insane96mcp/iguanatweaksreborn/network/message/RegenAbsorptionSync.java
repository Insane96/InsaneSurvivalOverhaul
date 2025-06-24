package insane96mcp.iguanatweaksreborn.network.message;

import insane96mcp.iguanatweaksreborn.module.combat.RegeneratingAbsorption;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import insane96mcp.iguanatweaksreborn.network.NetworkHelper;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RegenAbsorptionSync {
	float regenAbsorption;

	public RegenAbsorptionSync(float regenAbsorption) {
		this.regenAbsorption = regenAbsorption;
	}

	public static void encode(RegenAbsorptionSync pkt, FriendlyByteBuf buf) {
		buf.writeFloat(pkt.regenAbsorption);
	}

	public static RegenAbsorptionSync decode(FriendlyByteBuf buf) {
		return new RegenAbsorptionSync(buf.readFloat());
	}

	public static void handle(final RegenAbsorptionSync message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> ModNBTData.put(NetworkHelper.getSidedPlayer(ctx.get()), RegeneratingAbsorption.REGEN_ABSORPTION_TAG, message.regenAbsorption));
		ctx.get().setPacketHandled(true);
	}

	public static void sync(ServerPlayer player, float regenAbsorption) {
		Object msg = new RegenAbsorptionSync(regenAbsorption);
		NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}
}
