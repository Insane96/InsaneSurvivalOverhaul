package insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Loads villager trades, their tags and trade sets from data packs:
 * <ul>
 *     <li>trades: {@code data/<namespace>/insanesurvivaloverhaul/villager_trade/<path>.json}</li>
 *     <li>trade tags: {@code data/<namespace>/tags/insanesurvivaloverhaul/villager_trade/<path>.json}, merged across data packs like every tag</li>
 *     <li>trade sets: {@code data/<namespace>/insanesurvivaloverhaul/trade_set/<path>.json}</li>
 * </ul>
 * Trades that fail to parse (e.g. items from a mod that is not installed) or whose {@code neoforge:conditions} are not met are skipped,
 * and silently dropped from the tags that reference them.
 */
public class MerchantTradesReloadListener extends SimplePreparableReloadListener<MerchantTradesReloadListener.Prepared> {
    public static final String TRADES_DIRECTORY = InsaneSO.MOD_ID + "/villager_trade";
    public static final String TAGS_DIRECTORY = "tags/" + InsaneSO.MOD_ID + "/villager_trade";
    public static final String TRADE_SETS_DIRECTORY = InsaneSO.MOD_ID + "/trade_set";

    public static final MerchantTradesReloadListener INSTANCE = new MerchantTradesReloadListener();
    private static final Gson GSON = new GsonBuilder().create();

    private static volatile Data data = Data.EMPTY;

    public static Data getData() {
        return data;
    }

    @Override
    protected @NotNull Prepared prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> trades = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, TRADES_DIRECTORY, GSON, trades);
        Map<ResourceLocation, JsonElement> tradeSets = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, TRADE_SETS_DIRECTORY, GSON, tradeSets);
        Map<ResourceLocation, List<TagLoader.EntryWithSource>> tags = new TagLoader<TagValue>(id -> Optional.empty(), TAGS_DIRECTORY).load(resourceManager);
        return new Prepared(trades, tradeSets, tags);
    }

    @Override
    protected void apply(@NotNull Prepared prepared, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        DynamicOps<JsonElement> ops = this.makeConditionalOps();

        Map<ResourceLocation, VillagerTrade> trades = new HashMap<>();
        Set<ResourceLocation> skippedTrades = new HashSet<>();
        prepared.trades().forEach((id, json) -> ConditionalOps.createConditionalCodec(VillagerTrade.CODEC).parse(ops, json)
                .ifSuccess(optional -> optional.ifPresentOrElse(trade -> trades.put(id, trade), () -> skippedTrades.add(id)))
                .ifError(error -> {
                    InsaneSO.LOGGER.warn("Skipped villager trade {}: {}", id, error.message());
                    skippedTrades.add(id);
                }));

        // Skipped trades resolve to an empty value, so tags referencing them still load
        Map<ResourceLocation, Collection<TagValue>> builtTags = new TagLoader<TagValue>(id -> {
            if (trades.containsKey(id))
                return Optional.of(new TagValue(id, trades.get(id)));
            if (skippedTrades.contains(id))
                return Optional.of(new TagValue(id, null));
            return Optional.empty();
        }, TAGS_DIRECTORY).build(prepared.tags());
        Map<ResourceLocation, List<VillagerTrade>> tags = new HashMap<>();
        builtTags.forEach((id, values) -> tags.put(id, values.stream().map(TagValue::trade).filter(Objects::nonNull).toList()));

        Map<ResourceLocation, ResolvedTradeSet> tradeSets = new HashMap<>();
        prepared.tradeSets().forEach((id, json) -> ConditionalOps.createConditionalCodec(TradeSet.CODEC).parse(ops, json)
                .ifSuccess(optional -> optional.ifPresent(tradeSet -> tradeSets.put(id, new ResolvedTradeSet(tradeSet, resolve(id, tradeSet, trades, skippedTrades, tags)))))
                .ifError(error -> InsaneSO.LOGGER.error("Failed to load trade set {}: {}", id, error.message())));

        data = new Data(trades, tags, tradeSets);
        InsaneSO.LOGGER.info("Loaded {} villager trades ({} skipped), {} villager trade tags and {} trade sets", trades.size(), skippedTrades.size(), tags.size(), tradeSets.size());
    }

    private static List<VillagerTrade> resolve(ResourceLocation tradeSetId, TradeSet tradeSet, Map<ResourceLocation, VillagerTrade> trades, Set<ResourceLocation> skippedTrades, Map<ResourceLocation, List<VillagerTrade>> tags) {
        List<VillagerTrade> list = new ArrayList<>();
        for (ExtraCodecs.TagOrElementLocation entry : tradeSet.trades()) {
            if (entry.tag()) {
                List<VillagerTrade> tag = tags.get(entry.id());
                if (tag == null)
                    InsaneSO.LOGGER.warn("Trade set {} references the missing villager trade tag #{}", tradeSetId, entry.id());
                else
                    list.addAll(tag);
            }
            else {
                VillagerTrade trade = trades.get(entry.id());
                if (trade != null)
                    list.add(trade);
                else if (!skippedTrades.contains(entry.id()))
                    InsaneSO.LOGGER.warn("Trade set {} references the missing villager trade {}", tradeSetId, entry.id());
            }
        }
        return List.copyOf(list);
    }

    @Override
    public @NotNull String getName() {
        return "Villager Trades Listener";
    }

    public record Prepared(Map<ResourceLocation, JsonElement> trades, Map<ResourceLocation, JsonElement> tradeSets, Map<ResourceLocation, List<TagLoader.EntryWithSource>> tags) {}

    /**
     * Value of the trade tags; {@code trade} is null for trades that were skipped.
     */
    public record TagValue(ResourceLocation id, @Nullable VillagerTrade trade) {}

    public record ResolvedTradeSet(TradeSet tradeSet, List<VillagerTrade> trades) {}

    public record Data(Map<ResourceLocation, VillagerTrade> trades, Map<ResourceLocation, List<VillagerTrade>> tags, Map<ResourceLocation, ResolvedTradeSet> tradeSets) {
        public static final Data EMPTY = new Data(Map.of(), Map.of(), Map.of());

        @Nullable
        public ResolvedTradeSet getTradeSet(ResourceLocation id) {
            return this.tradeSets.get(id);
        }
    }
}
