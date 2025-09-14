package insane96mcp.iguanatweaksreborn.module.misc.fishing;

import insane96mcp.iguanatweaksreborn.event.HookTickToHookLureEvent;
import insane96mcp.iguanatweaksreborn.event.TideHookTickToHookLureEvent;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.MISC)
public class Fishing extends Feature {

    @Config(description = "If enabled after fishing for a few times in the same spot you won't be able to fish again unless you move in another spot")
    public static Boolean antiFishingFarms = true;

    @SubscribeEvent
    public void onRetrieveBobber(ItemFishedEvent event) {
        if (!this.isEnabled()
                || !antiFishingFarms)
            return;

        Player owner = event.getHookEntity().getPlayerOwner();
        MinecraftServer server = event.getEntity().level().getServer();
        if (owner == null
                || server == null)
            return;

        server.overworld().getDataStorage()
                .computeIfAbsent(FishingData::load, FishingData::create, "fishingData")
                .addOrIncrementPos(event.getEntity().blockPosition());
    }

    public static int getSlowdownFishing(MinecraftServer server, BlockPos pos) {
        int count = server.overworld().getDataStorage()
                .computeIfAbsent(FishingData::load, FishingData::create, "fishingData")
                .getCountForPos(pos);
        if (count <= 12)
            return 0;
        return (count - 12) * 100;
    }

    @SubscribeEvent
    public void onLureTick(HookTickToHookLureEvent event) {
        if (event.getType() != HookTickToHookLureEvent.Type.LURE)
            return;
        MinecraftServer server = event.getHookEntity().level().getServer();
        if (server == null)
            return;
        event.setTick(event.getTick() + getSlowdownFishing(server, event.getHookEntity().blockPosition()));
    }

    public static void shouldTideSlowdownFishing(TideHookTickToHookLureEvent event) {
        if (event.getType() != TideHookTickToHookLureEvent.Type.LURE)
            return;
        MinecraftServer server = event.getHookEntity().level().getServer();
        if (server == null)
            return;
        event.setTick(event.getTick() + getSlowdownFishing(server, event.getHookEntity().blockPosition()));
    }
}
