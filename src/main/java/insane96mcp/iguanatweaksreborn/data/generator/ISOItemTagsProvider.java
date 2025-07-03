package insane96mcp.iguanatweaksreborn.data.generator;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.combat.bows.Bows;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.FireAspect;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.Knockback;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.Luck;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.BonusDamageEnchantment;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.SweepingEdge;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.iguanatweaksreborn.module.items.StackSizes;
import insane96mcp.iguanatweaksreborn.module.items.copper.CopperEquipment;
import insane96mcp.iguanatweaksreborn.module.items.flintexpansion.FlintExpansion;
import insane96mcp.iguanatweaksreborn.module.items.unbreakableitems.UnbreakableItems;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.iguanatweaksreborn.module.movement.TerrainSlowdown;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.Tiredness;
import insane96mcp.iguanatweaksreborn.module.world.BiomeCompass;
import insane96mcp.iguanatweaksreborn.module.world.CyanFlower;
import insane96mcp.iguanatweaksreborn.module.world.coalfire.CoalFire;
import insane96mcp.iguanatweaksreborn.module.world.spawners.Spawners;
import insane96mcp.iguanatweaksreborn.module.world.timber.TimberTrees;
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
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import sereneseasons.api.SSItems;

import java.util.concurrent.CompletableFuture;

public class ISOItemTagsProvider extends ItemTagsProvider {

    public static final TagKey<Item> WOODEN_HAND_EQUIPMENT = ISOItemTagsProvider.create("equipment/hand/wooden");
    public static final TagKey<Item> STONE_HAND_EQUIPMENT = ISOItemTagsProvider.create("equipment/hand/stone");
    public static final TagKey<Item> FLINT_HAND_EQUIPMENT = ISOItemTagsProvider.create("equipment/hand/flint");
    public static final TagKey<Item> GOLDEN_HAND_EQUIPMENT = ISOItemTagsProvider.create("equipment/hand/golden");
    public static final TagKey<Item> LEATHER_ARMOR_EQUIPMENT = ISOItemTagsProvider.create("equipment/armor/leather");

    public static final TagKey<Item> HUD_CARDINAL_DIRECTION = ISOItemTagsProvider.create("hud/cardinal_direction");
    public static final TagKey<Item> HUD_SEASON = ISOItemTagsProvider.create("hud/season");
    public static final TagKey<Item> HUD_DEPTH = ISOItemTagsProvider.create("hud/depth");
    public static final TagKey<Item> HUD_TIME = ISOItemTagsProvider.create("hud/time");
    public static final TagKey<Item> HUD_BIOME = ISOItemTagsProvider.create("hud/biome");

    public ISOItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture, CompletableFuture<TagLookup<Block>> tagLookupCompletableFuture, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, completableFuture, tagLookupCompletableFuture, modId, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        //Vanilla
        tag(ItemTags.FLOWERS)
                .add(CyanFlower.FLOWER.item().get());
        tag(ItemTags.PICKAXES).add(FlintExpansion.PICKAXE.get()).add(CopperEquipment.PICKAXE.get());
        tag(ItemTags.AXES).add(FlintExpansion.AXE.get()).add(CopperEquipment.AXE.get());
        tag(ItemTags.SHOVELS).add(FlintExpansion.SHOVEL.get()).add(CopperEquipment.SHOVEL.get());
        tag(ItemTags.SWORDS).add(FlintExpansion.SWORD.get()).add(CopperEquipment.SWORD.get());
        tag(ItemTags.HOES).add(FlintExpansion.HOE.get()).add(CopperEquipment.HOE.get());

        tag(ItemTags.TRIMMABLE_ARMOR)
                .add(CopperEquipment.HELMET.get(), CopperEquipment.CHESTPLATE.get(), CopperEquipment.LEGGINGS.get(), CopperEquipment.BOOTS.get());

        //Forge
        tag(Tags.Items.ARMORS_HELMETS).add(CopperEquipment.HELMET.get());
        tag(Tags.Items.ARMORS_CHESTPLATES).add(CopperEquipment.CHESTPLATE.get());
        tag(Tags.Items.ARMORS_LEGGINGS).add(CopperEquipment.LEGGINGS.get());
        tag(Tags.Items.ARMORS_BOOTS).add(CopperEquipment.BOOTS.get());

