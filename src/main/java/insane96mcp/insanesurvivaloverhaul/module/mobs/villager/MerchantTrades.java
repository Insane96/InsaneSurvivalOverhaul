package insane96mcp.insanesurvivaloverhaul.module.mobs.villager;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade.MerchantTradesReloadListener;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade.TradeSet;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade.VillagerTrade;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import javax.annotation.Nullable;
import java.util.*;

@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ISOModules.MOBS, canBeDisabled = false, description = "Villager and wandering trader trades from data packs. Trades go in `data/<namespace>/insanesurvivaloverhaul/villager_trade/`, their lists in the `insanesurvivaloverhaul/villager_trade` tags, and the pool of each villager level or wandering trader pool in `data/<namespace>/insanesurvivaloverhaul/trade_set/` (e.g. `minecraft:armorer/level_1`, `minecraft:wandering_trader/common`). It is a COMMON feature because trades are applied when data loads, before the server config. Changes apply on the next /reload. `/insanesurvivaloverhaul villager_trades dump` writes every trade, mods included, to `logs/insanesurvivaloverhaul/`.")
public class MerchantTrades extends Feature {
    public static final ResourceLocation WANDERING_TRADER_BUYING = ResourceLocation.withDefaultNamespace("wandering_trader/buying");
    public static final ResourceLocation WANDERING_TRADER_UNCOMMON = ResourceLocation.withDefaultNamespace("wandering_trader/uncommon");
    public static final ResourceLocation WANDERING_TRADER_COMMON = ResourceLocation.withDefaultNamespace("wandering_trader/common");

    /** Listings that were there before any mod changed them, by identity */
    private static final Set<VillagerTrades.ItemListing> VANILLA_LISTINGS = Collections.newSetFromMap(new IdentityHashMap<>());
    /** Villager listings, mods included, before ISO's changes. Used by the dump command */
    private static final Map<VillagerProfession, Int2ObjectMap<List<VillagerTrades.ItemListing>>> VILLAGER_SNAPSHOT = new LinkedHashMap<>();
    /** Wandering trader listings (common and uncommon), mods included, before ISO's changes. Used by the dump command */
    private static final Map<ResourceLocation, List<VillagerTrades.ItemListing>> WANDERING_TRADER_SNAPSHOT = new LinkedHashMap<>();
    /** The wandering trader `buying` pool, which does not exist in 1.21.1 */
    private static List<VillagerTrades.ItemListing> wanderingTraderBuying = List.of();

