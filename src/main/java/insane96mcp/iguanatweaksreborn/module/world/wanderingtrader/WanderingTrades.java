package insane96mcp.iguanatweaksreborn.module.world.wanderingtrader;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalTweaks;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Label(name = "Wandering Trades", description = "Change wandering trader offers. Trades can be customized via json in this feature's folder")
@LoadFeature(module = Modules.Ids.WORLD)
public class WanderingTrades extends JsonFeature {
    public static final TagKey<Structure> DESERT_TEMPLE_TAG = TagKey.create(Registries.STRUCTURE, new ResourceLocation(InsaneSurvivalTweaks.RESOURCE_PREFIX + "desert_pyramid"));
    public static final TagKey<Structure> TRAIL_RUINS_TAG = TagKey.create(Registries.STRUCTURE, new ResourceLocation(InsaneSurvivalTweaks.RESOURCE_PREFIX + "trail_ruins"));
    public static final TagKey<Structure> JUNGLE_PYRAMID_TAG = TagKey.create(Registries.STRUCTURE, new ResourceLocation(InsaneSurvivalTweaks.RESOURCE_PREFIX + "jungle_pyramid"));
    public static final TagKey<Structure> IGLOO_TAG = TagKey.create(Registries.STRUCTURE, new ResourceLocation(InsaneSurvivalTweaks.RESOURCE_PREFIX + "igloo"));

