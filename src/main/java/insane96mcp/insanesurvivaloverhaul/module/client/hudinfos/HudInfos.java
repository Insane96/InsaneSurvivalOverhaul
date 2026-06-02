package insane96mcp.insanesurvivaloverhaul.module.client.hudinfos;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOBlockTagsProvider;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOClientModules;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.Pouch;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.PouchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = ISOClientModules.CLIENT,
        name = "HUD Infos",
        description = "Adds various infos on top left of the screen")
public class HudInfos extends Feature {
    @Config(description = "If true, items in the insanesurvivaloverhaul:hud/cardinal_direction will display the cardinal direction.")
    public static Boolean cardinalDirection = true;
    @Config(description = "If true, items in the insanesurvivaloverhaul:hud/season will display the current season. Only available if Serene Seasons is installed")
    public static Boolean season = true;
    @Config(description = "If true, items in the insanesurvivaloverhaul:hud/depth will display the current Y level")
    public static Boolean depth = true;
    @Config(description = "If true, items in the insanesurvivaloverhaul:hud/time will display the time of day")
    public static Boolean time = true;
    @Config(description = "If true, items in the insanesurvivaloverhaul:hud/biome will display the current biome")
    public static Boolean biome = true;

    @OnlyIn(Dist.CLIENT)
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(InsaneSO.id("hud_infos"), (guiGraphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null || mc.getDebugOverlay().showDebugScreen() || !Feature.isEnabled(HudInfos.class))
                return;

            ArrayList<String> toDraw = new ArrayList<>();
            tryRenderCardinalDirection(player, toDraw);
            tryRenderDepth(player, toDraw);
            tryRenderBiome(player, toDraw);
            tryRenderTime(player, toDraw);
            //tryRenderSeason(player, toDraw);

            int y = 2;
            for (String s : toDraw) {
                guiGraphics.drawString(mc.font, s, 2, y, 0xFFFFFF);
                y += mc.font.lineHeight + 1;
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static void tryRenderCardinalDirection(Player player, List<String> toDraw) {
        if (!cardinalDirection
                || !shouldRender(player, Minecraft.getInstance().hitResult, ISOItemTagsProvider.HUD_CARDINAL_DIRECTION, ISOBlockTagsProvider.HUD_CARDINAL_DIRECTION))
            return;

        renderCardinalDirection(player, toDraw);
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderCardinalDirection(Player player, List<String> toDraw) {
        float direction = Mth.wrapDegrees(player.getYHeadRot());
        String d = getDirectionTranslatable(direction);
        toDraw.add(Component.translatable(d).getString());
    }

    @OnlyIn(Dist.CLIENT)
    public static void tryRenderDepth(Player player, List<String> toDraw) {
        if (!depth
                || !shouldRender(player, Minecraft.getInstance().hitResult, ISOItemTagsProvider.HUD_DEPTH, ISOBlockTagsProvider.HUD_DEPTH))
            return;

        renderDepth(player, toDraw);
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderDepth(Player player, List<String> toDraw) {
        toDraw.add(Component.translatable("hud_info.depth", player.getBlockY()).getString());
    }

    @OnlyIn(Dist.CLIENT)
    public static void tryRenderBiome(Player player, List<String> toDraw) {
        if (!biome
                || !shouldRender(player, Minecraft.getInstance().hitResult, ISOItemTagsProvider.HUD_BIOME, ISOBlockTagsProvider.HUD_BIOME))
            return;

        renderBiome(player, toDraw);
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderBiome(Player player, List<String> toDraw) {
        Holder<Biome> biome = player.level().getBiome(player.blockPosition());
        String name = biome.unwrapKey().get().location().toString();
        name = name.replace(':', '.');
        toDraw.add(Component.translatable("biome." + name).getString());
    }

    @OnlyIn(Dist.CLIENT)
    public static void tryRenderTime(Player player, List<String> toDraw) {
        if (!time
                || !shouldRender(player, Minecraft.getInstance().hitResult, ISOItemTagsProvider.HUD_TIME, ISOBlockTagsProvider.HUD_TIME))
            return;

        renderTime(player, toDraw);
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderTime(Player player, List<String> toDraw) {
        long dayTime = player.level().getDayTime();
        toDraw.add(Component.translatable("hud_info.time", ticksToTimeString(dayTime), player.level().getGameTime() / 24000).getString());
    }

    public static String ticksToTimeString(long ticks) {
        int hours = (int) ((ticks + 6000) % 24000 / 1000);
        String minutes = String.format("%02d", ticks % 1000 / 20);
        return hours + ":" + minutes;
    }

    /*@OnlyIn(Dist.CLIENT)
    public static void tryRenderSeason(Player player, List<String> toDraw) {
        if (!ModList.get().isLoaded("sereneseasons")
                || !season
                || !shouldRender(player, Minecraft.getInstance().hitResult, ISOItemTagsProvider.HUD_SEASON, ISOBlockTagsProvider.HUD_SEASON))
            return;

        SereneSeasonsIntegration.addSeasonInfo(toDraw, player.level());
    }*/

    @OnlyIn(Dist.CLIENT)
    public static boolean shouldRender(Player player, @Nullable HitResult hitResult, TagKey<Item> itemTag, TagKey<Block> blockTagKey) {
        return player.getInventory().contains(itemTag)
                || containerContains(player, itemTag)
                || isLookingAtItemFrameWith(hitResult, itemTag)
                || isLookingAtBlock(hitResult, player.level(), blockTagKey);
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean containerContains(Player player, TagKey<Item> itemTag) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == Items.BUNDLE && bundleContains(stack, itemTag))
                return true;
            else if (stack.getItem() == Pouch.ITEM.get() && pouchContains(stack, itemTag))
                return true;
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean bundleContains(ItemStack bundle, TagKey<Item> itemTag) {
        BundleContents bundlecontents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        if (bundlecontents == null)
            return false;
        for (ItemStack itemStack : bundlecontents.items()) {
            if (itemStack.is(itemTag))
                return true;
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean pouchContains(ItemStack bundle, TagKey<Item> itemTag) {
        return PouchItem.getContentsList(bundle).stream().anyMatch(s -> s.is(itemTag));
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isLookingAtItemFrameWith(@Nullable HitResult hitResult, TagKey<Item> itemTag) {
        return hitResult != null && hitResult.getType() == HitResult.Type.ENTITY && ((EntityHitResult) hitResult).getEntity() instanceof ItemFrame itemFrame && itemFrame.getItem().is(itemTag);
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isLookingAtBlock(HitResult hitResult, Level level, TagKey<Block> blockTag) {
        return hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && level.getBlockState(((BlockHitResult) hitResult).getBlockPos()).is(blockTag);
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
