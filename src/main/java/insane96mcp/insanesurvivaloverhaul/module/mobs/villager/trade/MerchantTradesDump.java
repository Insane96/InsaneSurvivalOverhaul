package insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade;

import com.google.gson.*;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.MerchantTrades;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModFileInfo;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * {@code /insanesurvivaloverhaul villager_trades dump [<profession>|wandering_trader|all] [vanilla|modded|all]}
 * <p>
 * Listings are anonymous classes, so each one is sampled by calling {@code getOffer} several times with a fake merchant (not added to the level),
 * for every villager type. The listings are read from the snapshot taken before ISO's changes, so mod trades removed by {@code remove: "all"} show up too.
 * <p>
 * The output is a data pack in {@code logs/insanesurvivaloverhaul/villager_trades_<date-time>/}, in ISO's trade format: one file per trade, the tags listing them per level,
 * and, when dumping every origin, the trade sets. A listing that gives different items is split in one file per item. Random enchantments, potions, colours, etc. are not
 * rules that can be read back: the {@code _dump} object of each file shows the samples.
 * Exploration map listings are skipped, since they search for structures and can freeze the server.
 */
public class MerchantTradesDump {
    public static final ResourceLocation ALL_TARGET = ResourceLocation.withDefaultNamespace("all");
    public static final ResourceLocation WANDERING_TRADER_TARGET = ResourceLocation.withDefaultNamespace("wandering_trader");

    private static final int SAMPLES_PER_VILLAGER_TYPE = 5;
    private static final int WANDERING_TRADER_SAMPLES = 25;
    private static final int MAX_SAMPLE_COMPONENTS = 5;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public enum Origin {
        VANILLA, MODDED, ALL;

        boolean accepts(boolean vanilla) {
            return this == ALL || (this == VANILLA) == vanilla;
        }
    }

    private final CommandSourceStack source;
    private final ServerLevel level;
    private final Vec3 position;
    private final Origin origin;
    private final Path root;
    private final DynamicOps<JsonElement> ops;
    private final Set<ResourceLocation> usedIds = new HashSet<>();
    private final List<String> skipped = new ArrayList<>();
    private int listings;
    private int trades;

