package insane96mcp.iguanatweaksreborn.network.message;

import insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul.ISOExplosion;
import insane96mcp.iguanatweaksreborn.network.ClientNetworkHandler;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ExplodeParticles {
	private final double x;
	private final double y;
	private final double z;
	private final float radius;
	private final boolean hasBrokenBlocks;
	private final Explosion.BlockInteraction blockInteraction;

	public ExplodeParticles(double x, double y, double z, float radius, boolean hasBrokenBlocks, Explosion.BlockInteraction blockInteraction) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.radius = radius;
		this.hasBrokenBlocks = hasBrokenBlocks;
		this.blockInteraction = blockInteraction;
	}

	public static void encode(ExplodeParticles pkt, FriendlyByteBuf buf) {
		buf.writeDouble(pkt.x);
		buf.writeDouble(pkt.y);
		buf.writeDouble(pkt.z);
		buf.writeFloat(pkt.radius);
		buf.writeBoolean(pkt.hasBrokenBlocks);
		buf.writeEnum(pkt.blockInteraction);
	}

	public static ExplodeParticles decode(FriendlyByteBuf buf) {
		return new ExplodeParticles(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readBoolean(), buf.readEnum(Explosion.BlockInteraction.class));
	}

	public static void handle(final ExplodeParticles message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientNetworkHandler.handleExplosionParticles(message.x, message.y, message.z, message.radius, message.hasBrokenBlocks, message.blockInteraction);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void sync(ServerLevel level, ISOExplosion explosion) {
		Object msg = new ExplodeParticles(explosion.getPosition().x, explosion.getPosition().y, explosion.getPosition().z, explosion.radius, !explosion.getToBlow().isEmpty(), explosion.blockInteraction);
		level.players().forEach(player -> NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT));
	}
}
