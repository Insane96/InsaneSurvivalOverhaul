package insane96mcp.iguanatweaksreborn.module.client.hudinfos;

import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.ClientModules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Label(name = "HudInfos", description = "Adds various infos on top left of the screen")
@LoadFeature(module = ClientModules.Ids.CLIENT)
public class HudInfos extends Feature {
    @Config
    @Label(name = "Cardinal Direction", description = "If true, items in the iguanatweaksreborn:hud/cardinal_direction will display the cardinal direction.")
    public static Boolean cardinalDirection = true;
    @Config
    @Label(name = "Season", description = "If true, items in the iguanatweaksreborn:hud/season will display the current season. Only available if Serene Seasons is installed")
    public static Boolean season = true;
    @Config
    @Label(name = "Depth", description = "If true, items in the iguanatweaksreborn:hud/depth will display the current Y level")
    public static Boolean depth = true;
    @Config
    @Label(name = "Time", description = "If true, items in the iguanatweaksreborn:hud/time will display the time of day")
    public static Boolean time = true;
    @Config
    @Label(name = "Biome", description = "If true, items in the iguanatweaksreborn:hud/biome will display the current biome")
    public static Boolean biome = true;

    public HudInfos(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onHud(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        if (mc.options.renderDebug || player == null || level == null)
            return;

        ArrayList<String> toDraw = new ArrayList<>();
        if (cardinalDirection && player.getInventory().contains(ISOItemTagsProvider.HUD_CARDINAL_DIRECTION)) {
            float direction = Mth.wrapDegrees(player.getYHeadRot());
            String d = getDirectionTranslatable(direction);
            toDraw.add(Component.translatable(d).getString());
        }
        if (depth && player.getInventory().contains(ISOItemTagsProvider.HUD_DEPTH)) {
            toDraw.add(Component.translatable("hud_info.depth", player.getBlockY()).getString());
        }
        if (biome && player.getInventory().contains(ISOItemTagsProvider.HUD_BIOME)) {
            Holder<Biome> biome = level.getBiome(player.blockPosition());
            String name = biome.unwrapKey().get().location().toString();
            name = name.replace(':', '.');
            toDraw.add(Component.translatable("biome." + name).getString());
        }
        if (time && player.getInventory().contains(ISOItemTagsProvider.HUD_TIME)) {
            toDraw.add(Component.translatable("hud_info.time", (int)((level.getDayTime() + 6000) % 24000 / 1000), String.format("%02d",level.getDayTime() % 1000 / 20), level.getGameTime() / 24000).getString());
        }
        if (ModList.get().isLoaded("sereneseasons") && season && player.getInventory().contains(ISOItemTagsProvider.HUD_SEASON)) {
            SereneSeasonsIntegration.addSeasonInfo(toDraw, level);
        }

        event.getLeft().addAll(toDraw);
    }

    private static @NotNull String getDirectionTranslatable(float direction) {
        String d = "";
        if (direction > -22.5 && direction <= 22.5)
            d = "hud_info.cardinal_direction.south";
        else if (direction > 22.5 && direction <= 67.5)
            d = "hud_info.cardinal_direction.south_west";
        else if (direction > 67.5 && direction <= 112.5)
            d = "hud_info.cardinal_direction.west";
        else if (direction > 112.5 && direction <= 157.5)
            d = "hud_info.cardinal_direction.north_west";
        else if (direction > 157.5 || direction <= -157.5)
            d = "hud_info.cardinal_direction.north";
        else if (direction > -157.5 && direction <= -112.5)
            d = "hud_info.cardinal_direction.north_east";
        else if (direction > -112.5 && direction <= -67.5)
            d = "hud_info.cardinal_direction.east";
        else if (direction > -67.5 && direction <= -22.5)
            d = "hud_info.cardinal_direction.south_east";
        return d;
    }
}
