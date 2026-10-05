package insane96mcp.insanesurvivaloverhaul.module.death;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@LoadFeature(module = ISOModules.DEATH, description = "Replace score in the death screen with time since last death")
public class TimeSinceLastDeath extends Feature {
    /** Synced from the server on death. Stores ticks since last death. */
    public static int syncedTimeSinceDeath = -1;
    /** Synced from the server on death. Stores total death count. */
    public static int syncedDeaths = -1;

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof ServerPlayer player))
            return;
        ClientboundDeathStatsPacket.send(player);
    }
}
