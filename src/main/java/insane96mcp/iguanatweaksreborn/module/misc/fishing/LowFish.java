package insane96mcp.iguanatweaksreborn.module.misc.fishing;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.event.HookTickToHookLureEvent;
import insane96mcp.iguanatweaksreborn.event.TideHookTickToHookLureEvent;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

@LoadFeature(module = Modules.Ids.MISC, description = "Fishing more and more in the same spot will slow down the fishing")
public class LowFish extends Feature {
    private static final Component LOW_FISH_LANG = Component.translatable(InsaneSO.lang("fishing.low_fish"));

    @Config
    public static Integer fishingSpotRange = 8;
    @Config
    public static Integer fishedBeforeSlowdown = 12;
    @Config
    public static Integer slowdownPerOnePastFished = 100;
    @Config
    public static Integer slowdownMessage = 900;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        if (ModList.get().isLoaded("tide"))
            MinecraftForge.EVENT_BUS.addListener(LowFish::shouldTideSlowdownFishing);
    }

    @SubscribeEvent
    public void onRetrieveBobber(ItemFishedEvent event) {
        if (!this.isEnabled())
            return;

        Player owner = event.getHookEntity().getPlayerOwner();
        MinecraftServer server = event.getEntity().level().getServer();
        if (owner == null
                || server == null)
            return;

        FishingData data = server.overworld().getDataStorage()
                .computeIfAbsent(FishingData::load, FishingData::create, "fishingData");
        data.addOrIncrementPos(event.getHookEntity().blockPosition(), event.getHookEntity().level().getGameTime());
        data.setDirty();
    }

    public static int getSlowdownFishing(MinecraftServer server, BlockPos pos, long gameTime) {
        int count = server.overworld().getDataStorage()
                .computeIfAbsent(FishingData::load, FishingData::create, "fishingData")
                .getCountForPos(pos, gameTime);
        if (count <= fishedBeforeSlowdown)
            return 0;
        return (count - fishedBeforeSlowdown) * slowdownPerOnePastFished;
    }

    @SubscribeEvent
    public void onLureTick(HookTickToHookLureEvent event) {
        if (event.getType() != HookTickToHookLureEvent.Type.LURE)
            return;
        MinecraftServer server = event.getHookEntity().level().getServer();
        if (server == null)
            return;
        int slowdown = getSlowdownFishing(server, event.getHookEntity().blockPosition(), event.getHookEntity().level().getGameTime());
        event.setTick(event.getTick() + slowdown);
        if (slowdown >= slowdownMessage && event.getHookEntity().getPlayerOwner() != null)
            event.getHookEntity().getPlayerOwner().displayClientMessage(LOW_FISH_LANG, true);
    }

    public static void shouldTideSlowdownFishing(TideHookTickToHookLureEvent event) {
        if (event.getType() != TideHookTickToHookLureEvent.Type.LURE)
            return;
        MinecraftServer server = event.getHookEntity().level().getServer();
        if (server == null)
            return;
        int slowdown = getSlowdownFishing(server, event.getHookEntity().blockPosition(), event.getHookEntity().level().getGameTime());
        event.setTick(event.getTick() + slowdown);
        if (slowdown >= slowdownMessage && event.getHookEntity().getPlayerOwner() != null)
            event.getHookEntity().getPlayerOwner().displayClientMessage(LOW_FISH_LANG, true);
    }
}
