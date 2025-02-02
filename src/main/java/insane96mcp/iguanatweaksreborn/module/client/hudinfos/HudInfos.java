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
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Label(name = "HUD Infos", description = "Adds various infos on top left of the screen")
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

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onHud(CustomizeGuiOverlayEvent.DebugText event) {
        Player player = Minecraft.getInstance().player;

        if (Minecraft.getInstance().options.renderDebug || player == null)
            return;

        ArrayList<String> toDraw = new ArrayList<>();
        tryRenderCardinalDirection(player, toDraw);
        tryRenderDepth(player, toDraw);
        tryRenderBiome(player, toDraw);
        tryRenderTime(player, toDraw);
        tryRenderSeason(player, toDraw);

        event.getLeft().addAll(toDraw);
    }

    @OnlyIn(Dist.CLIENT)
    public void tryRenderCardinalDirection(Player player, List<String> toDraw) {
        if (!cardinalDirection
                || !shouldRender(player, Minecraft.getInstance().hitResult, ISOItemTagsProvider.HUD_CARDINAL_DIRECTION))
            return;

        renderCardinalDirection(player, toDraw);

    }

    @OnlyIn(Dist.CLIENT)
    public void renderCardinalDirection(Player player, List<String> toDraw) {
        float direction = Mth.wrapDegrees(player.getYHeadRot());
        String d = getDirectionTranslatable(direction);
        toDraw.add(Component.translatable(d).getString());
    }

    @OnlyIn(Dist.CLIENT)
    public void tryRenderDepth(Player player, List<String> toDraw) {
        if (!depth
                || !shouldRender(player, Minecraft.getInstance().hitResult, ISOItemTagsProvider.HUD_DEPTH))
            return;

        renderDepth(player, toDraw);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderDepth(Player player, List<String> toDraw) {
        toDraw.add(Component.translatable("hud_info.depth", player.getBlockY()).getString());
    }

    @OnlyIn(Dist.CLIENT)
    public void tryRenderBiome(Player player, List<String> toDraw) {
        if (!biome
                || !shouldRender(player, Minecraft.getInstance().hitResult, ISOItemTagsProvider.HUD_BIOME))
            return;

        renderBiome(player, toDraw);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderBiome(Player player, List<String> toDraw) {
        Holder<Biome> biome = player.level().getBiome(player.blockPosition());
        String name = biome.unwrapKey().get().location().toString();
        name = name.replace(':', '.');
        toDraw.add(Component.translatable("biome." + name).getString());
    }

    @OnlyIn(Dist.CLIENT)
    public void tryRenderTime(Player player, List<String> toDraw) {
        if (!time
                || !shouldRender(player, Minecraft.getInstance().hitResult, ISOItemTagsProvider.HUD_TIME))
            return;

        renderTime(player, toDraw);
    }

    @OnlyIn(Dist.CLIENT)
    public void renderTime(Player player, List<String> toDraw) {
        toDraw.add(Component.translatable("hud_info.time", (int)((player.level().getDayTime() + 6000) % 24000 / 1000), String.format("%02d",player.level().getDayTime() % 1000 / 20), player.level().getGameTime() / 24000).getString());
    }

    @OnlyIn(Dist.CLIENT)
    public void tryRenderSeason(Player player, List<String> toDraw) {
        if (!ModList.get().isLoaded("sereneseasons")
                || !season
                || !shouldRender(player, Minecraft.getInstance().hitResult, ISOItemTagsProvider.HUD_SEASON))
            return;

        SereneSeasonsIntegration.addSeasonInfo(toDraw, player.level());
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldRender(Player player, @Nullable HitResult hitResult, TagKey<Item> itemTag) {
        return player.getInventory().contains(itemTag) || isLookingAtItemFrameWith(hitResult, itemTag);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isLookingAtItemFrameWith(@Nullable HitResult hitResult, TagKey<Item> itemTag) {
        return hitResult != null && hitResult.getType() == HitResult.Type.ENTITY && ((EntityHitResult) hitResult).getEntity() instanceof ItemFrame itemFrame && itemFrame.getItem().is(itemTag);
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
