package insane96mcp.iguanatweaksreborn.module.sleeprespawn;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.Tiredness;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.SLEEP_RESPAWN, enabledByDefault = false, name = "Sleeping and Spawn Point", description = "Changes to sleeping and spawn points")
public class Sleeping extends Feature {

	public static final String DECORATIVE_BEDS = "iguanatweaksreborn.decorative_beds";
	public static final String ENJOY_THE_NIGHT = "iguanatweaksreborn.enjoy_the_night";
	@Config(description = "If set to true the player will not be able to sleep.")
	public static Boolean disableSleeping = true;
	@Config(description = "If set to true the player spawn point cannot be changed with beds.")
	public static Boolean disableSpawnPoint = false;
	@Config(description = "If set to true the player will be able to sleep during day time. On wake up it will be night time. Note that with 'Tiredness' feature enabled you are still not able to sleep during day unless you're tired enough.")
	public static Boolean allowDaySleep = false;

	public Sleeping(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void disableSleeping(PlayerSleepInBedEvent event) {
		if (!this.isEnabled()
				|| event.getResultStatus() != null
				|| event.getEntity().level().isClientSide)
			return;

		if (disableSleeping) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
			if (disableSpawnPoint) {
				player.displayClientMessage(Component.translatable(DECORATIVE_BEDS), false);
			} else {
				player.setRespawnPosition(player.level().dimension(), event.getPos(), player.getYRot(), false, true);
				player.displayClientMessage(Component.translatable(ENJOY_THE_NIGHT), false);
			}
		}
	}

	@SubscribeEvent
	public void forceSleepDuringDay(PlayerSleepInBedEvent event) {
		if (!this.isEnabled()
				|| !allowDaySleep
				|| event.getResultStatus() != null
				|| !Tiredness.canSleepDuringDay(event.getEntity()))
			return;

		ServerPlayer player = (ServerPlayer) event.getEntity();

		event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
		player.startSleeping(event.getPos());
		((ServerLevel)player.level()).updateSleepingPlayerList();
	}

	@SubscribeEvent
	public void trySleepDuringDay(SleepingTimeCheckEvent event) {
		if (!this.isEnabled()
				|| !allowDaySleep
				|| (isEnabled(Tiredness.class) && !Tiredness.canSleepDuringDay(event.getEntity())))
			return;
		event.setResult(Event.Result.ALLOW);
	}

	@SubscribeEvent
	public void onWakeUp(SleepFinishedTimeEvent event) {
		if (!this.isEnabled()
				|| !allowDaySleep)
			return;
		int toSub = 11458;
		if (event.getLevel().getLevelData().isRaining())
			toSub = 11990;
		event.setTimeAddition(event.getNewTime() - toSub);
	}
}