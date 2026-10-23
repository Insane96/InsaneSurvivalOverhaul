package insane96mcp.insanesurvivaloverhaul.module.misc.lowfish;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.event.HookTickToHookLureEvent;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

@LoadFeature(module = ISOModules.MISC, description = "Fishing more and more in the same spot will slow down the fishing")
public class LowFish extends Feature {
    private static final Component LOW_FISH_LANG = InsaneSO.translatableLang("fishing.low_fish");

    @Config
    public static Integer fishingSpotRange = 8;
    @Config
    public static Integer fishedBeforeSlowdown = 10;
    @Config
    public static Integer slowdownPerOnePastFished = 100;
    @Config(description = "Slowdown message is displayed when 'Slowdown Per One Past Fished' sums up to this value")
    public static Integer slowdownMessage = 500;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        //if (ModList.get().isLoaded("tide"))
        //    NeoForge.EVENT_BUS.addListener(LowFish::shouldTideSlowdownFishing);
    }

    @SubscribeEvent
    public void onRetrieveBobber(ItemFishedEvent event) {
        if (!this.isEnabled())
            return;

        Player owner = event.getHookEntity().getPlayerOwner();
        if (owner == null
                || !(event.getHookEntity().level() instanceof ServerLevel level))
            return;

        FishingData data = getFishingData(level);
        data.addOrIncrementPos(event.getHookEntity().blockPosition(), level.getGameTime());
        data.setDirty();
    }

    /**
     * Each dimension has its own fishing data, so the same coordinates in different dimensions are different spots
     */
    private static FishingData getFishingData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(FishingData::create, FishingData::load), "fishing_data");
    }

    public static int getSlowdownFishing(ServerLevel level, BlockPos pos, long gameTime) {
        int count = getFishingData(level).getCountForPos(pos, gameTime);
        if (count <= fishedBeforeSlowdown)
            return 0;
        return (count - fishedBeforeSlowdown) * slowdownPerOnePastFished;
    }

    @SubscribeEvent
    public void onLureTick(HookTickToHookLureEvent event) {
        if (!this.isEnabled()
                || event.getType() != HookTickToHookLureEvent.Type.LURE
                || !(event.getHookEntity().level() instanceof ServerLevel level))
            return;
        int slowdown = getSlowdownFishing(level, event.getHookEntity().blockPosition(), level.getGameTime());
        event.setTick(event.getTick() + slowdown);
        if (slowdown >= slowdownMessage && event.getHookEntity().getPlayerOwner() != null)
            event.getHookEntity().getPlayerOwner().displayClientMessage(LOW_FISH_LANG, true);
    }

    /*public static void shouldTideSlowdownFishing(TideHookTickToHookLureEvent event) {
        if (event.getType() != TideHookTickToHookLureEvent.Type.LURE)
            return;
        MinecraftServer server = event.getHookEntity().level().getServer();
        if (server == null)
            return;
        int slowdown = getSlowdownFishing(server, event.getHookEntity().blockPosition(), event.getHookEntity().level().getGameTime());
        event.setTick(event.getTick() + slowdown);
        if (slowdown >= slowdownMessage && event.getHookEntity().getPlayerOwner() != null)
            event.getHookEntity().getPlayerOwner().displayClientMessage(LOW_FISH_LANG, true);
    }*/
}
