package insane96mcp.iguanatweaksreborn.data.generator;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.FireAspect;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.Knockback;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.Luck;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.BonusDamageEnchantment;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage.SweepingEdge;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.iguanatweaksreborn.module.items.StackSizes;
import insane96mcp.iguanatweaksreborn.module.items.flintexpansion.FlintExpansion;
import insane96mcp.iguanatweaksreborn.module.items.unbreakableitems.UnbreakableItems;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.Tiredness;
import insane96mcp.iguanatweaksreborn.module.world.BiomeCompass;
import insane96mcp.iguanatweaksreborn.module.world.CyanFlower;
import insane96mcp.iguanatweaksreborn.module.world.coalfire.CoalFire;
import insane96mcp.iguanatweaksreborn.module.world.spawners.Spawners;
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
        tag(ItemTags.PICKAXES).add(FlintExpansion.PICKAXE.get());
        tag(ItemTags.AXES).add(FlintExpansion.AXE.get());
        tag(ItemTags.SHOVELS).add(FlintExpansion.SHOVEL.get());
        tag(ItemTags.SWORDS).add(FlintExpansion.SWORD.get());
        tag(ItemTags.HOES).add(FlintExpansion.HOE.get());

        //ISO
        tag(StackSizes.NO_STACK_SIZE_CHANGES)
                .add(Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.PUFFERFISH)
                .addOptional(new ResourceLocation("supplementaries", "soap"));

        tag(Tiredness.ENERGY_BOOST_ITEM_TAG)
                .add(Items.COOKIE)
                .addOptional(new ResourceLocation("farmersdelight:chocolate_pie_slice")).addOptional(new ResourceLocation("create:bar_of_chocolate")).addOptional(new ResourceLocation("create:chocolate_glazed_berries"));

        tag(Crops.CHICKEN_FOOD_ITEMS)
                .add(Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TORCHFLOWER_SEEDS);

        tag(UnbreakableItems.NOT_UNBREAKABLE)
                .addTags(WOODEN_HAND_EQUIPMENT, STONE_HAND_EQUIPMENT, LEATHER_ARMOR_EQUIPMENT)
                .add(CoalFire.FIRESTARTER.get())
                .addTag(FLINT_HAND_EQUIPMENT)
                .addOptional(ForgeRegistries.ITEMS.getKey(FlintExpansion.ShieldsPlusIntegration.SHIELD.get()))
                .addOptionalTag(new ResourceLocation("iguanatweaksexpanded:equipment/hand/copper"))
                .addOptionalTag(new ResourceLocation("iguanatweaksexpanded:equipment/armor/chained_copper"))
                .addOptional(new ResourceLocation("shieldsplus:wooden_shield")).addOptional(new ResourceLocation("shieldsplus:stone_shield")).addOptional(new ResourceLocation("iguanatweaksexpanded:copper_shield"));
        tag(FoodDrinks.RAW_FOOD)
                .add(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.PORKCHOP, Items.MUTTON, Items.BEEF, Items.CHICKEN, Items.RABBIT, Items.ROTTEN_FLESH, Items.GOLDEN_CARROT)
                .addOptional(new ResourceLocation("autumnity:turkey_piece"))
                .addOptional(new ResourceLocation("berry_good:sweet_berry_mince"))
                .addOptional(new ResourceLocation("environmental:venison"));
		tag(Spawners.SPAWNER_REACTIVATOR_TAG)
				.add(Items.ECHO_SHARD);

        tag(SweepingEdge.ACCEPTS_ENCHANTMENT)
                .addTags(ItemTags.HOES);
        tag(BonusDamageEnchantment.ACCEPTS_ENCHANTMENT)
                .addTags(ItemTags.AXES, ItemTags.PICKAXES, ItemTags.SHOVELS, ItemTags.HOES, ItemTags.SWORDS)
                .addOptional(new ResourceLocation("savage_and_ravage:cleaver_of_beheading"));
        tag(Luck.ACCEPTS_ENCHANTMENT)
                .addTag(BonusDamageEnchantment.ACCEPTS_ENCHANTMENT)
                .add(Items.FISHING_ROD);
        tag(Knockback.ACCEPTS_ENCHANTMENT)
                .addTag(BonusDamageEnchantment.ACCEPTS_ENCHANTMENT);
        tag(FireAspect.ACCEPTS_ENCHANTMENT)
                .addTag(BonusDamageEnchantment.ACCEPTS_ENCHANTMENT);
        tag(Tweaks.WORLD_IMMUNE)
                .add(Items.NETHERITE_BLOCK, Items.NETHERITE_INGOT)
                .addOptionalTag(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "equipment/netherite"));

        tag(CoalFire.ITEM_ORES)
                .add(CoalFire.SOUL_SAND_HELLISH_COAL_ORE.item().get(), CoalFire.SOUL_SOIL_HELLISH_COAL_ORE.item().get());

        tag(HUD_CARDINAL_DIRECTION)
                .add(Items.COMPASS);
        tag(HUD_SEASON)
                .addOptional(ForgeRegistries.ITEMS.getKey(SSItems.CALENDAR));
        tag(HUD_DEPTH)
                .addOptional(new ResourceLocation("caverns_and_chasms:depth_gauge"))
                .addOptional(new ResourceLocation("iguanatweaksexpanded:altimeter"))
                .addOptional(new ResourceLocation("supplementaries:altimeter"));
        tag(HUD_TIME)
                .add(Items.CLOCK);
        tag(HUD_BIOME)
                .add(BiomeCompass.COMPASS.get());
    }

    public static TagKey<Item> create(String tagName) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, tagName));
    }
}
