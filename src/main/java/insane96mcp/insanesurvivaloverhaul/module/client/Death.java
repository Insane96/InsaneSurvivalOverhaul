package insane96mcp.insanesurvivaloverhaul.module.client;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOClientModules;
import insane96mcp.insanesurvivaloverhaul.network.message.DeathStatsMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@LoadFeature(module = ISOClientModules.CLIENT,
        name = "Death",
        description = "Changes to death")
public class Death extends Feature {
    @Config(description = "Why is that still a thing?")
    public static Boolean removeScore = true;
    @Config
    public static Boolean replaceScoreWithTimeSinceLastDeath = true;

    /** Synced from the server on death. Stores ticks since last death. */
    public static int syncedTimeSinceDeath = 0;
    /** Synced from the server on death. Stores total death count. */
    public static int syncedDeaths = 0;

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!this.isEnabled()
                || !replaceScoreWithTimeSinceLastDeath
                || !(event.getEntity() instanceof ServerPlayer player))
            return;
        DeathStatsMessage.send(player);
    }
}