    public static final Supplier<ArrayList<SerializableTrade>> WANDERING_TRADER_GENERIC_TRADES_DEFAULT = () -> new ArrayList<>(List.of(
            new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.WHEAT_SEEDS), 4),
            new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Crops.CARROT_SEEDS.get()), 4),
            new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Crops.ROOTED_POTATO.get()), 4),
            new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BEETROOT_SEEDS), 4),
            new SerializableTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.BROWN_MUSHROOM), 4),
            new SerializableTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.RED_MUSHROOM), 4),
            new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.MELON), 2),
            new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.PUMPKIN), 2),
            new SerializableTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.VINE), 4),
            new SerializableTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.LILY_PAD, 2), 4),
            new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.POINTED_DRIPSTONE, 2), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.SEA_PICKLE), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.CACTUS), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.KELP), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.ACACIA_SAPLING), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.BIRCH_SAPLING), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.CHERRY_SAPLING), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.SPRUCE_SAPLING), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.OAK_SAPLING), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.DARK_OAK_SAPLING), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.JUNGLE_SAPLING), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.NAUTILUS_SHELL), 5),
            new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.TROPICAL_FISH_BUCKET), 4),
            new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.PUFFERFISH_BUCKET), 4)
    ));

    public static final ArrayList<SerializableTrade> wanderingTraderGenericTrades = new ArrayList<>();

    public static final Supplier<ArrayList<SerializableTrade>> WANDERING_TRADER_RARE_TRADES_DEFAULT = () -> new ArrayList<>(List.of(
            new SerializableTrade(new ItemStack(Items.EMERALD, 9), new ItemStack(Items.BOOK), 1)
                    .enchantResult(12, 22, true),
            new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.BOOK), 1)
                    .enchantResult(4, 8, false),
            new SerializableTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.GUNPOWDER, 4), 3),
            new SerializableTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.LILY_PAD, 5), 2),
            new SerializableTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.PACKED_ICE, 1), 6),
            new SerializableTrade(new ItemStack(Items.EMERALD, 6), new ItemStack(Items.EXPERIENCE_BOTTLE), 4),
            new SerializableTrade(new ItemStack(Items.EMERALD, 8), createStackWithName(Items.MAP, 1, Component.translatable("filled_map.desert_pyramid")), 1)
                    .explorationMap(DESERT_TEMPLE_TAG, MapDecoration.Type.MANSION, ExplorationMapFunction.DEFAULT_ZOOM, 50, false),
            new SerializableTrade(new ItemStack(Items.EMERALD, 8), createStackWithName(Items.MAP, 1, Component.translatable("filled_map.jungle_pyramid")), 1)
                    .explorationMap(JUNGLE_PYRAMID_TAG, MapDecoration.Type.MANSION, ExplorationMapFunction.DEFAULT_ZOOM, 50, false),
            new SerializableTrade(new ItemStack(Items.EMERALD, 8), createStackWithName(Items.MAP, 1, Component.translatable("filled_map.igloo")), 1)
                    .explorationMap(IGLOO_TAG, MapDecoration.Type.MANSION, ExplorationMapFunction.DEFAULT_ZOOM, 50, false),
            new SerializableTrade(new ItemStack(Items.EMERALD, 8), createStackWithName(Items.MAP, 1, Component.translatable("filled_map.trail_ruins")), 1)
                    .explorationMap(TRAIL_RUINS_TAG, MapDecoration.Type.TARGET_POINT, ExplorationMapFunction.DEFAULT_ZOOM, 50, false),
            new SerializableTrade(new ItemStack(Items.EMERALD, 8), createStackWithName(Items.MAP, 1, Component.translatable("filled_map.mansion")), 1)
                    .explorationMap(StructureTags.ON_WOODLAND_EXPLORER_MAPS, MapDecoration.Type.MANSION, ExplorationMapFunction.DEFAULT_ZOOM, 100, false),
            new SerializableTrade(new ItemStack(Items.EMERALD, 8), createStackWithName(Items.MAP, 1, Component.translatable("filled_map.monument")), 1)
                    .explorationMap(StructureTags.ON_OCEAN_EXPLORER_MAPS, MapDecoration.Type.MONUMENT, ExplorationMapFunction.DEFAULT_ZOOM, 50, false)
    ));

    public static final ArrayList<SerializableTrade> wanderingTraderRareTrades = new ArrayList<>();

    public static final Supplier<ArrayList<SerializableTrade>> WANDERING_TRADER_BUYING_TRADES_DEFAULT = () -> new ArrayList<>(List.of(
            new SerializableTrade(new ItemStack(Items.BAKED_POTATO, 4), new ItemStack(Items.EMERALD), 1),
            new SerializableTrade(new ItemStack(Items.FERMENTED_SPIDER_EYE, 1), new ItemStack(Items.EMERALD, 3), 1),
            new SerializableTrade(new ItemStack(Items.HAY_BLOCK, 1), new ItemStack(Items.EMERALD), 1),
            new SerializableTrade(new ItemStack(Items.MILK_BUCKET, 1), new ItemStack(Items.EMERALD, 2), 1),
            new SerializableTrade(PotionUtils.setPotion(new ItemStack(Items.POTION, 1), Potions.WATER), new ItemStack(Items.EMERALD), 1),
            new SerializableTrade(new ItemStack(Items.WATER_BUCKET, 1), new ItemStack(Items.EMERALD, 2), 1)
    ));

    public static final ArrayList<SerializableTrade> wanderingTraderBuyingTrades = new ArrayList<>();

    @Config
    @Label(name = "Amount of Buying trades", description = "Vanilla is 0 pre-23w31a experimental feature, 2 otherwise")
    public static Integer buyingTrades = 2;

    @Config
    @Label(name = "Amount of Ordinary trades", description = "Vanilla is 5")
    public static Integer ordinaryTrades = 6;

    @Config
    @Label(name = "Amount of Rare trades", description = "Vanilla is 1")
    public static Integer rareTrades = 2;

    public WanderingTrades(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSurvivalTweaks.CONFIG_FOLDER;
    }

    @Override
    public void loadJsonConfigs() {
        if (!this.isEnabled())
            return;
        //Load this here so no need for a Supplier for items
        if (JSON_CONFIGS.isEmpty()) {
            JSON_CONFIGS.add(new JsonConfig<>("generic_trades.json", wanderingTraderGenericTrades, WANDERING_TRADER_GENERIC_TRADES_DEFAULT.get(), SerializableTrade.SERIALIZABLE_TRADE_LIST_TYPE));
            JSON_CONFIGS.add(new JsonConfig<>("rare_trades.json", wanderingTraderRareTrades, WANDERING_TRADER_RARE_TRADES_DEFAULT.get(), SerializableTrade.SERIALIZABLE_TRADE_LIST_TYPE));
            JSON_CONFIGS.add(new JsonConfig<>("buying_trades.json", wanderingTraderBuyingTrades, WANDERING_TRADER_BUYING_TRADES_DEFAULT.get(), SerializableTrade.SERIALIZABLE_TRADE_LIST_TYPE));
        }
        super.loadJsonConfigs();
        NonNullList<VillagerTrades.ItemListing> generic = NonNullList.create();
        NonNullList<VillagerTrades.ItemListing> rare = NonNullList.create();
        MinecraftForge.EVENT_BUS.post(new WandererTradesEvent(generic, rare));
        VillagerTrades.WANDERING_TRADER_TRADES.put(1, generic.toArray(new VillagerTrades.ItemListing[0]));
        VillagerTrades.WANDERING_TRADER_TRADES.put(2, rare.toArray(new VillagerTrades.ItemListing[0]));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWanderingTradesEvent(WandererTradesEvent event) {
        if (!this.isEnabled())
            return;
        event.getGenericTrades().clear();
        for (SerializableTrade serializableTrade : wanderingTraderGenericTrades) {
            event.getGenericTrades().add(serializableTrade);
        }
        event.getRareTrades().clear();
        for (SerializableTrade serializableTrade : wanderingTraderRareTrades) {
            event.getRareTrades().add(serializableTrade);
        }
        VillagerTrades.WANDERING_TRADER_TRADES.put(3, wanderingTraderBuyingTrades.toArray(new VillagerTrades.ItemListing[0]));
    }

    public static ItemStack createStackWithName(Item item, int count, Component name) {
        ItemStack stack = new ItemStack(item, count);
        stack.setHoverName(name);
        return stack;
    }
}