        //ISO
        tag(StackSizes.NO_STACK_SIZE_CHANGES)
                .add(Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.PUFFERFISH)
                .addOptional(ResourceLocation.fromNamespaceAndPath("supplementaries", "soap"));

        tag(Tiredness.ENERGY_BOOST_ITEM_TAG)
                .add(Items.COOKIE)
                .addOptional(ResourceLocation.parse("farmersdelight:chocolate_pie_slice")).addOptional(ResourceLocation.parse("create:bar_of_chocolate")).addOptional(ResourceLocation.parse("create:chocolate_glazed_berries"));

        tag(Crops.CHICKEN_FOOD_ITEMS)
                .add(Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TORCHFLOWER_SEEDS);

        tag(UnbreakableItems.NOT_UNBREAKABLE)
                .addOptionalTag(ResourceLocation.parse("chalk:chalks"));
        tag(FoodDrinks.RAW_FOOD)
                .add(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.PORKCHOP, Items.MUTTON, Items.BEEF, Items.CHICKEN, Items.RABBIT, Items.ROTTEN_FLESH, Items.GOLDEN_CARROT)
                .addOptional(ResourceLocation.parse("autumnity:turkey_piece"))
                .addOptional(ResourceLocation.parse("berry_good:sweet_berry_mince"))
                .addOptional(ResourceLocation.parse("environmental:venison"));
		tag(Spawners.SPAWNER_REACTIVATOR_TAG)
				.add(Items.ECHO_SHARD);

        tag(SweepingEdge.ACCEPTS_ENCHANTMENT)
                .addTags(ItemTags.HOES);
        tag(BonusDamageEnchantment.ACCEPTS_ENCHANTMENT)
                .addTags(ItemTags.AXES, ItemTags.PICKAXES, ItemTags.SHOVELS, ItemTags.HOES, ItemTags.SWORDS)
                .add(Items.CROSSBOW)
                .addOptional(ResourceLocation.parse("savage_and_ravage:cleaver_of_beheading"));
        tag(Luck.ACCEPTS_ENCHANTMENT)
                .addTag(BonusDamageEnchantment.ACCEPTS_ENCHANTMENT)
                .add(Items.FISHING_ROD)
                .add(Items.BOW)
                .add(Bows.SHORTBOW.get());
        tag(Knockback.ACCEPTS_ENCHANTMENT)
                .addTag(BonusDamageEnchantment.ACCEPTS_ENCHANTMENT);
        tag(FireAspect.ACCEPTS_ENCHANTMENT)
                .addTag(BonusDamageEnchantment.ACCEPTS_ENCHANTMENT);
        tag(Tweaks.WORLD_IMMUNE)
                .add(Items.NETHERITE_BLOCK, Items.NETHERITE_INGOT)
                .addOptionalTag(InsaneSO.location("equipment/netherite"));

        tag(CoalFire.ITEM_ORES)
                .add(CoalFire.SOUL_SAND_HELLISH_COAL_ORE.item().get(), CoalFire.SOUL_SOIL_HELLISH_COAL_ORE.item().get());

        tag(TimberTrees.BLACKLISTED_ITEMS)
                .addOptional(ResourceLocation.parse("tconstruct:broad_axe"));

        tag(TerrainSlowdown.SNOW_SLOWDOWN_IGNORE)
                .add(Items.LEATHER_BOOTS)
                .addOptional(ResourceLocation.parse("tconstruct:travelers_boots"));

        tag(HUD_CARDINAL_DIRECTION)
                .add(Items.COMPASS);
        tag(HUD_SEASON)
                .addOptional(ForgeRegistries.ITEMS.getKey(SSItems.CALENDAR));
        tag(HUD_DEPTH)
                .addOptional(ResourceLocation.parse("caverns_and_chasms:depth_gauge"))
                .addOptional(ResourceLocation.parse("iguanatweaksexpanded:altimeter"))
                .addOptional(ResourceLocation.parse("supplementaries:altimeter"));
        tag(HUD_TIME)
                .add(Items.CLOCK);
        tag(HUD_BIOME)
                .add(BiomeCompass.COMPASS.get());
    }

    public static TagKey<Item> create(String tagName) {
        return TagKey.create(Registries.ITEM, InsaneSO.location(tagName));
    }
}
