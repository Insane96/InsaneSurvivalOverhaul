package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ISOItemTagsProvider extends ItemTagsProvider {
    public static final TagKey<Item> HUD_CARDINAL_DIRECTION = create("hud/cardinal_direction");
    public static final TagKey<Item> HUD_SEASON = create("hud/season");
    public static final TagKey<Item> HUD_DEPTH = create("hud/depth");
    public static final TagKey<Item> HUD_TIME = create("hud/time");
    public static final TagKey<Item> HUD_BIOME = create("hud/biome");

    public ISOItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagLookup<Block>> tagLookupCompletableFuture, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, completableFuture, tagLookupCompletableFuture, modId, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        //Vanilla

        //Mod's
        tag(HUD_CARDINAL_DIRECTION)
                .add(Items.COMPASS);
        tag(HUD_SEASON);
                //.addOptional(BuiltInRegistries.ITEM.getKey(SSItems.CALENDAR));
        tag(HUD_DEPTH)
                //.add(Altimeter.ITEM.get())
                .addOptional(ResourceLocation.parse("caverns_and_chasms:depth_gauge"))
                .addOptional(ResourceLocation.parse("supplementaries:altimeter"));
        tag(HUD_TIME)
                .add(Items.CLOCK);
        tag(HUD_BIOME);
                //.add(Sextant.ITEM.get());

        tag(Tweaks.WORLD_IMMUNE)
                .add(Items.NETHERITE_BLOCK, Items.NETHERITE_INGOT)
                .addOptionalTag(InsaneSO.location("equipment/netherite"));
    }

    public static TagKey<Item> create(String tagName) {
        return TagKey.create(Registries.ITEM, InsaneSO.location(tagName));
    }
}
