package insane96mcp.insanesurvivaloverhaul.module.death.respawn;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.world.effect.ILMobEffect;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.DEATH, description = "Gives the player the Ghostly effect on respawn, making you really hard to see for mobs.")
public class Ghostly extends Feature {
	public static final DeferredHolder<MobEffect, ILMobEffect> GHOSTLY = ISORegistries.MOB_EFFECTS.register("ghostly", () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x857965, true));

	@Config(min = 0, description = "How many seconds of the Ghostly effect is given to the player on respawn.")
	public static Integer timeOnRespawn = 150;

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!this.isEnabled()
				|| timeOnRespawn <= 0
				|| event.isEndConquered())
			return;

		event.getEntity().addEffect(new MobEffectInstance(GHOSTLY, timeOnRespawn * 20, 0, false, false, true));
	}

	@SubscribeEvent
	public void onStartTracking(PlayerEvent.StartTracking event) {
		if (!this.isEnabled())
			return;
		Entity tracked = event.getTarget();
		if (!(tracked instanceof ServerPlayer trackedPlayer))
			return;
		MobEffectInstance instance = trackedPlayer.getEffect(GHOSTLY);
		if (instance == null)
			return;
		if (event.getEntity() instanceof ServerPlayer observer)
			observer.connection.send(new ClientboundUpdateMobEffectPacket(trackedPlayer.getId(), instance, false));
	}

	@SubscribeEvent
	public void onGhostlyAdded(MobEffectEvent.Added event) {
		if (!this.isEnabled() || !event.getEffectInstance().is(GHOSTLY))
			return;
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;
		player.serverLevel().getChunkSource().broadcast(player, new ClientboundUpdateMobEffectPacket(player.getId(), event.getEffectInstance(), false));
	}

	@SubscribeEvent
	public void onGhostlyRemoved(MobEffectEvent.Remove event) {
		if (!this.isEnabled() || !event.getEffect().is(GHOSTLY))
			return;
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;
		MobEffectInstance instance = event.getEffectInstance();
		if (instance == null)
			return;
		player.serverLevel().getChunkSource().broadcast(player, new ClientboundRemoveMobEffectPacket(player.getId(), instance.getEffect()));
	}

	@SubscribeEvent
	public void onGhostlyExpired(MobEffectEvent.Expired event) {
		if (!this.isEnabled() || !event.getEffectInstance().is(GHOSTLY))
			return;
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;
		player.serverLevel().getChunkSource().broadcast(player, new ClientboundRemoveMobEffectPacket(player.getId(), event.getEffectInstance().getEffect()));
	}

	@SubscribeEvent
	public void onFollowRange(LivingEvent.LivingVisibilityEvent event) {
		if (!event.getEntity().hasEffect(GHOSTLY))
			return;

		event.modifyVisibility(-256d);
	}
}