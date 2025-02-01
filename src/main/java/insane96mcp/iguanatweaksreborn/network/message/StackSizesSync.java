package insane96mcp.iguanatweaksreborn.network.message;

import insane96mcp.iguanatweaksreborn.module.items.StackSizes;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StackSizesSync {
	String foodStackReductionFormula;
	Double itemStackMultiplier;
	Double blockStackMultiplier;

	public StackSizesSync(String foodStackReductionFormula, Double itemStackMultiplier, Double blockStackMultiplier) {
		this.foodStackReductionFormula = foodStackReductionFormula;
		this.itemStackMultiplier = itemStackMultiplier;
		this.blockStackMultiplier = blockStackMultiplier;
	}

	public static void encode(StackSizesSync pkt, FriendlyByteBuf buf) {
		buf.writeUtf(pkt.foodStackReductionFormula);
		buf.writeDouble(pkt.itemStackMultiplier);
		buf.writeDouble(pkt.blockStackMultiplier);
	}

	public static StackSizesSync decode(FriendlyByteBuf buf) {
		return new StackSizesSync(buf.readUtf(), buf.readDouble(), buf.readDouble());
	}

	public static void handle(final StackSizesSync message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			StackSizes.foodStackReductionFormula = message.foodStackReductionFormula;
			StackSizes.itemStackMultiplier = message.itemStackMultiplier;
			StackSizes.blockStackMultiplier = message.blockStackMultiplier;
		});
		ctx.get().setPacketHandled(true);
	}

	public static void sync(String foodStackReductionFormula, Double itemStackMultiplier, Double blockStackMultiplier, ServerPlayer player) {
		Object msg = new StackSizesSync(foodStackReductionFormula, itemStackMultiplier, blockStackMultiplier);
		NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
	}
}