    private MerchantTradesDump(CommandSourceStack source, Origin origin) {
        this.source = source;
        this.level = source.getLevel();
        this.position = source.getPosition();
        this.origin = origin;
        this.root = FMLPaths.GAMEDIR.get().resolve("logs").resolve(InsaneSO.MOD_ID).resolve("villager_trades_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")));
        this.ops = this.level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
    }

    public static int dump(CommandSourceStack source, ResourceLocation target, Origin origin) {
        boolean all = target.equals(ALL_TARGET);
        boolean wanderingTrader = target.equals(WANDERING_TRADER_TARGET);
        VillagerProfession profession = null;
        if (!all && !wanderingTrader) {
            profession = BuiltInRegistries.VILLAGER_PROFESSION.getOptional(target).orElse(null);
            if (profession == null || !MerchantTrades.getVillagerSnapshot().containsKey(profession)) {
                source.sendFailure(Component.literal("Unknown profession, or profession without trades: " + target));
                return 0;
            }
        }

        MerchantTradesDump dump = new MerchantTradesDump(source, origin);
        try {
            if (all || wanderingTrader)
                dump.dumpWanderingTrader();
            if (all) {
                for (var entry : MerchantTrades.getVillagerSnapshot().entrySet())
                    dump.dumpProfession(entry.getKey(), entry.getValue());
            }
            else if (profession != null)
                dump.dumpProfession(profession, MerchantTrades.getVillagerSnapshot().get(profession));
            dump.writeJson(dump.root.resolve("pack.mcmeta"), packMcmeta());
            if (!dump.skipped.isEmpty())
                Files.writeString(dump.root.resolve("skipped.txt"), String.join(System.lineSeparator(), dump.skipped));
        }
        catch (IOException e) {
            InsaneSO.LOGGER.error("Failed to write the villager trades dump", e);
            source.sendFailure(Component.literal("Failed to write the villager trades dump: " + e.getMessage()));
            return 0;
        }

        String path = dump.root.toAbsolutePath().toString();
        source.sendSuccess(() -> Component.literal("Dumped %d trades from %d listings (%d skipped, see skipped.txt) to ".formatted(dump.trades, dump.listings, dump.skipped.size()))
                .append(Component.literal(path).withStyle(style -> style.withUnderlined(true).withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path)))), false);
        return dump.trades;
    }

    private void dumpWanderingTrader() throws IOException {
        WanderingTrader trader = new WanderingTrader(EntityType.WANDERING_TRADER, this.level);
        trader.setPos(this.position);
        for (var entry : MerchantTrades.getWanderingTraderSnapshot().entrySet()) {
            ResourceLocation pool = entry.getKey();
            List<ResourceLocation> ids = new ArrayList<>();
            for (VillagerTrades.ItemListing listing : entry.getValue()) {
                if (!this.origin.accepts(MerchantTrades.isVanillaListing(listing)))
                    continue;
                List<Sample> samples = new ArrayList<>();
                for (int i = 0; i < WANDERING_TRADER_SAMPLES; i++) {
                    if (!this.sample(listing, trader, null, samples, pool.toString()))
                        break;
                }
                ids.addAll(this.writeListing(listing, samples, pool.getPath(), pool.toString(), null));
            }
            int amount = pool.equals(MerchantTrades.WANDERING_TRADER_COMMON) ? 5 : 1;
            this.writePool(pool, ids, amount);
        }
    }

    private void dumpProfession(VillagerProfession profession, Int2ObjectMap<List<VillagerTrades.ItemListing>> listingsByLevel) throws IOException {
        ResourceLocation professionId = BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession);
        Villager villager = new Villager(EntityType.VILLAGER, this.level);
        villager.setPos(this.position);
        List<VillagerType> villagerTypes = BuiltInRegistries.VILLAGER_TYPE.stream().toList();
        for (int villagerLevel = 1; villagerLevel <= 5; villagerLevel++) {
            List<VillagerTrades.ItemListing> listings = listingsByLevel.get(villagerLevel);
            if (listings == null || listings.isEmpty())
                continue;
            String location = professionId + " level " + villagerLevel;
            List<ResourceLocation> ids = new ArrayList<>();
            for (VillagerTrades.ItemListing listing : listings) {
                if (!this.origin.accepts(MerchantTrades.isVanillaListing(listing)))
                    continue;
                List<Sample> samples = new ArrayList<>();
                sampling:
                for (VillagerType villagerType : villagerTypes) {
                    villager.setVillagerData(new VillagerData(villagerType, profession, villagerLevel));
                    for (int i = 0; i < SAMPLES_PER_VILLAGER_TYPE; i++) {
                        if (!this.sample(listing, villager, villagerType, samples, location))
                            break sampling;
                    }
                }
                ids.addAll(this.writeListing(listing, samples, professionId.getPath() + "/level_" + villagerLevel, location, villagerTypes));
            }
            this.writePool(ResourceLocation.fromNamespaceAndPath(professionId.getNamespace(), professionId.getPath() + "/level_" + villagerLevel), ids, null);
        }
    }

    /**
     * Samples the listing once. Returns false if the listing must not be sampled anymore (skipped or failing).
     */
    private boolean sample(VillagerTrades.ItemListing listing, AbstractVillager merchant, @Nullable VillagerType villagerType, List<Sample> samples, String location) {
        if (listing instanceof VillagerTrades.TreasureMapForEmeralds) {
            this.skip(location, listing, "exploration map: searching structures can freeze the server");
            return false;
        }
        try {
            MerchantOffer offer = listing.getOffer(merchant, merchant.getRandom());
            if (offer != null)
                samples.add(new Sample(offer, villagerType));
            return true;
        }
        catch (Exception e) {
            this.skip(location, listing, "exception while sampling: " + e);
            return false;
        }
    }

    private void skip(String location, VillagerTrades.ItemListing listing, String reason) {
        this.skipped.add("%s: %s (%s): %s".formatted(location, listing.getClass().getName(), modIdOf(listing), reason));
    }

    /**
     * Writes one trade file per item shape (cost A, cost B, result) produced by the listing. Returns the ids of the written trades.
     */
    private List<ResourceLocation> writeListing(VillagerTrades.ItemListing listing, List<Sample> samples, String basePath, String location, @Nullable List<VillagerType> villagerTypes) throws IOException {
        if (samples.isEmpty()) {
            if (this.skipped.stream().noneMatch(s -> s.startsWith(location + ": " + listing.getClass().getName())))
                this.skip(location, listing, "no offer in any sample");
            return List.of();
        }
        this.listings++;
        boolean vanilla = MerchantTrades.isVanillaListing(listing);
        String modId = vanilla ? "minecraft" : modIdOf(listing);

        Map<String, List<Sample>> shapes = new LinkedHashMap<>();
        for (Sample sample : samples)
            shapes.computeIfAbsent(sample.shapeKey(), k -> new ArrayList<>()).add(sample);

        List<ResourceLocation> ids = new ArrayList<>();
        for (List<Sample> shape : shapes.values()) {
            MerchantOffer first = shape.getFirst().offer();
            ResourceLocation id = this.uniqueId(ResourceLocation.fromNamespaceAndPath(namespaceOf(modId), basePath + "/" + BuiltInRegistries.ITEM.getKey(first.getResult().getItem()).getPath()));
            JsonObject json = new JsonObject();

            JsonObject dumpInfo = new JsonObject();
            dumpInfo.addProperty("listing", listing.getClass().getName());
            dumpInfo.addProperty("origin", vanilla ? "vanilla" : "modded");
            dumpInfo.addProperty("mod", modId);
            dumpInfo.addProperty("samples", "%d of %d".formatted(shape.size(), samples.size()));
            if (shapes.size() > 1)
                dumpInfo.addProperty("note", "The listing gives %d different items: this file is one of them".formatted(shapes.size()));
            json.add("_dump", dumpInfo);

            json.add("wants", this.costJson(shape.stream().map(s -> s.offer().getItemCostA()).toList()));
            if (first.getItemCostB().isPresent())
                json.add("additional_wants", this.costJson(shape.stream().map(s -> s.offer().getItemCostB().orElseThrow()).toList()));

            List<ItemStack> results = shape.stream().map(s -> s.offer().getResult()).toList();
            JsonObject gives = new JsonObject();
            gives.addProperty("id", BuiltInRegistries.ITEM.getKey(first.getResult().getItem()).toString());
            int minCount = results.stream().mapToInt(ItemStack::getCount).min().orElse(1);
            int maxCount = results.stream().mapToInt(ItemStack::getCount).max().orElse(1);
            gives.addProperty("count", minCount);
            List<DataComponentPatch> patches = results.stream().map(ItemStack::getComponentsPatch).distinct().toList();
            if (patches.size() == 1) {
                if (!patches.getFirst().isEmpty())
                    DataComponentPatch.CODEC.encodeStart(this.ops, patches.getFirst()).ifSuccess(components -> gives.add("components", components));
            }
            else {
                dumpInfo.addProperty("gives_components", "Vary between samples (random enchantments, potions, colours...): add given_item_modifiers to reproduce them");
                JsonArray sampleComponents = new JsonArray();
                patches.stream().limit(MAX_SAMPLE_COMPONENTS).forEach(patch -> DataComponentPatch.CODEC.encodeStart(this.ops, patch).ifSuccess(sampleComponents::add));
                dumpInfo.add("gives_components_samples", sampleComponents);
            }
            json.add("gives", gives);
            if (minCount != maxCount) {
                JsonArray modifiers = new JsonArray();
                JsonObject setCount = new JsonObject();
                setCount.addProperty("function", "minecraft:set_count");
                setCount.add("count", intProvider(minCount, maxCount));
                modifiers.add(setCount);
                json.add("given_item_modifiers", modifiers);
            }

            json.add("max_uses", intProvider(shape.stream().mapToInt(s -> s.offer().getMaxUses()).min().orElse(1), shape.stream().mapToInt(s -> s.offer().getMaxUses()).max().orElse(1)));
            json.add("reputation_discount", floatProvider(shape.stream().map(s -> s.offer().getPriceMultiplier()).toList()));
            json.add("xp", intProvider(shape.stream().mapToInt(s -> s.offer().getXp()).min().orElse(0), shape.stream().mapToInt(s -> s.offer().getXp()).max().orElse(0)));

            if (villagerTypes != null) {
                List<VillagerType> shapeTypes = shape.stream().map(Sample::villagerType).filter(Objects::nonNull).distinct().toList();
                if (!shapeTypes.isEmpty() && shapeTypes.size() < villagerTypes.size()) {
                    json.add("merchant_predicate", villagerTypePredicate(shapeTypes));
                    dumpInfo.addProperty("villager_types", "Only seen on some villager types: merchant_predicate added");
                }
            }

            this.writeJson(this.root.resolve("data").resolve(id.getNamespace()).resolve(MerchantTradesReloadListener.TRADES_DIRECTORY).resolve(id.getPath() + ".json"), json);
            ids.add(id);
            this.trades++;
        }
        return ids;
    }

    /**
     * Writes the tag listing the pool's trades and, when every origin is dumped, the trade set replacing every existing listing.
     */
    private void writePool(ResourceLocation pool, List<ResourceLocation> ids, @Nullable Integer amount) throws IOException {
        if (ids.isEmpty())
            return;
        JsonObject tag = new JsonObject();
        JsonArray values = new JsonArray();
        ids.forEach(id -> values.add(id.toString()));
        tag.add("values", values);
        this.writeJson(this.root.resolve("data").resolve(pool.getNamespace()).resolve(MerchantTradesReloadListener.TAGS_DIRECTORY).resolve(pool.getPath() + ".json"), tag);

        if (this.origin != Origin.ALL)
            return;
        JsonObject tradeSet = new JsonObject();
        tradeSet.addProperty("trades", "#" + pool);
        if (amount != null)
            tradeSet.addProperty("amount", amount);
        tradeSet.addProperty("remove", TradeSet.RemoveMode.ALL.getSerializedName());
        this.writeJson(this.root.resolve("data").resolve(pool.getNamespace()).resolve(MerchantTradesReloadListener.TRADE_SETS_DIRECTORY).resolve(pool.getPath() + ".json"), tradeSet);
    }

    private JsonObject costJson(List<ItemCost> costs) {
        ItemCost first = costs.getFirst();
        JsonObject json = new JsonObject();
        json.addProperty("id", BuiltInRegistries.ITEM.getKey(first.item().value()).toString());
        json.add("count", intProvider(costs.stream().mapToInt(ItemCost::count).min().orElse(1), costs.stream().mapToInt(ItemCost::count).max().orElse(1)));
        List<DataComponentPredicate> predicates = costs.stream().map(ItemCost::components).distinct().toList();
        if (predicates.size() == 1 && !predicates.getFirst().equals(DataComponentPredicate.EMPTY))
            DataComponentPredicate.CODEC.encodeStart(this.ops, predicates.getFirst()).ifSuccess(components -> json.add("components", components));
        return json;
    }

    private static JsonElement intProvider(int min, int max) {
        if (min == max)
            return new JsonPrimitive(min);
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:uniform");
        json.addProperty("min", min);
        json.addProperty("max", max);
        return json;
    }

    private static JsonElement floatProvider(List<Float> values) {
        float min = values.stream().min(Float::compare).orElse(0f);
        float max = values.stream().max(Float::compare).orElse(0f);
        if (min == max)
            return new JsonPrimitive(min);
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:uniform");
        json.addProperty("min", min);
        json.addProperty("max", max);
        return json;
    }

    private static JsonObject villagerTypePredicate(List<VillagerType> villagerTypes) {
        JsonArray terms = new JsonArray();
        for (VillagerType villagerType : villagerTypes) {
            JsonObject typeSpecific = new JsonObject();
            typeSpecific.addProperty("type", "minecraft:villager");
            typeSpecific.addProperty("variant", BuiltInRegistries.VILLAGER_TYPE.getKey(villagerType).toString());
            JsonObject predicate = new JsonObject();
            predicate.add("type_specific", typeSpecific);
            JsonObject condition = new JsonObject();
            condition.addProperty("condition", "minecraft:entity_properties");
            condition.addProperty("entity", "this");
            condition.add("predicate", predicate);
            terms.add(condition);
        }
        if (terms.size() == 1)
            return terms.get(0).getAsJsonObject();
        JsonObject anyOf = new JsonObject();
        anyOf.addProperty("condition", "minecraft:any_of");
        anyOf.add("terms", terms);
        return anyOf;
    }

    private static JsonObject packMcmeta() {
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", 48);
        pack.addProperty("description", "Villager trades dumped by Insane's Survival Overhaul");
        JsonObject json = new JsonObject();
        json.add("pack", pack);
        return json;
    }

    private ResourceLocation uniqueId(ResourceLocation id) {
        ResourceLocation unique = id;
        for (int i = 2; this.usedIds.contains(unique); i++)
            unique = id.withSuffix("_" + i);
        this.usedIds.add(unique);
        return unique;
    }

    private void writeJson(Path path, JsonElement json) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(json));
    }

    private static String namespaceOf(String modId) {
        return ResourceLocation.isValidNamespace(modId) ? modId : "unknown";
    }

    /**
     * Mod id of the mod whose jar contains the listing's class, from the class' module. "unknown" if it can't be derived.
     */
    private static String modIdOf(VillagerTrades.ItemListing listing) {
        String module = listing.getClass().getModule().getName();
        if (module != null) {
            for (IModFileInfo modFile : ModList.get().getModFiles()) {
                if (module.equals(modFile.moduleName()) && !modFile.getMods().isEmpty())
                    return modFile.getMods().getFirst().getModId();
            }
        }
        return "unknown";
    }

    private record Sample(MerchantOffer offer, @Nullable VillagerType villagerType) {
        String shapeKey() {
            return BuiltInRegistries.ITEM.getKey(this.offer.getItemCostA().item().value())
                    + "|" + this.offer.getItemCostB().map(cost -> BuiltInRegistries.ITEM.getKey(cost.item().value()).toString()).orElse("")
                    + "|" + BuiltInRegistries.ITEM.getKey(this.offer.getResult().getItem());
        }
    }
}
