package insane96mcp.insanesurvivaloverhaul.data.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade.MerchantTradesReloadListener;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade.TradeSet;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade.VillagerTrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

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
 *     <li>one trade set per level: {@code minecraft:<profession>/level_<n>}, with {@code remove: "all"}, written for every level of a defined profession, empty ones too.</li>
 * </ul>
 * Every trade costs 1 emerald, with {@code reputation_discount} {@link #REPUTATION_DISCOUNT}.
 * Items that recycle into diamonds, iron, gold or copper also cost {@link #MATERIAL_PRICE_MULTIPLIER} of their recycling output, read from the recycling recipes of the {@code recycling} integrated pack.
 * <p>
 * Files of trades removed from here are not deleted: delete them by hand.
 */
public class ISOVillagerTradesProvider implements DataProvider {
    public static final TagKey<Item> DYED_TERRACOTTA = ISOItemTagsProvider.create("dyed_terracotta");

    public static final float REPUTATION_DISCOUNT = 0.2f;
    /**
     * Share of the recycling output (at full value) asked as second cost. It's {@code Recycling.outputMultiplier}'s default, fixed so the prices don't depend on the config.
     */
    public static final float MATERIAL_PRICE_MULTIPLIER = 0.75f;
    /**
     * Merchant experience per trade, by level. Vanilla's values for selling trades.
     */
    private static final int[] XP_PER_LEVEL = {1, 5, 10, 15, 30};
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
    private Map<ResourceLocation, RecyclingOutput> recyclingOutputs;

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
        mason();
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

    //Quantities and uses per restock (report_villagers.md §5.3)
    public static final int SIMPLE_BLOCKS = 64, SIMPLE_BLOCKS_USES = 6;
    public static final int LOGS = 48, LOGS_USES = 6;
    public static final int PROCESSED_BLOCKS = 48, PROCESSED_BLOCKS_USES = 4;
    public static final int SPECIAL_ITEMS_USES = 1;

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return this.registries.thenCompose(lookup -> {
            this.trades.clear();
            this.tags.clear();
            this.tradeSets.clear();
            this.recyclingOutputs = readRecyclingOutputs();
            this.addTrades();
            this.validate(lookup);

            List<CompletableFuture<?>> futures = new ArrayList<>();
            this.trades.forEach((id, json) -> futures.add(DataProvider.saveStable(cache, json, dataPath(id, MerchantTradesReloadListener.TRADES_DIRECTORY))));
            this.tags.forEach((id, values) -> {
                JsonObject json = new JsonObject();
                JsonArray array = new JsonArray();
                values.forEach(value -> array.add(value.toString()));
                json.add("values", array);
                futures.add(DataProvider.saveStable(cache, json, dataPath(id, MerchantTradesReloadListener.TAGS_DIRECTORY)));
            });
            this.tradeSets.forEach((id, json) -> futures.add(DataProvider.saveStable(cache, json, dataPath(id, MerchantTradesReloadListener.TRADE_SETS_DIRECTORY))));
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    /**
     * Parses back every written file with the codecs the game uses, so a malformed trade fails {@code runData} instead of being skipped in game.
     */
    private void validate(HolderLookup.Provider lookup) {
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, lookup);
        this.trades.forEach((id, json) -> VillagerTrade.CODEC.parse(ops, json).getOrThrow(error -> new IllegalStateException("Invalid villager trade " + id + ": " + error)));
        this.tradeSets.forEach((id, json) -> TradeSet.CODEC.parse(ops, json).getOrThrow(error -> new IllegalStateException("Invalid trade set " + id + ": " + error)));
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
     * The trades of a profession. Creating it writes the trade sets of all its 5 levels.
     */
    protected class Profession {
        private final ResourceLocation id;

        public Profession(VillagerProfession profession) {
            this(BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession));
        }

        public Profession(ResourceLocation id) {
            this.id = id;
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
            return new Level(this, level, tagId);
        }
    }

    /**
     * Helpers for the common trade shapes of a profession level. Every trade costs 1 emerald.
     */
    protected class Level {
        private final Profession profession;
        private final int level;
        private final ResourceLocation tagId;

        private Level(Profession profession, int level, ResourceLocation tagId) {
            this.profession = profession;
            this.level = level;
            this.tagId = tagId;
        }

        /**
         * 1 emerald → {@link #SIMPLE_BLOCKS} blocks, {@link #SIMPLE_BLOCKS_USES} uses.
         */
        public Level simpleBlock(ItemLike item) {
            return this.sells(item, SIMPLE_BLOCKS, SIMPLE_BLOCKS_USES);
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
         * 1 emerald → {@code count} of a random item from {@code items}, picked when the offer is created. {@code item} is the item written in the file before the pick.
         */
        public Level sellsRandom(String name, ItemLike item, TagKey<Item> items, int count, int uses) {
            JsonObject function = new JsonObject();
            function.addProperty("function", "insanesurvivaloverhaul:set_random_item");
            function.addProperty("items", "#" + items.location());
            return this.add(name, new TradeBuilder(id(item), count, uses).function(function));
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
         * 1 emerald + 1 book → a book with a random enchantment from {@code enchantments} at a random level, {@link #SPECIAL_ITEMS_USES} uses.
         */
        public Level sellsEnchantedBook(String name, ResourceLocation enchantments) {
            JsonObject function = new JsonObject();
            function.addProperty("function", "minecraft:enchant_randomly");
            function.addProperty("options", "#" + enchantments);
            return this.add(name, new TradeBuilder(id(Items.BOOK), 1, SPECIAL_ITEMS_USES)
                    .additionalWants(new Cost(id(Items.BOOK), 1))
                    .function(function));
        }

        private Level add(String name, TradeBuilder builder) {
            ResourceLocation tradeId = InsaneSO.id(this.profession.id.getPath() + "/" + name);
            if (ISOVillagerTradesProvider.this.trades.containsKey(tradeId))
                throw new IllegalStateException("Duplicate villager trade " + tradeId);
            ISOVillagerTradesProvider.this.trades.put(tradeId, builder.toJson(XP_PER_LEVEL[this.level - 1]));
            ISOVillagerTradesProvider.this.tags.get(this.tagId).add(tradeId);
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
        private Cost additionalWants;
        private final List<JsonObject> functions = new ArrayList<>();

        private TradeBuilder(ResourceLocation gives, int count, int uses) {
            Item item = BuiltInRegistries.ITEM.get(gives);
            //ISO's stack sizes aren't applied in data generation, so this is the vanilla stack size (a lower bound)
            if (item != Items.AIR && count > item.getDefaultMaxStackSize())
                throw new IllegalStateException("Villager trade gives " + count + " " + gives + ", above its stack size");
            this.gives = gives;
            this.count = count;
            this.uses = uses;
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

        private JsonObject toJson(int xp) {
            JsonObject json = new JsonObject();
            json.add("wants", itemJson(id(Items.EMERALD), 1));
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
