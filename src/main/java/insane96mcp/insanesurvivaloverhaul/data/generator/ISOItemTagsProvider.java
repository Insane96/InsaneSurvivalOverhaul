package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.combat.AttackSounds;
import insane96mcp.insanesurvivaloverhaul.module.death.respawn.EchoPillar;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanesurvivaloverhaul.module.items.StackSizes;
import insane96mcp.insanesurvivaloverhaul.module.items.UnvanishableItems;
import insane96mcp.insanesurvivaloverhaul.module.items.copper.CopperEquipment;
import insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins.BigOreVeins;
import insane96mcp.insanesurvivaloverhaul.module.misc.CreativeRemoval;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import insane96mcp.insanesurvivaloverhaul.module.movement.TerrainSlowdown;
import insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness.Tiredness;
import insane96mcp.insanesurvivaloverhaul.module.world.CyanFlower;
import insane96mcp.insanesurvivaloverhaul.module.world.spawners.Spawners;
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
        tag(ItemTags.FLOWERS)
                .add(CyanFlower.FLOWER.item().get());
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
                .addOptionalTag(InsaneSO.id("equipment/netherite"));

        tag(WOODEN_HAND_EQUIPMENT)
                .add(Items.WOODEN_AXE, Items.WOODEN_HOE, Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_SWORD);

        tag(FoodDrinks.RAW_FOODS)
                .add(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.PORKCHOP, Items.MUTTON, Items.BEEF, Items.CHICKEN, Items.RABBIT, Items.ROTTEN_FLESH)
                .addOptional(ResourceLocation.parse("autumnity:turkey_piece"))
                .addOptional(ResourceLocation.parse("berry_good:sweet_berry_mince"))
                .addOptional(ResourceLocation.parse("environmental:venison"));

        tag(FoodDrinks.DRINKING_FOODS)
                .add(Items.SUSPICIOUS_STEW, Items.MUSHROOM_STEW, Items.RABBIT_STEW)
                .add(FoodDrinks.FUNGI_STEW.value())
                .add(FoodDrinks.COD_CHOWDER.value(), FoodDrinks.PUFFERFISH_CHOWDER.value());

        tag(CreativeRemoval.ITEM_TAG)
                .add(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);

        tag(TerrainSlowdown.SNOW_SLOWDOWN_IGNORE)
                .add(Items.LEATHER_BOOTS)
                .addOptional(ResourceLocation.parse("tconstruct:travelers_boots"));

        tag(Crops.CHICKEN_FOOD_ITEMS)
                .add(Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TORCHFLOWER_SEEDS);

        tag(UnvanishableItems.NOT_UNVANISHABLE)
                .addOptionalTag(ResourceLocation.parse("chalk:chalks"));

        tag(Tiredness.ENERGY_BOOST_ITEM_TAG)
                .add(Items.COOKIE)
                .addOptional(ResourceLocation.parse("farmersdelight:chocolate_pie_slice")).addOptional(ResourceLocation.parse("create:bar_of_chocolate")).addOptional(ResourceLocation.parse("create:chocolate_glazed_berries"));

        tag(Spawners.SPAWNER_REACTIVATOR_TAG)
                .add(Items.ECHO_SHARD);
        tag(EchoPillar.ECHO_PILLAR_CATALYST)
                .add(Items.ECHO_SHARD);

        tag(BigOreVeins.ORE_ROCKS_ITEM)
                .add(BigOreVeins.COPPER_ORE_ROCK.item().get(), BigOreVeins.IRON_ORE_ROCK.item().get(), BigOreVeins.GOLD_ORE_ROCK.item().get());

        tag(StackSizes.NO_FOOD_STACK_SIZE_CHANGES)
                .add(Items.GOLDEN_CARROT);

        tag(AttackSounds.AXE_ATTACK_SOUND)
                .addTag(ItemTags.AXES);
        tag(AttackSounds.SWORD_ATTACK_SOUND)
                .addTag(ItemTags.SWORDS);
        tag(AttackSounds.PICKAXE_ATTACK_SOUND)
                .addTag(ItemTags.PICKAXES);
        tag(AttackSounds.SHOVEL_ATTACK_SOUND)
                .addTag(ItemTags.SHOVELS);
    }

    public static TagKey<Item> create(String tagName) {
        return TagKey.create(Registries.ITEM, InsaneSO.id(tagName));
    }
}
