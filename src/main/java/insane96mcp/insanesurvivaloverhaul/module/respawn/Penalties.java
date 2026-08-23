package insane96mcp.insanesurvivaloverhaul.module.respawn;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.DifficultyBasedConfig;
import insane96mcp.insanelib.world.scheduled.ScheduledTasks;
import insane96mcp.insanelib.world.scheduled.ScheduledTickTask;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@LoadFeature(module = ISOModules.RESPAWN, description = "Health and hunger penalties on respawn")
public class Penalties extends Feature {
	
	public static ResourceLocation HUNGER_ON_DEATH_TAG;
	public static ResourceLocation SATURATION_ON_DEATH_TAG;

	@Config(min = 0, max = 20, description = "Min Health of respawning players")
	public static DifficultyBasedConfig health$minimum = new DifficultyBasedConfig(10, 10, 6);
	@Config(min = 0, max = 20, description = "How much health respawning players lose on respawn (not max health)")
	public static DifficultyBasedConfig health$perDeath = new DifficultyBasedConfig(1, 2, 2);
	@Config(min = 0, max = 20, description = "Min Hunger of respawning players. If below this value on death will be set to this value")
	public static DifficultyBasedConfig hunger$min = new DifficultyBasedConfig(6, 6, 6);
	@Config(min = 0, max = 20, description = "Max Hunger of respawning players. If above this value on death will be set to this value")
	public static DifficultyBasedConfig hunger$maximum = new DifficultyBasedConfig(14, 14, 10);
	@Config(min = 0, max = 20, description = "Min Saturation of respawning players. If below this value on death will be set to this value")
	public static DifficultyBasedConfig saturation$minimum = new DifficultyBasedConfig(6, 6, 6);
	@Config(min = 0, max = 20, description = "Max Saturation of respawning players. If above this value on death will be set to this value")
	public static DifficultyBasedConfig saturation$maximum = new DifficultyBasedConfig(10, 10, 6);

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		HUNGER_ON_DEATH_TAG = this.createDataKey("hunger_on_death");
		SATURATION_ON_DEATH_TAG = this.createDataKey("saturation_on_death");
	}

	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Player player))
			return;
		ModNBTData.putPersisted(player, HUNGER_ON_DEATH_TAG, player.getFoodData().getFoodLevel());
		ModNBTData.putPersisted(player, SATURATION_ON_DEATH_TAG, player.getFoodData().getSaturationLevel());
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!this.isEnabled()
				|| event.isEndConquered())
			return;

		ScheduledTasks.schedule(new ScheduledTickTask(2) {
			@Override
			public void run() {
				if (!(event.getEntity() instanceof ServerPlayer player))
					return;
				int hunger = ModNBTData.getPersisted(player, HUNGER_ON_DEATH_TAG, Integer.class);
				int maxHunger = (int) hunger$maximum.getByDifficulty(player.level());
				int minHunger = (int) hunger$min.getByDifficulty(player.level());
				hunger = Mth.clamp(hunger, minHunger, maxHunger);
				player.getFoodData().setFoodLevel(hunger);
				ModNBTData.removePersisted(player, HUNGER_ON_DEATH_TAG);

				float saturation = ModNBTData.getPersisted(player, SATURATION_ON_DEATH_TAG, Float.class);
				float maxSaturation = (float) saturation$maximum.getByDifficulty(player.level());
				float minSaturation = (float) saturation$minimum.getByDifficulty(player.level());
				saturation = Mth.clamp(saturation, minSaturation, maxSaturation);
				player.getFoodData().setSaturation(saturation);
				ModNBTData.removePersisted(player, SATURATION_ON_DEATH_TAG);

				double healthOnRespawn = player.getMaxHealth() - (health$perDeath.getByDifficulty(player.level()) * player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS)));
				double minHealth = health$minimum.getByDifficulty(player.level());
				player.setHealth((float) Math.max(healthOnRespawn, minHealth));
			}
		});
	}
}