    /**
     * NeoForge fires the wanderer event first on every data load, so the vanilla snapshot is cleared here.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWandererTradesSnapshot(WandererTradesEvent event) {
        VANILLA_LISTINGS.clear();
        VANILLA_LISTINGS.addAll(event.getGenericTrades());
        VANILLA_LISTINGS.addAll(event.getRareTrades());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onVillagerTradesSnapshot(VillagerTradesEvent event) {
        event.getTrades().values().forEach(VANILLA_LISTINGS::addAll);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWandererTrades(WandererTradesEvent event) {
        WANDERING_TRADER_SNAPSHOT.clear();
        WANDERING_TRADER_SNAPSHOT.put(WANDERING_TRADER_COMMON, List.copyOf(event.getGenericTrades()));
        WANDERING_TRADER_SNAPSHOT.put(WANDERING_TRADER_UNCOMMON, List.copyOf(event.getRareTrades()));
        wanderingTraderBuying = List.of();
        if (!this.isEnabled())
            return;

        MerchantTradesReloadListener.Data data = MerchantTradesReloadListener.getData();
        applyTradeSet(data.getTradeSet(WANDERING_TRADER_COMMON), event.getGenericTrades());
        applyTradeSet(data.getTradeSet(WANDERING_TRADER_UNCOMMON), event.getRareTrades());
        MerchantTradesReloadListener.ResolvedTradeSet buying = data.getTradeSet(WANDERING_TRADER_BUYING);
        if (buying != null)
            wanderingTraderBuying = List.copyOf(buying.trades());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onVillagerTrades(VillagerTradesEvent event) {
        Int2ObjectMap<List<VillagerTrades.ItemListing>> snapshot = new Int2ObjectOpenHashMap<>();
        event.getTrades().forEach((level, listings) -> snapshot.put(level.intValue(), List.copyOf(listings)));
        VILLAGER_SNAPSHOT.put(event.getType(), snapshot);
        if (!this.isEnabled())
            return;

        ResourceLocation professionId = BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.getType());
        MerchantTradesReloadListener.Data data = MerchantTradesReloadListener.getData();
        for (int level = 1; level <= 5; level++) {
            int finalLevel = level;
            applyTradeSet(data.getTradeSet(professionId.withPath(path -> path + "/level_" + finalLevel)), event.getTrades().get(level));
        }
    }

    private static void applyTradeSet(@Nullable MerchantTradesReloadListener.ResolvedTradeSet tradeSet, @Nullable List<VillagerTrades.ItemListing> listings) {
        if (tradeSet == null || listings == null)
            return;
        switch (tradeSet.tradeSet().remove()) {
            case VANILLA -> listings.removeIf(VANILLA_LISTINGS::contains);
            case ALL -> listings.clear();
            case NONE -> {}
        }
        listings.addAll(tradeSet.trades());
    }

    public static boolean isVanillaListing(VillagerTrades.ItemListing listing) {
        return VANILLA_LISTINGS.contains(listing);
    }

    public static Map<VillagerProfession, Int2ObjectMap<List<VillagerTrades.ItemListing>>> getVillagerSnapshot() {
        return VILLAGER_SNAPSHOT;
    }

    public static Map<ResourceLocation, List<VillagerTrades.ItemListing>> getWanderingTraderSnapshot() {
        return WANDERING_TRADER_SNAPSHOT;
    }

    /**
     * Fills the wandering trader offers from the `buying`, `uncommon` and `common` pools, in this order as in 26.1.
     * Each pool rolls the trade set's `amount`; without a trade set, or without `amount`, the 1.21.1 counts are used (0 buying, 1 uncommon, 5 common).
     */
    public static void updateWanderingTraderOffers(WanderingTrader trader, MerchantOffers offers) {
        MerchantTradesReloadListener.Data data = MerchantTradesReloadListener.getData();
        addPoolOffers(trader, offers, data.getTradeSet(WANDERING_TRADER_BUYING), wanderingTraderBuying, 0);
        addPoolOffers(trader, offers, data.getTradeSet(WANDERING_TRADER_UNCOMMON), getListings(2), 1);
        addPoolOffers(trader, offers, data.getTradeSet(WANDERING_TRADER_COMMON), getListings(1), 5);
    }

    private static List<VillagerTrades.ItemListing> getListings(int key) {
        VillagerTrades.ItemListing[] listings = VillagerTrades.WANDERING_TRADER_TRADES.get(key);
        return listings == null ? List.of() : Arrays.asList(listings);
    }

    private static void addPoolOffers(WanderingTrader trader, MerchantOffers offers, @Nullable MerchantTradesReloadListener.ResolvedTradeSet resolvedTradeSet, List<VillagerTrades.ItemListing> listings, int defaultAmount) {
        if (listings.isEmpty())
            return;
        RandomSource random = trader.getRandom();
        int amount = defaultAmount;
        boolean allowDuplicates = false;
        if (resolvedTradeSet != null && trader.level() instanceof ServerLevel level) {
            TradeSet tradeSet = resolvedTradeSet.tradeSet();
            if (tradeSet.randomSequence().isPresent())
                random = level.getRandomSequence(tradeSet.randomSequence().get());
            if (tradeSet.amount().isPresent())
                amount = tradeSet.amount().get().getInt(VillagerTrade.createContext(level, trader, random));
            allowDuplicates = tradeSet.allowDuplicates();
        }
        if (amount <= 0)
            return;

        if (allowDuplicates) {
            // Listings can return null offers, so the attempts are capped
            int added = 0;
            for (int attempts = 0; added < amount && attempts < amount * 10; attempts++) {
                MerchantOffer offer = listings.get(random.nextInt(listings.size())).getOffer(trader, random);
                if (offer != null) {
                    offers.add(offer);
                    added++;
                }
            }
        }
        else {
            List<VillagerTrades.ItemListing> available = new ArrayList<>(listings);
            int added = 0;
            while (added < amount && !available.isEmpty()) {
                MerchantOffer offer = available.remove(random.nextInt(available.size())).getOffer(trader, random);
                if (offer != null) {
                    offers.add(offer);
                    added++;
                }
            }
        }
    }
}
