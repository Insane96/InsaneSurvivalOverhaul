package insane96mcp.insanesurvivaloverhaul.data.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.animals.scutes.Scutes;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingFeature;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanesurvivaloverhaul.module.items.CopperEquipment;
import insane96mcp.insanesurvivaloverhaul.module.items.dagger.DaggerEquipment;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.MerchantTrades;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.Villagers;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade.MerchantTradesReloadListener;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade.TradeSet;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade.VillagerTrade;
import insane96mcp.insanesurvivaloverhaul.module.world.Berries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Writes the villagers overhaul trades into the {@code villagers_overhaul} integrated pack (not into {@code src/generated}):
 * <ul>
 *     <li>one trade file per trade: {@code data/insanesurvivaloverhaul/insanesurvivaloverhaul/villager_trade/<profession>/<name>.json};</li>
 *     <li>one trade tag per level: {@code #insanesurvivaloverhaul:<profession>/level_<n>}, so data packs can add trades to it;</li>
 *     <li>one trade set per level: {@code minecraft:<profession>/level_<n>}, with {@code remove: "all"}, written for every level of a defined profession, empty ones too;</li>
 *     <li>one Hero of the Village gift loot table per profession and effect level, from the same trades (see {@link #writeGifts}).</li>
 * </ul>
 * Every trade costs 1 emerald, with {@code reputation_discount} {@link #REPUTATION_DISCOUNT}.
 * Items that recycle into diamonds, iron, gold or copper also cost {@link #MATERIAL_PRICE_MULTIPLIER} of their recycling output, read from the recycling recipes of the {@code recycling} integrated pack.
 * <p>
 * Files of trades removed from here are not deleted: delete them by hand.
 */
public class ISOVillagerTradesProvider implements DataProvider {
    public static final TagKey<Item> DYED_TERRACOTTA = ISOItemTagsProvider.create("dyed_terracotta");
    /**
     * Small flowers sold by the farmer: no wither rose and no sniffer flowers.
     */
    public static final TagKey<Item> FARMER_FLOWERS = ISOItemTagsProvider.create("farmer_flowers");
    /**
     * Craftable banner patterns sold by the shepherd (flower, creeper, skull, thing), no loot-only patterns. The Thing pattern is included because ISO disables golden apples, so it can't be crafted.
     */
    public static final TagKey<Item> SHEPHERD_BANNER_PATTERNS = ISOItemTagsProvider.create("shepherd_banner_patterns");
    /**
     * Living coral blocks sold by the wandering trader.
     */
    public static final TagKey<Item> CORAL_BLOCKS = ISOItemTagsProvider.create("coral_blocks");
    /**
     * Complex foods bought by every villager (Local Flavours). No villager sells them.
     */
    public static final TagKey<Item> COMPLEX_FOODS = ISOItemTagsProvider.create("complex_foods");

    public static final float REPUTATION_DISCOUNT = 0.2f;
    /**
     * Share of the recycling output (at full value) asked as second cost. It's {@code Recycling.outputMultiplier}'s default, fixed so the prices don't depend on the config.
     */
    public static final float MATERIAL_PRICE_MULTIPLIER = 0.75f;
    /**
     * Merchant experience per trade, by level. Vanilla's values for selling trades.
     */
    private static final int[] XP_PER_LEVEL = {1, 5, 10, 15, 30};
    private static final String WANDERING_TRADER = "wandering_trader";
    /**
     * Merchant experience of wandering trader trades, as vanilla (the wandering trader has no levels).
     */
    private static final int WANDERING_TRADER_XP = 1;
    private static final int NUGGETS_PER_INGOT = 9;
    /**
     * Above this many nuggets the material cost is written in ingots.
     */
    private static final int MAX_NUGGETS = 64;
    private static final Map<ResourceLocation, ResourceLocation> NUGGET_TO_INGOT = Map.of(
            id(Items.IRON_NUGGET), id(Items.IRON_INGOT),
            id(Items.GOLD_NUGGET), id(Items.GOLD_INGOT),
            InsaneSO.id("copper_nugget"), id(Items.COPPER_INGOT));

    private final Path packRoot;
    private final Path recyclingRecipesFolder;
    private final CompletableFuture<HolderLookup.Provider> registries;

    private final Map<ResourceLocation, JsonObject> trades = new LinkedHashMap<>();
    private final Map<ResourceLocation, List<ResourceLocation>> tags = new LinkedHashMap<>();
    private final Map<ResourceLocation, JsonObject> tradeSets = new LinkedHashMap<>();
    private final Map<ResourceLocation, List<ResourceLocation>> structureTags = new LinkedHashMap<>();
    private final Map<ResourceLocation, List<ResourceLocation>> potionTags = new LinkedHashMap<>();
    /**
     * Giftable trades of each profession, by level (index 0 is level 1).
     */
    private final Map<ResourceLocation, List<List<GiftEntry>>> gifts = new LinkedHashMap<>();
    private final Map<ResourceLocation, JsonObject> giftLootTables = new LinkedHashMap<>();
    private Map<ResourceLocation, RecyclingOutput> recyclingOutputs;
    /**
     * Potion tags of the random potions and tipped arrows, written by {@link #potionTags()}.
     */
    private ResourceLocation levelOnePotions, levelTwoPotions;

    /**
     * @param output the default data generator output ({@code src/generated/resources}); the pack and the recycling recipes are found from it in {@code src/main/resources}.
     */
    public ISOVillagerTradesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        Path mainResources = output.getOutputFolder().resolve("../../main/resources").normalize();
        this.packRoot = mainResources.resolve("integrated_packs/villagers_overhaul");
        this.recyclingRecipesFolder = mainResources.resolve("integrated_packs/recycling/data/insanesurvivaloverhaul/recipe/recycling");
        this.registries = registries;
    }

    protected void addTrades() {
        potionTags();
        armorer();
        butcher();
        cartographer();
        cleric();
        farmer();
        fisherman();
        fletcher();
        leatherworker();
        librarian();
        mason();
        shepherd();
        toolsmith();
        weaponsmith();
        wanderingTrader();
        localFlavours();
    }

    /**
     * The random potions of the cleric's potions and the fletcher's tipped arrows.
     */
    private void potionTags() {
        //Brewable potions, long variants excluded (report_villagers.md §6.6)
        this.levelOnePotions = potionTag("level_1_potions", Potions.NIGHT_VISION, Potions.INVISIBILITY, Potions.LEAPING, Potions.FIRE_RESISTANCE, Potions.SWIFTNESS, Potions.SLOWNESS,
                Potions.TURTLE_MASTER, Potions.WATER_BREATHING, Potions.HEALING, Potions.HARMING, Potions.POISON, Potions.REGENERATION, Potions.STRENGTH, Potions.WEAKNESS, Potions.SLOW_FALLING,
                Potions.WIND_CHARGED, Potions.WEAVING, Potions.OOZING, Potions.INFESTED, Scutes.RESISTANCE);
        this.levelTwoPotions = potionTag("level_2_potions", Potions.STRONG_LEAPING, Potions.STRONG_SWIFTNESS, Potions.STRONG_SLOWNESS, Potions.STRONG_TURTLE_MASTER,
                Potions.STRONG_HEALING, Potions.STRONG_HARMING, Potions.STRONG_POISON, Potions.STRONG_REGENERATION, Potions.STRONG_STRENGTH, Scutes.STRONG_RESISTANCE);
    }

    private void armorer() {
        Profession armorer = new Profession(VillagerProfession.ARMORER);
        armorer.level(1)
                .sellsEquipment(CopperEquipment.HELMET.get())
                .sellsEquipment(CopperEquipment.CHESTPLATE.get())
                .sellsEquipment(CopperEquipment.LEGGINGS.get())
                .sellsEquipment(CopperEquipment.BOOTS.get());
        armorer.level(2)
                .sellsEquipment(Items.IRON_HELMET)
                .sellsEquipment(Items.IRON_CHESTPLATE)
                .sellsEquipment(Items.IRON_LEGGINGS)
                .sellsEquipment(Items.IRON_BOOTS)
                .sells(Items.BELL, 1, SPECIAL_ITEMS_USES);
        armorer.level(3)
                .sellsEquipment(Items.ANVIL)
                .sellsFilledBucket(Items.LAVA_BUCKET);
        armorer.level(4)
                .sellsWithMaterial(Items.PISTON, 16, REDSTONE_COMPONENTS_USES)
                .sellsWithMaterial(Items.HOPPER, 8, REDSTONE_COMPONENTS_USES)
                .sells(Items.DISPENSER, 16, REDSTONE_COMPONENTS_USES);
        armorer.level(5)
                .sellsEquipment(Items.IRON_HORSE_ARMOR)
                .sellsEquipment(Items.DIAMOND_HELMET)
                .sellsEquipment(Items.DIAMOND_CHESTPLATE)
                .sellsEquipment(Items.DIAMOND_LEGGINGS)
                .sellsEquipment(Items.DIAMOND_BOOTS);
    }

    private void butcher() {
        Profession butcher = new Profession(VillagerProfession.BUTCHER);
        butcher.level(1)
                .sellsEquipment(DaggerEquipment.IRON_DAGGER.get())
                .sellsEquipment(DaggerEquipment.COPPER_DAGGER.get());
        butcher.level(2)
                .sells(Items.CHICKEN, 2, OTHER_ITEMS_USES)
                .sells(Items.MUTTON, 2, OTHER_ITEMS_USES)
                .sells(Items.RABBIT, 2, OTHER_ITEMS_USES);
        butcher.level(3)
                .sells(Items.COOKED_CHICKEN, 1, OTHER_ITEMS_USES)
                .sells(Items.COOKED_MUTTON, 1, OTHER_ITEMS_USES)
                .sells(FoodDrinks.OVER_EASY_EGG.get(), 1, OTHER_ITEMS_USES);
        butcher.level(4)
                .sells(Items.LEATHER, 1, OTHER_ITEMS_USES)
                .sells(Items.COOKED_PORKCHOP, 1, OTHER_ITEMS_USES)
                .sells(Items.COOKED_BEEF, 1, OTHER_ITEMS_USES);
        butcher.level(5)
                .sells(Items.RABBIT_FOOT, 1, OTHER_ITEMS_USES)
                .sells(Items.COOKED_RABBIT, 1, OTHER_ITEMS_USES)
                .sellsEquipment(DaggerEquipment.DIAMOND_DAGGER.get());
    }

    private void cartographer() {
        Profession cartographer = new Profession(VillagerProfession.CARTOGRAPHER);
        cartographer.level(1)
                .sells(Items.PAPER, 16, OTHER_ITEMS_USES)
                .sells(Items.MAP, 1, OTHER_ITEMS_USES)
                .sellsWithMaterial(Items.COMPASS, 4, OTHER_ITEMS_USES);
        cartographer.level(2)
                .sellsMap("trial_chambers_map", StructureTags.ON_TRIAL_CHAMBERS_MAPS.location(), "trial_chambers", "filled_map.trial_chambers")
                .sellsEquipment(Items.BRUSH);
        cartographer.level(3)
                .sellsMap("ocean_monument_map", StructureTags.ON_OCEAN_EXPLORER_MAPS.location(), "monument", "filled_map.monument")
                .sellsEquipment(Items.SPYGLASS);
        cartographer.level(4)
                .sellsRandom("pottery_sherd", Items.ANGLER_POTTERY_SHERD, ItemTags.DECORATED_POT_SHERDS, 4, OTHER_ITEMS_USES)
                .sellsRandom("banner", Items.WHITE_BANNER, ItemTags.BANNERS, 16, OTHER_ITEMS_USES)
                .sells(Items.ITEM_FRAME, 16, OTHER_ITEMS_USES);
        cartographer.level(5)
                .sellsMap("ancient_city_map", structureTag("on_ancient_city_maps", BuiltinStructures.ANCIENT_CITY.location()), "target_point", "filled_map.insanesurvivaloverhaul.ancient_city")
                .sellsRandom("pottery_sherd_level_5", Items.ANGLER_POTTERY_SHERD, ItemTags.DECORATED_POT_SHERDS, 4, OTHER_ITEMS_USES);
    }

    private void cleric() {
        Profession cleric = new Profession(VillagerProfession.CLERIC);
        cleric.level(1)
                .sells(Items.GLASS_BOTTLE, 16, OTHER_ITEMS_USES)
                .sells(Items.REDSTONE, 16, OTHER_ITEMS_USES)
                .sells(Items.GLOWSTONE_DUST, 16, OTHER_ITEMS_USES);
        cleric.level(2)
                .sells(Items.NETHER_WART, SEEDS, SEEDS_USES)
                .sells(Items.FERMENTED_SPIDER_EYE, 8, OTHER_ITEMS_USES)
                .sells(Items.SUGAR, 16, OTHER_ITEMS_USES)
                .sells(Items.MAGMA_CREAM, 8, OTHER_ITEMS_USES);
        cleric.level(3)
                .sellsRandomPotion("potion", Items.POTION, this.levelOnePotions)
                .sellsRandomPotion("splash_potion", Items.SPLASH_POTION, this.levelOnePotions);
        cleric.level(4)
                .sellsRandomPotion("strong_potion", Items.POTION, this.levelTwoPotions)
                .sellsRandomPotion("strong_splash_potion", Items.SPLASH_POTION, this.levelTwoPotions);
        cleric.level(5)
                .sellsRandomPotion("lingering_potion", Items.LINGERING_POTION, this.levelOnePotions)
                .sells(Items.BLAZE_POWDER, 4, OTHER_ITEMS_USES)
                .sells(Items.GHAST_TEAR, 4, OTHER_ITEMS_USES);
    }

    private void farmer() {
        Profession farmer = new Profession(VillagerProfession.FARMER);
        farmer.level(1)
                .sells(Items.BONE_MEAL, 16, OTHER_ITEMS_USES)
                .sellsRandom("flower", Items.DANDELION, FARMER_FLOWERS, 24, OTHER_ITEMS_USES);
        farmer.level(2)
                .sells(Items.PUMPKIN_SEEDS, SEEDS, SEEDS_USES)
                .sells(Items.MELON_SEEDS, SEEDS, SEEDS_USES)
                .sells(Items.COCOA_BEANS, SEEDS, SEEDS_USES)
                .sells(Items.RED_MUSHROOM, SEEDS, SEEDS_USES)
                .sells(Items.BROWN_MUSHROOM, SEEDS, SEEDS_USES)
                .sells(Items.SUGAR_CANE, SEEDS, SEEDS_USES);
        farmer.level(3)
                .sells(Items.EGG, 8, OTHER_ITEMS_USES);
        farmer.level(4)
                .sells(Items.GLOW_BERRIES, 8, OTHER_ITEMS_USES)
                .sells(Items.HONEY_BOTTLE, 8, OTHER_ITEMS_USES)
                .sells(Items.HONEYCOMB, 8, OTHER_ITEMS_USES);
        //Non-renewable seeds, 1 per emerald (report_villagers.md §6.7)
        farmer.level(5)
                .sells(Items.WHEAT_SEEDS, 1, SEEDS_USES)
                .sells(Items.BEETROOT_SEEDS, 1, SEEDS_USES)
                .sells(Crops.CARROT_SEEDS.get(), 1, SEEDS_USES)
                .sells(Crops.ROOTED_POTATO.get(), 1, SEEDS_USES)
                .sells(Berries.SWEET_BERRY_SEEDS.get(), 1, SEEDS_USES);
    }

    private void fisherman() {
        Profession fisherman = new Profession(VillagerProfession.FISHERMAN);
        fisherman.level(1)
                .sells(Items.COD, 4, OTHER_ITEMS_USES)
                .sells(Items.SALMON, 4, OTHER_ITEMS_USES)
                .sells(Items.DRIED_KELP, 8, OTHER_ITEMS_USES)
                .sells(Items.FISHING_ROD, 1, SPECIAL_ITEMS_USES);
        fisherman.level(2)
                .sells(Items.COOKED_COD, 2, OTHER_ITEMS_USES)
                .sells(Items.COOKED_SALMON, 2, OTHER_ITEMS_USES)
                .sells(Items.PUFFERFISH, 2, OTHER_ITEMS_USES);
        fisherman.level(3)
                .sells(FoodDrinks.COD_CHOWDER.get(), 1, OTHER_ITEMS_USES)
                .sellsFilledBucket(Items.COD_BUCKET);
        fisherman.level(4)
                .sells(Items.NAUTILUS_SHELL, 1, OTHER_ITEMS_USES)
                .sellsFilledBucket(Items.SALMON_BUCKET)
                .sellsFilledBucket(Items.PUFFERFISH_BUCKET);
        fisherman.level(5)
                .sells(Items.LILY_PAD, 8, OTHER_ITEMS_USES)
                .sells(Items.INK_SAC, 8, OTHER_ITEMS_USES)
                .sells(Items.BAMBOO, 8, OTHER_ITEMS_USES)
                .sellsFilledBucket(Items.TROPICAL_FISH_BUCKET);
    }

    private void fletcher() {
        Profession fletcher = new Profession(VillagerProfession.FLETCHER);
        fletcher.level(1)
                .sells(Items.ARROW, ARROWS, ARROWS_USES)
                .sells(Bows.SHORTBOW.get(), 1, SPECIAL_ITEMS_USES);
        fletcher.level(2)
                .sells(Items.BOW, 1, SPECIAL_ITEMS_USES)
                .sellsEquipment(Items.CROSSBOW)
                .sells(FletchingFeature.TORCH_ARROW_ITEM.get(), SPECIAL_ARROWS, SPECIAL_ARROWS_USES)
                .sells(Items.FLINT, 8, OTHER_ITEMS_USES)
                .sells(Items.FEATHER, 8, OTHER_ITEMS_USES);
        fletcher.level(3)
                .sells(Items.SPECTRAL_ARROW, SPECIAL_ARROWS, SPECIAL_ARROWS_USES)
                .sells(FletchingFeature.QUARTZ_ARROW_ITEM.get(), SPECIAL_ARROWS, SPECIAL_ARROWS_USES)
                .sellsRandomTippedArrow("tipped_arrow", this.levelOnePotions);
        fletcher.level(4)
                .sells(FletchingFeature.ICE_ARROW_ITEM.get(), SPECIAL_ARROWS, SPECIAL_ARROWS_USES)
                .sellsRandomTippedArrow("strong_tipped_arrow", this.levelTwoPotions);
        fletcher.level(5)
                .sells(FletchingFeature.EXPLOSIVE_ARROW_ITEM.get(), SPECIAL_ARROWS, SPECIAL_ARROWS_USES)
                .sells(FletchingFeature.DIAMOND_ARROW_ITEM.get(), SPECIAL_ARROWS, SPECIAL_ARROWS_USES);
    }

    private void leatherworker() {
        Profession leatherworker = new Profession(VillagerProfession.LEATHERWORKER);
        leatherworker.level(1)
                .log(Items.OAK_LOG)
                .log(Items.BIRCH_LOG)
                .log(Items.SPRUCE_LOG)
                .sellsEquipment(CopperEquipment.AXE.get())
                .sellsEquipment(Items.IRON_AXE);
        leatherworker.level(2)
                .log(Items.ACACIA_LOG)
                .log(Items.JUNGLE_LOG)
                .log(Items.DARK_OAK_LOG)
                .log(Items.CHERRY_LOG)
                .log(Items.MANGROVE_LOG);
        leatherworker.level(3)
                .log(Items.CRIMSON_STEM)
                .log(Items.WARPED_STEM)
                .log(Items.BAMBOO_BLOCK)
                .log(Items.MUSHROOM_STEM);
        leatherworker.level(4)
                .sells(Items.LEATHER_HELMET, 1, SPECIAL_ITEMS_USES)
                .sells(Items.LEATHER_CHESTPLATE, 1, SPECIAL_ITEMS_USES)
                .sells(Items.LEATHER_LEGGINGS, 1, SPECIAL_ITEMS_USES)
                .sells(Items.LEATHER_BOOTS, 1, SPECIAL_ITEMS_USES);
        leatherworker.level(5)
                .sells(Items.SADDLE, 1, SPECIAL_ITEMS_USES)
                .sells(Items.LEATHER_HORSE_ARMOR, 1, SPECIAL_ITEMS_USES)
                .sells(Items.LEAD, 8, OTHER_ITEMS_USES);
    }

    private void librarian() {
        ResourceLocation tradeable = EnchantmentTags.TRADEABLE.location();
        Profession librarian = new Profession(VillagerProfession.LIBRARIAN);
        librarian.level(1)
                .sells(Items.PAPER, 16, OTHER_ITEMS_USES)
                .sells(Items.BOOK, 8, OTHER_ITEMS_USES);
        librarian.level(2)
                .sells(Items.LAPIS_LAZULI, 16, OTHER_ITEMS_USES)
                .sells(Items.BOOKSHELF, 4, OTHER_ITEMS_USES)
                .sellsWithMaterial(Items.LANTERN, 12, OTHER_ITEMS_USES)
                .sellsEnchantedBook("enchanted_book", tradeable, 10);
        librarian.level(3)
                .sells("lapis_lazuli_level_3", id(Items.LAPIS_LAZULI), 32, OTHER_ITEMS_USES)
                .sellsEnchantedBook("enchanted_book_level_3", tradeable, 20)
                .sellsWithMaterial(Items.COMPASS, 4, OTHER_ITEMS_USES)
                .sellsWithMaterial(Items.CLOCK, 4, OTHER_ITEMS_USES);
        librarian.level(4)
                .sellsEnchantedBook("enchanted_book_level_4", tradeable, 30)
                .sells(Items.WRITABLE_BOOK, 1, OTHER_ITEMS_USES);
        //Divine Fragment: bounded by echo shards, which only come from ancient city chests (report_villagers.md §6.11)
        librarian.level(5)
                .sellsFor(Items.HEART_OF_THE_SEA, 1, SPECIAL_ITEMS_USES, Items.ECHO_SHARD, 1);
    }

    /**
     * Writes a potion tag into the pack, for the random potions of a trade. Returns its id.
     */
    @SafeVarargs
    private ResourceLocation potionTag(String path, Holder<Potion>... potions) {
        ResourceLocation id = InsaneSO.id(path);
        this.potionTags.put(id, Arrays.stream(potions).map(potion -> potion.unwrapKey().orElseThrow().location()).toList());
        return id;
    }

    /**
     * Writes a structure tag into the pack, for map destinations that have no vanilla tag. Returns its id.
     */
    private ResourceLocation structureTag(String path, ResourceLocation... structures) {
        ResourceLocation id = InsaneSO.id(path);
        this.structureTags.put(id, Arrays.stream(structures).toList());
        return id;
    }

    private void mason() {
        Profession mason = new Profession(VillagerProfession.MASON);
        mason.level(1)
                .simpleBlock(Items.STONE)
                .simpleBlock(Items.ANDESITE)
                .simpleBlock(Items.DIORITE)
                .simpleBlock(Items.GRANITE)
                .simpleBlock(Items.DIRT);
        mason.level(2)
                .simpleBlock(Items.DEEPSLATE)
                .processedBlock(Items.GLASS)
                .processedBlock(Items.SMOOTH_SANDSTONE)
                .processedBlock(Items.SMOOTH_RED_SANDSTONE)
                .simpleBlock(Items.TUFF);
        mason.level(3)
                .simpleBlock(Items.CALCITE)
                .simpleBlock(Items.DRIPSTONE_BLOCK)
                .processedBlock(Items.MUD_BRICKS)
                .sellsRandom("terracotta", Items.WHITE_TERRACOTTA, DYED_TERRACOTTA, PROCESSED_BLOCKS, PROCESSED_BLOCKS_USES);
        mason.level(4)
                .simpleBlock(Items.BLACKSTONE)
                .processedBlock(Items.SMOOTH_BASALT)
                .processedBlock(Items.SMOOTH_QUARTZ);
        mason.level(5)
                .sellsRandom("concrete", Items.WHITE_CONCRETE, Tags.Items.CONCRETES, PROCESSED_BLOCKS, PROCESSED_BLOCKS_USES)
                .sellsRandom("glazed_terracotta", Items.WHITE_GLAZED_TERRACOTTA, Tags.Items.GLAZED_TERRACOTTAS, PROCESSED_BLOCKS, PROCESSED_BLOCKS_USES);
    }

    //Coloured items are one trade with a random colour, so every colour can be found (report_villagers.md §6.13)
    private void shepherd() {
        Profession shepherd = new Profession(VillagerProfession.SHEPHERD);
        shepherd.level(1)
                .sells(Items.WHITE_WOOL, WOOL, WOOL_USES)
                .sellsEquipment(Items.SHEARS)
                .sells(Items.STRING, 16, OTHER_ITEMS_USES);
        shepherd.level(2)
                .sellsRandom("wool", Items.WHITE_WOOL, ItemTags.WOOL, WOOL, WOOL_USES)
                .sellsRandom("dye", Items.WHITE_DYE, Tags.Items.DYES, 16, OTHER_ITEMS_USES)
                .sellsRandom("carpet", Items.WHITE_CARPET, ItemTags.WOOL_CARPETS, 64, WOOL_USES);
        shepherd.level(3)
                .sellsRandom("bed", Items.WHITE_BED, ItemTags.BEDS, 1, OTHER_ITEMS_USES)
                .sellsRandom("banner", Items.WHITE_BANNER, ItemTags.BANNERS, 16, OTHER_ITEMS_USES)
                .sellsRandom("wool_level_3", Items.WHITE_WOOL, ItemTags.WOOL, WOOL, WOOL_USES);
        shepherd.level(4)
                .sells(Items.PAINTING, 16, OTHER_ITEMS_USES)
                .sells(Items.ITEM_FRAME, 16, OTHER_ITEMS_USES)
                .sells(Items.GLOW_ITEM_FRAME, 16, OTHER_ITEMS_USES)
                .sellsRandom("wool_level_4", Items.WHITE_WOOL, ItemTags.WOOL, WOOL, WOOL_USES);
        shepherd.level(5)
                .sellsRandom("banner_pattern", Items.FLOWER_BANNER_PATTERN, SHEPHERD_BANNER_PATTERNS, 1, SPECIAL_ITEMS_USES);
    }

    private void toolsmith() {
        Profession toolsmith = new Profession(VillagerProfession.TOOLSMITH);
        toolsmith.level(1)
                .sellsEquipment(CopperEquipment.PICKAXE.get())
                .sellsEquipment(CopperEquipment.SHOVEL.get())
                .sellsEquipment(CopperEquipment.HOE.get());
        toolsmith.level(2)
                .sellsEquipment(Items.IRON_PICKAXE)
                .sellsEquipment(Items.IRON_SHOVEL)
                .sellsEquipment(Items.IRON_HOE)
                .sells(Items.BELL, 1, SPECIAL_ITEMS_USES);
        toolsmith.level(3)
                .sellsEquipment(Items.ANVIL)
                .sellsFilledBucket(Items.LAVA_BUCKET);
        toolsmith.level(4)
                .sellsWithMaterial(Items.CRAFTER, 8, SPECIAL_ITEMS_USES)
                .sellsEquipment(Items.SMITHING_TABLE)
                //No recycling recipe for grindstones (no metal): only the emerald
                .sells(Items.GRINDSTONE, 1, SPECIAL_ITEMS_USES);
        toolsmith.level(5)
                .sellsEquipment(Items.DIAMOND_PICKAXE)
                .sellsEquipment(Items.DIAMOND_SHOVEL)
                .sellsEquipment(Items.DIAMOND_AXE)
                .sellsEquipment(Items.DIAMOND_HOE);
    }

    private void weaponsmith() {
        Profession weaponsmith = new Profession(VillagerProfession.WEAPONSMITH);
        weaponsmith.level(1)
                .sellsEquipment(CopperEquipment.SWORD.get())
                .sellsEquipment(DaggerEquipment.COPPER_DAGGER.get())
                .sellsEquipment(CopperEquipment.AXE.get());
        weaponsmith.level(2)
                .sellsEquipment(Items.IRON_SWORD)
                .sellsEquipment(DaggerEquipment.IRON_DAGGER.get())
                .sellsEquipment(Items.IRON_AXE)
                .sellsEquipment(Items.SHIELD)
                .sells(Items.BELL, 1, SPECIAL_ITEMS_USES);
        weaponsmith.level(3)
                .sellsEquipment(Items.ANVIL)
                .sellsFilledBucket(Items.LAVA_BUCKET);
        weaponsmith.level(4)
                .sells(Items.REPEATER, 16, REDSTONE_COMPONENTS_USES)
                .sells(Items.COMPARATOR, 16, REDSTONE_COMPONENTS_USES)
                .sells(Items.OBSERVER, 16, REDSTONE_COMPONENTS_USES);
        weaponsmith.level(5)
                .sellsEquipment(Items.DIAMOND_SWORD)
                .sellsEquipment(DaggerEquipment.DIAMOND_DAGGER.get());
    }

    //report_villagers.md §6.15
    private void wanderingTrader() {
        wanderingTraderPool("buying", 2)
                //One trade for every disc, so discs don't crowd out the other purchases
                .buysRandom("music_disc", Items.MUSIC_DISC_13, ItemTags.CREEPER_DROP_MUSIC_DISCS, 1)
                //As in ITR (vanilla's trade rebalance), but always for 1 emerald
                .buys(Items.BAKED_POTATO, 4)
                .buys(Items.FERMENTED_SPIDER_EYE, 1)
                .buys(Items.HAY_BLOCK, 1)
                .buys(Items.MILK_BUCKET, 1)
                .buys(Items.WATER_BUCKET, 1)
                .buys("water_bottle", Items.POTION, 1, waterBottleComponents());
        wanderingTraderPool("uncommon", 2)
                .sellsMap("desert_pyramid_map", structureTag("on_desert_pyramid_maps", BuiltinStructures.DESERT_PYRAMID.location()), "target_point", "filled_map.insanesurvivaloverhaul.desert_pyramid")
                .sellsMap("jungle_pyramid_map", StructureTags.ON_JUNGLE_EXPLORER_MAPS.location(), "jungle_temple", "filled_map.explorer_jungle")
                .sellsMap("swamp_hut_map", StructureTags.ON_SWAMP_EXPLORER_MAPS.location(), "swamp_hut", "filled_map.explorer_swamp")
                .sellsMap("igloo_map", structureTag("on_igloo_maps", BuiltinStructures.IGLOO.location()), "target_point", "filled_map.insanesurvivaloverhaul.igloo")
                .sellsMap("woodland_mansion_map", StructureTags.ON_WOODLAND_EXPLORER_MAPS.location(), "mansion", "filled_map.mansion")
                //Villages are all abandoned (zombie villagers only)
                .sellsMap("village_map", StructureTags.VILLAGE.location(), "target_point", "filled_map.insanesurvivaloverhaul.village")
                .sellsMap("trail_ruins_map", structureTag("on_trail_ruins_maps", BuiltinStructures.TRAIL_RUINS.location()), "target_point", "filled_map.insanesurvivaloverhaul.trail_ruins");
        wanderingTraderPool("common", 6)
                .sellsRandom("sapling", Items.OAK_SAPLING, ItemTags.SAPLINGS, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sellsRandom("flower", Items.DANDELION, FARMER_FLOWERS, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.CACTUS, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.SUGAR_CANE, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.BAMBOO, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.VINE, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.LILY_PAD, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.GLOW_LICHEN, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.MOSS_BLOCK, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.SMALL_DRIPLEAF, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.POINTED_DRIPSTONE, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.ROOTED_DIRT, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.KELP, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.SEA_PICKLE, WANDERING_TRADER_RARE_PLANTS, OTHER_ITEMS_USES)
                .sellsRandom("coral_block", Items.TUBE_CORAL_BLOCK, CORAL_BLOCKS, WANDERING_TRADER_RARE_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.RED_MUSHROOM, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.BROWN_MUSHROOM, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.PODZOL, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.MYCELIUM, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.PACKED_ICE, WANDERING_TRADER_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.BLUE_ICE, WANDERING_TRADER_RARE_PLANTS, OTHER_ITEMS_USES)
                .sells(Items.WHEAT_SEEDS, SEEDS, SEEDS_USES)
                .sells(Items.BEETROOT_SEEDS, SEEDS, SEEDS_USES)
                .sells(Crops.CARROT_SEEDS.get(), SEEDS, SEEDS_USES)
                .sells(Crops.ROOTED_POTATO.get(), SEEDS, SEEDS_USES)
                .sells(Berries.SWEET_BERRY_SEEDS.get(), SEEDS, SEEDS_USES);
    }

    /**
     * Local Flavours (report_villagers.md §6.16): every employed villager buys 1 random complex food for 1 emerald, once per restock.
     * The trade set {@link MerchantTrades#LOCAL_FLAVOURS} isn't a level: its offer is added before the level 1 offers (see {@link MerchantTrades#addLocalFlavoursOffer}).
     */
    private void localFlavours() {
        ResourceLocation tagId = MerchantTrades.LOCAL_FLAVOURS;
        this.tags.put(tagId, new ArrayList<>());
        JsonObject tradeSet = new JsonObject();
        tradeSet.addProperty("trades", "#" + tagId);
        tradeSet.addProperty("amount", 1);
        this.tradeSets.put(MerchantTrades.LOCAL_FLAVOURS, tradeSet);
        new Level(tagId.getPath(), tagId, XP_PER_LEVEL[0], null)
                .buysRandom("complex_food", Items.RABBIT_STEW, COMPLEX_FOODS, 1);
    }

    /**
     * Matches only water bottles (potions with the water potion and nothing else in their contents).
     */
    private static JsonObject waterBottleComponents() {
        JsonObject potionContents = new JsonObject();
        potionContents.addProperty("potion", "minecraft:water");
        JsonObject components = new JsonObject();
        components.add("minecraft:potion_contents", potionContents);
        return components;
    }

    //Quantities and uses per restock (report_villagers.md §5.3)
    public static final int SIMPLE_BLOCKS = 64, SIMPLE_BLOCKS_USES = 6;
    public static final int LOGS = 48, LOGS_USES = 6;
    public static final int PROCESSED_BLOCKS = 48, PROCESSED_BLOCKS_USES = 4;
    public static final int WOOL = 24, WOOL_USES = 4;
    public static final int SEEDS = 8, SEEDS_USES = 4;
    public static final int POTIONS = 2, POTIONS_USES = 2;
    public static final int ARROWS = 16, ARROWS_USES = 4;
    public static final int SPECIAL_ARROWS = 8, SPECIAL_ARROWS_USES = 4;
    public static final int REDSTONE_COMPONENTS_USES = 4;
    /**
     * Wandering trader plants and natural blocks: 24 the common ones, 4 the rare ones (blue ice, coral, sea pickles). report_villagers.md §6.15
     */
    public static final int WANDERING_TRADER_PLANTS = 24, WANDERING_TRADER_RARE_PLANTS = 4;
    public static final int SPECIAL_ITEMS_USES = 1;
    /**
     * Items with no category in report_villagers.md §5.3 (food, paper, decorations, compasses): a starting value.
     */
    public static final int OTHER_ITEMS_USES = 4;

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return this.registries.thenCompose(lookup -> {
            this.trades.clear();
            this.tags.clear();
            this.tradeSets.clear();
            this.structureTags.clear();
            this.potionTags.clear();
            this.gifts.clear();
            this.giftLootTables.clear();
            this.recyclingOutputs = readRecyclingOutputs();
            this.addTrades();
            this.writeGifts();
            this.validate(lookup);

            List<CompletableFuture<?>> futures = new ArrayList<>();
            this.trades.forEach((id, json) -> futures.add(DataProvider.saveStable(cache, json, dataPath(id, MerchantTradesReloadListener.TRADES_DIRECTORY))));
            this.tags.forEach((id, values) -> futures.add(DataProvider.saveStable(cache, tagJson(values), dataPath(id, MerchantTradesReloadListener.TAGS_DIRECTORY))));
            this.structureTags.forEach((id, values) -> futures.add(DataProvider.saveStable(cache, tagJson(values), dataPath(id, "tags/worldgen/structure"))));
            this.potionTags.forEach((id, values) -> futures.add(DataProvider.saveStable(cache, tagJson(values), dataPath(id, "tags/potion"))));
            this.tradeSets.forEach((id, json) -> futures.add(DataProvider.saveStable(cache, json, dataPath(id, MerchantTradesReloadListener.TRADE_SETS_DIRECTORY))));
            this.giftLootTables.forEach((id, json) -> futures.add(DataProvider.saveStable(cache, json, dataPath(id, "loot_table"))));
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    /**
     * Hero of the Village gifts (report_villagers.md §6.17): the loot table {@code <profession>_gift_level_<n>} (see {@link Villagers#heroGiftLootTable}) gifts 1 random item
     * the profession sells up to level n, in the amount of {@link TradeBuilder#giftCount}. Trades with a second cost are excluded.
     * When the first levels have nothing to gift (e.g. the smiths, whose level 1 sells only equipment), they get an empty loot table and their villagers gift nothing.
     */
    private void writeGifts() {
        this.gifts.forEach((professionId, levels) -> {
            List<GiftEntry> entries = new ArrayList<>();
            for (int level = 1; level <= 5; level++) {
                entries.addAll(levels.get(level - 1));
                JsonArray pools = new JsonArray();
                //Nothing to gift: the loot table is written without pools, so the villager gifts nothing instead of the vanilla gift
                if (!entries.isEmpty()) {
                    JsonArray entriesJson = new JsonArray();
                    entries.forEach(entry -> entriesJson.add(entry.toJson()));
                    JsonObject pool = new JsonObject();
                    pool.addProperty("rolls", 1);
                    pool.add("entries", entriesJson);
                    pools.add(pool);
                }
                JsonObject lootTable = new JsonObject();
                lootTable.addProperty("type", "minecraft:gift");
                lootTable.add("pools", pools);
                this.giftLootTables.put(Villagers.heroGiftLootTable(professionId, level), lootTable);
            }
        });
    }

    /**
     * Parses back every written file with the codecs the game uses, so a malformed trade fails {@code runData} instead of being skipped in game.
     */
    private void validate(HolderLookup.Provider lookup) {
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, lookup);
        this.trades.forEach((id, json) -> VillagerTrade.CODEC.parse(ops, json).getOrThrow(error -> new IllegalStateException("Invalid villager trade " + id + ": " + error)));
        this.tradeSets.forEach((id, json) -> TradeSet.CODEC.parse(ops, json).getOrThrow(error -> new IllegalStateException("Invalid trade set " + id + ": " + error)));
        this.giftLootTables.forEach((id, json) -> LootTable.DIRECT_CODEC.parse(ops, json).getOrThrow(error -> new IllegalStateException("Invalid gift loot table " + id + ": " + error)));
    }

    private Path dataPath(ResourceLocation id, String directory) {
        return this.packRoot.resolve("data").resolve(id.getNamespace()).resolve(directory).resolve(id.getPath() + ".json");
    }

    /**
     * Reads the full value output of every recycling recipe, by ingredient item id.
     */
    private Map<ResourceLocation, RecyclingOutput> readRecyclingOutputs() {
        Map<ResourceLocation, RecyclingOutput> outputs = new HashMap<>();
        try (Stream<Path> files = Files.walk(this.recyclingRecipesFolder)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
                try (Reader reader = Files.newBufferedReader(file)) {
                    JsonElement element = JsonParser.parseReader(reader);
                    if (!(element instanceof JsonObject json) || !(json.get("result") instanceof JsonObject result))
                        continue;
                    int count = result.has("count") ? result.get("count").getAsInt() : 1;
                    RecyclingOutput output = new RecyclingOutput(ResourceLocation.parse(result.get("id").getAsString()), count);
                    //The ingredient is an item or a list of items (e.g. every oxidation stage); tags can't be matched to a sold item and are ignored
                    JsonElement ingredient = json.get("ingredient");
                    List<JsonElement> entries = new ArrayList<>();
                    if (ingredient instanceof JsonArray array)
                        array.forEach(entries::add);
                    else if (ingredient != null)
                        entries.add(ingredient);
                    for (JsonElement entry : entries) {
                        if (entry instanceof JsonObject entryJson && entryJson.has("item"))
                            outputs.put(ResourceLocation.parse(entryJson.get("item").getAsString()), output);
                    }
                }
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to read the recycling recipes in " + this.recyclingRecipesFolder, e);
        }
        if (outputs.isEmpty())
            throw new IllegalStateException("No recycling recipes found in " + this.recyclingRecipesFolder);
        return outputs;
    }

    /**
     * The material cost of {@code quantity} items, following report_villagers.md §5.4: {@link #MATERIAL_PRICE_MULTIPLIER} of the full recycling output of the whole quantity, rounded down,
     * in the recycling output's unit, and in ingots when it's above {@link #MAX_NUGGETS} nuggets (it must then be a whole number of ingots).
     * Fails if the cost is below what recycling gives back for the quantity (ignoring {@code Recycling.minimumOutput}). Returns null when the cost is 0.
     */
    private Cost materialCost(ResourceLocation item, int quantity) {
        RecyclingOutput output = this.recyclingOutputs.get(item);
        if (output == null)
            throw new IllegalStateException("No recycling recipe for " + item + ", can't compute its material cost");
        int cost = (int) Math.floor(output.count() * quantity * MATERIAL_PRICE_MULTIPLIER);
        int recycledBack = quantity * (int) Math.floor(output.count() * MATERIAL_PRICE_MULTIPLIER);
        if (cost < recycledBack)
            throw new IllegalStateException("The material cost of " + quantity + " " + item + " (" + cost + " " + output.item() + ") is below what recycling gives back (" + recycledBack + ")");
        if (cost == 0)
            return null;
        ResourceLocation ingot = NUGGET_TO_INGOT.get(output.item());
        if (ingot != null && cost > MAX_NUGGETS) {
            if (cost % NUGGETS_PER_INGOT != 0)
                throw new IllegalStateException("The material cost of " + quantity + " " + item + " is " + cost + " " + output.item() + ", not a whole number of ingots: pick another quantity");
            return new Cost(ingot, cost / NUGGETS_PER_INGOT);
        }
        return new Cost(output.item(), cost);
    }

    /**
     * The cost of a filled bucket: {@link #MATERIAL_PRICE_MULTIPLIER} of the empty bucket's recycling output in ingots, rounded down.
     * Recycling the emptied bucket gives back a bit more than paid (accepted in report_villagers.md §5.4), so there's no check.
     */
    private Cost bucketCost() {
        RecyclingOutput output = this.recyclingOutputs.get(id(Items.BUCKET));
        if (output == null)
            throw new IllegalStateException("No recycling recipe for the bucket");
        ResourceLocation ingot = NUGGET_TO_INGOT.get(output.item());
        if (ingot == null)
            return new Cost(output.item(), (int) Math.floor(output.count() * MATERIAL_PRICE_MULTIPLIER));
        return new Cost(ingot, (int) Math.floor((float) output.count() / NUGGETS_PER_INGOT * MATERIAL_PRICE_MULTIPLIER));
    }

    private static ResourceLocation id(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem());
    }

    private static JsonObject tagJson(List<ResourceLocation> values) {
        JsonObject json = new JsonObject();
        JsonArray array = new JsonArray();
        values.forEach(value -> array.add(value.toString()));
        json.add("values", array);
        return json;
    }

    private static JsonObject itemJson(ResourceLocation item, int count) {
        JsonObject json = new JsonObject();
        json.addProperty("id", item.toString());
        json.addProperty("count", count);
        return json;
    }

    @Override
    public @NotNull String getName() {
        return "Villager Trades";
    }

    private record RecyclingOutput(ResourceLocation item, int count) {}

    private record Cost(ResourceLocation item, int count) {}

    /**
     * A gift loot table entry: {@code count} {@code item}, with the trade's item modifiers (random item, random potion).
     */
    private record GiftEntry(ResourceLocation item, int count, List<JsonObject> functions) {
        private JsonObject toJson() {
            JsonArray functionsJson = new JsonArray();
            if (this.count > 1) {
                JsonObject setCount = new JsonObject();
                setCount.addProperty("function", "minecraft:set_count");
                setCount.addProperty("count", this.count);
                functionsJson.add(setCount);
            }
            this.functions.forEach(functionsJson::add);
            JsonObject json = new JsonObject();
            json.addProperty("type", "minecraft:item");
            json.addProperty("name", this.item.toString());
            if (!functionsJson.isEmpty())
                json.add("functions", functionsJson);
            return json;
        }
    }

    /**
     * The trades of a profession. Creating it writes the trade sets of all its 5 levels.
     */
    protected class Profession {
        private final ResourceLocation id;

        public Profession(VillagerProfession profession) {
            this(BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession));
        }

        public Profession(ResourceLocation id) {
            this.id = id;
            List<List<GiftEntry>> levels = new ArrayList<>();
            for (int level = 1; level <= 5; level++)
                levels.add(new ArrayList<>());
            ISOVillagerTradesProvider.this.gifts.put(id, levels);
            for (int level = 1; level <= 5; level++)
                this.level(level);
        }

        /**
         * The trades of a level, creating its tag and its trade set ({@code remove: "all"}) if missing.
         */
        public Level level(int level) {
            if (level < 1 || level > 5)
                throw new IllegalArgumentException("Invalid villager level " + level);
            ResourceLocation tagId = InsaneSO.id(this.id.getPath() + "/level_" + level);
            if (!ISOVillagerTradesProvider.this.tags.containsKey(tagId)) {
                ISOVillagerTradesProvider.this.tags.put(tagId, new ArrayList<>());
                JsonObject tradeSet = new JsonObject();
                tradeSet.addProperty("trades", "#" + tagId);
                tradeSet.addProperty("remove", TradeSet.RemoveMode.ALL.getSerializedName());
                ISOVillagerTradesProvider.this.tradeSets.put(this.id.withSuffix("/level_" + level), tradeSet);
            }
            return new Level(this.id.getPath(), tagId, XP_PER_LEVEL[level - 1], ISOVillagerTradesProvider.this.gifts.get(this.id).get(level - 1));
        }
    }

    /**
     * A wandering trader pool ({@code buying}, {@code uncommon} or {@code common}): its trade tag {@code #insanesurvivaloverhaul:wandering_trader/<pool>}
     * and its trade set {@code minecraft:wandering_trader/<pool>}, with {@code remove: "all"} and {@code amount} trades rolled.
     */
    protected Level wanderingTraderPool(String pool, int amount) {
        ResourceLocation tagId = InsaneSO.id(WANDERING_TRADER + "/" + pool);
        if (this.tags.containsKey(tagId))
            throw new IllegalStateException("Duplicate wandering trader pool " + pool);
        this.tags.put(tagId, new ArrayList<>());
        JsonObject tradeSet = new JsonObject();
        tradeSet.addProperty("trades", "#" + tagId);
        tradeSet.addProperty("amount", amount);
        tradeSet.addProperty("remove", TradeSet.RemoveMode.ALL.getSerializedName());
        this.tradeSets.put(ResourceLocation.withDefaultNamespace(WANDERING_TRADER + "/" + pool), tradeSet);
        return new Level(WANDERING_TRADER, tagId, WANDERING_TRADER_XP, null);
    }

    /**
     * Helpers for the common trade shapes of a profession level or of a wandering trader pool. Every trade costs 1 emerald, or gives 1 emerald ({@link #buys}).
     */
    protected class Level {
        /**
         * Folder of the trade files, e.g. the profession's path
         */
        private final String folder;
        private final ResourceLocation tagId;
        private final int xp;
        /**
         * Where the giftable trades go (Hero of the Village gifts), null if this isn't a profession level
         */
        @Nullable
        private final List<GiftEntry> gifts;

        private Level(String folder, ResourceLocation tagId, int xp, @Nullable List<GiftEntry> gifts) {
            this.folder = folder;
            this.tagId = tagId;
            this.xp = xp;
            this.gifts = gifts;
        }

        /**
         * 1 emerald → {@link #SIMPLE_BLOCKS} blocks, {@link #SIMPLE_BLOCKS_USES} uses.
         */
        public Level simpleBlock(ItemLike item) {
            return this.sells(item, SIMPLE_BLOCKS, SIMPLE_BLOCKS_USES);
        }

        /**
         * 1 emerald → {@link #LOGS} logs, {@link #LOGS_USES} uses.
         */
        public Level log(ItemLike item) {
            return this.sells(item, LOGS, LOGS_USES);
        }

        /**
         * 1 emerald → {@link #PROCESSED_BLOCKS} blocks, {@link #PROCESSED_BLOCKS_USES} uses.
         */
        public Level processedBlock(ItemLike item) {
            return this.sells(item, PROCESSED_BLOCKS, PROCESSED_BLOCKS_USES);
        }

        /**
         * 1 emerald → {@code count} items. Named after the item.
         */
        public Level sells(ItemLike item, int count, int uses) {
            return this.sells(id(item).getPath(), id(item), count, uses);
        }

        public Level sells(String name, ResourceLocation item, int count, int uses) {
            return this.add(name, new TradeBuilder(item, count, uses));
        }

        /**
         * 1 emerald + {@code costCount} {@code cost} → {@code count} items. Named after the item.
         */
        public Level sellsFor(ItemLike item, int count, int uses, ItemLike cost, int costCount) {
            return this.add(id(item).getPath(), new TradeBuilder(id(item), count, uses).additionalWants(new Cost(id(cost), costCount)));
        }

        /**
         * 1 emerald → {@code count} of a random item from {@code items}, picked when the offer is created. {@code item} is the item written in the file before the pick.
         */
        public Level sellsRandom(String name, ItemLike item, TagKey<Item> items, int count, int uses) {
            JsonObject function = new JsonObject();
            function.addProperty("function", "insanesurvivaloverhaul:set_random_item");
            function.addProperty("items", "#" + items.location());
            return this.add(name, new TradeBuilder(id(item), count, uses).function(function));
        }

        /**
         * 1 emerald → {@link #POTIONS} potions ({@code item}: potion, splash or lingering potion) of a random potion from {@code potions}, picked when the offer is created.
         * {@link #POTIONS_USES} uses. The stack size isn't checked: ISO makes potions stackable, but its stack sizes aren't applied in data generation.
         */
        public Level sellsRandomPotion(String name, ItemLike item, ResourceLocation potions) {
            JsonObject function = new JsonObject();
            function.addProperty("function", "insanesurvivaloverhaul:set_random_potion");
            function.addProperty("potions", "#" + potions);
            return this.add(name, new TradeBuilder(id(item), POTIONS, POTIONS_USES, false).function(function));
        }

        /**
         * 1 emerald → {@link #SPECIAL_ARROWS} tipped arrows of a random potion from {@code potions}, picked when the offer is created. {@link #SPECIAL_ARROWS_USES} uses.
         */
        public Level sellsRandomTippedArrow(String name, ResourceLocation potions) {
            JsonObject function = new JsonObject();
            function.addProperty("function", "insanesurvivaloverhaul:set_random_potion");
            function.addProperty("potions", "#" + potions);
            return this.add(name, new TradeBuilder(id(Items.TIPPED_ARROW), SPECIAL_ARROWS, SPECIAL_ARROWS_USES).function(function));
        }

        /**
         * 1 emerald + the item's material (report_villagers.md §5.4) → {@code count} items. Only the emerald if the material cost rounds down to 0.
         */
        public Level sellsWithMaterial(ItemLike item, int count, int uses) {
            return this.sellsWithMaterial(id(item).getPath(), id(item), count, uses);
        }

        public Level sellsWithMaterial(String name, ResourceLocation item, int count, int uses) {
            return this.add(name, new TradeBuilder(item, count, uses).additionalWants(materialCost(item, count)));
        }

        /**
         * 1 emerald + the item's material (report_villagers.md §5.4) → 1 item, {@link #SPECIAL_ITEMS_USES} uses. For equipment and workstations with a recycling recipe.
         */
        public Level sellsEquipment(ItemLike item) {
            return this.sellsWithMaterial(item, 1, SPECIAL_ITEMS_USES);
        }

        /**
         * 1 emerald + {@link #bucketCost()} → 1 filled bucket (lava, fish), {@link #SPECIAL_ITEMS_USES} uses.
         */
        public Level sellsFilledBucket(ItemLike bucket) {
            return this.add(id(bucket).getPath(), new TradeBuilder(id(bucket), 1, SPECIAL_ITEMS_USES).additionalWants(bucketCost()));
        }

        /**
         * 1 emerald + 1 empty map → an explorer map to the nearest structure in {@code destination}, {@link #SPECIAL_ITEMS_USES} uses.
         *
         * @param decoration the map decoration type, e.g. {@code monument}
         * @param mapName    the translation key of the map name, e.g. {@code filled_map.monument}
         */
        public Level sellsMap(String name, ResourceLocation destination, String decoration, String mapName) {
            JsonObject explorationMap = new JsonObject();
            explorationMap.addProperty("function", "minecraft:exploration_map");
            explorationMap.addProperty("destination", destination.toString());
            explorationMap.addProperty("decoration", decoration);
            explorationMap.addProperty("zoom", 2);
            explorationMap.addProperty("search_radius", 100);
            explorationMap.addProperty("skip_existing_chunks", true);
            JsonObject setName = new JsonObject();
            setName.addProperty("function", "minecraft:set_name");
            JsonObject text = new JsonObject();
            text.addProperty("translate", mapName);
            setName.add("name", text);
            setName.addProperty("target", "item_name");
            return this.add(name, new TradeBuilder(id(Items.MAP), 1, SPECIAL_ITEMS_USES)
                    .additionalWants(new Cost(id(Items.MAP), 1))
                    .function(explorationMap)
                    .function(setName));
        }

        /**
         * 1 emerald + 1 book → a book enchanted as at an enchanting table with {@code levels} levels, only with enchantments from {@code enchantments}, {@link #SPECIAL_ITEMS_USES} uses.
         * Higher levels give stronger (and sometimes more) enchantments.
         */
        public Level sellsEnchantedBook(String name, ResourceLocation enchantments, int levels) {
            JsonObject function = new JsonObject();
            function.addProperty("function", "minecraft:enchant_with_levels");
            function.addProperty("levels", levels);
            function.addProperty("options", "#" + enchantments);
            return this.add(name, new TradeBuilder(id(Items.BOOK), 1, SPECIAL_ITEMS_USES)
                    .additionalWants(new Cost(id(Items.BOOK), 1))
                    .function(function));
        }

        /**
         * {@code count} {@code item} → 1 emerald, {@link #SPECIAL_ITEMS_USES} uses. Named after the item.
         */
        public Level buys(ItemLike item, int count) {
            return this.buys(id(item).getPath(), item, count, null);
        }

        /**
         * {@code count} {@code item} with exactly {@code components} (e.g. a water bottle's potion contents) → 1 emerald, {@link #SPECIAL_ITEMS_USES} uses.
         */
        public Level buys(String name, ItemLike item, int count, @Nullable JsonObject components) {
            return this.add(name, new TradeBuilder(id(Items.EMERALD), 1, SPECIAL_ITEMS_USES).wants(new Cost(id(item), count), components));
        }

        /**
         * {@code count} of a random item from {@code items} (ISO's {@code random_item}), picked when the offer is created → 1 emerald, {@link #SPECIAL_ITEMS_USES} uses.
         * {@code item} is the item written in {@code id}, used if the tag is empty.
         */
        public Level buysRandom(String name, ItemLike item, TagKey<Item> items, int count) {
            return this.add(name, new TradeBuilder(id(Items.EMERALD), 1, SPECIAL_ITEMS_USES).wants(new Cost(id(item), count), null).wantsRandomItem(items));
        }

        private Level add(String name, TradeBuilder builder) {
            ResourceLocation tradeId = InsaneSO.id(this.folder + "/" + name);
            if (ISOVillagerTradesProvider.this.trades.containsKey(tradeId))
                throw new IllegalStateException("Duplicate villager trade " + tradeId);
            ISOVillagerTradesProvider.this.trades.put(tradeId, builder.toJson(this.xp));
            ISOVillagerTradesProvider.this.tags.get(this.tagId).add(tradeId);
            if (this.gifts != null && builder.isGiftable())
                this.gifts.add(new GiftEntry(builder.gives, builder.giftCount(), List.copyOf(builder.functions)));
            return this;
        }
    }

    /**
     * A trade file, with every field written explicitly (eases the move to the 26.3 format, where they are required).
     */
    private static class TradeBuilder {
        private final ResourceLocation gives;
        private final int count;
        private final int uses;
        private Cost wants = new Cost(id(Items.EMERALD), 1);
        @Nullable
        private JsonObject wantsComponents;
        @Nullable
        private TagKey<Item> wantsRandomItem;
        private Cost additionalWants;
        private final List<JsonObject> functions = new ArrayList<>();

        private TradeBuilder(ResourceLocation gives, int count, int uses) {
            this(gives, count, uses, true);
        }

        private TradeBuilder(ResourceLocation gives, int count, int uses, boolean checkStackSize) {
            Item item = BuiltInRegistries.ITEM.get(gives);
            //ISO's stack sizes aren't applied in data generation, so this is the vanilla stack size (a lower bound)
            if (checkStackSize && item != Items.AIR && count > item.getDefaultMaxStackSize())
                throw new IllegalStateException("Villager trade gives " + count + " " + gives + ", above its stack size");
            this.gives = gives;
            this.count = count;
            this.uses = uses;
        }

        /**
         * Replaces the 1 emerald asked by default (buying trades).
         */
        private TradeBuilder wants(Cost cost, @Nullable JsonObject components) {
            Item item = BuiltInRegistries.ITEM.get(cost.item());
            if (item != Items.AIR && cost.count() > item.getDefaultMaxStackSize())
                throw new IllegalStateException("Villager trade wants " + cost.count() + " " + cost.item() + ", above its stack size");
            this.wants = cost;
            this.wantsComponents = components;
            return this;
        }

        /**
         * The wanted item is picked from {@code items} when the offer is created (ISO's {@code random_item}).
         */
        private TradeBuilder wantsRandomItem(TagKey<Item> items) {
            this.wantsRandomItem = items;
            return this;
        }

        private TradeBuilder additionalWants(Cost cost) {
            if (cost != null) {
                Item item = BuiltInRegistries.ITEM.get(cost.item());
                if (item != Items.AIR && cost.count() > item.getDefaultMaxStackSize())
                    throw new IllegalStateException("Villager trade costs " + cost.count() + " " + cost.item() + ", above its stack size");
            }
            this.additionalWants = cost;
            return this;
        }

        private TradeBuilder function(JsonObject function) {
            this.functions.add(function);
            return this;
        }

        /**
         * Selling trades without a second cost can be gifted (report_villagers.md §6.17).
         */
        private boolean isGiftable() {
            return this.additionalWants == null && this.wants.item().equals(id(Items.EMERALD)) && !this.gives.equals(id(Items.EMERALD));
        }

        /**
         * The gifted amount: 1 for single items, half the trade's quantity (rounded down) otherwise.
         */
        private int giftCount() {
            return Math.max(1, this.count / 2);
        }

        private JsonObject toJson(int xp) {
            JsonObject json = new JsonObject();
            JsonObject wantsJson = itemJson(this.wants.item(), this.wants.count());
            if (this.wantsComponents != null)
                wantsJson.add("components", this.wantsComponents);
            if (this.wantsRandomItem != null)
                wantsJson.addProperty("random_item", "#" + this.wantsRandomItem.location());
            json.add("wants", wantsJson);
            if (this.additionalWants != null)
                json.add("additional_wants", itemJson(this.additionalWants.item(), this.additionalWants.count()));
            json.add("gives", itemJson(this.gives, this.count));
            if (!this.functions.isEmpty()) {
                JsonArray array = new JsonArray();
                this.functions.forEach(array::add);
                json.add("given_item_modifiers", array);
            }
            json.addProperty("max_uses", this.uses);
            json.addProperty("reputation_discount", REPUTATION_DISCOUNT);
            json.addProperty("xp", xp);
            return json;
        }
    }
}
