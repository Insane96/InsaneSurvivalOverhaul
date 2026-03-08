package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.items.copper.CopperEquipment;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
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

    public static final TagKey<Item> HORSE_ARMORS = create("horse_armors");
    public static final TagKey<Item> MINECARTS = create("minecarts");
    public static final TagKey<Item> WOODEN_HAND_EQUIPMENT = create("equipment/hand/wooden");

    public ISOItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagLookup<Block>> tagLookupCompletableFuture, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, completableFuture, tagLookupCompletableFuture, modId, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        //Vanilla
        tag(ItemTags.PICKAXES).add(CopperEquipment.PICKAXE.get());
        tag(ItemTags.AXES).add(CopperEquipment.AXE.get());
        tag(ItemTags.SHOVELS).add(CopperEquipment.SHOVEL.get());
        tag(ItemTags.SWORDS).add(CopperEquipment.SWORD.get());
        tag(ItemTags.HOES).add(CopperEquipment.HOE.get());

        tag(ItemTags.HEAD_ARMOR).add(CopperEquipment.HELMET.get());
        tag(ItemTags.CHEST_ARMOR).add(CopperEquipment.CHESTPLATE.get());
        tag(ItemTags.LEG_ARMOR).add(CopperEquipment.LEGGINGS.get());
        tag(ItemTags.FOOT_ARMOR).add(CopperEquipment.BOOTS.get());


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

        tag(HORSE_ARMORS)
                .add(Items.LEATHER_HORSE_ARMOR, Items.IRON_HORSE_ARMOR, Items.GOLDEN_HORSE_ARMOR, Items.DIAMOND_HORSE_ARMOR);
        tag(MINECARTS)
                .add(Items.MINECART, Items.CHEST_MINECART, Items.FURNACE_MINECART, Items.HOPPER_MINECART, Items.TNT_MINECART, Items.COMMAND_BLOCK_MINECART);

        tag(Tweaks.WORLD_IMMUNE)
                .add(Items.NETHERITE_BLOCK, Items.NETHERITE_INGOT)
                .addOptionalTag(InsaneSO.location("equipment/netherite"));

        tag(WOODEN_HAND_EQUIPMENT)
                .add(Items.WOODEN_AXE, Items.WOODEN_HOE, Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_SWORD);
    }

    public static TagKey<Item> create(String tagName) {
        return TagKey.create(Registries.ITEM, InsaneSO.location(tagName));
    }
}
