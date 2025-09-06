package insane96mcp.iguanatweaksreborn.module.mobs.villager.wanderingtrader;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.mobs.villager.SerializableTrade;
import insane96mcp.iguanatweaksreborn.module.mobs.villager.SerializableTrade.Count;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
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

@LoadFeature(module = Modules.Ids.MOBS, description = "Change wandering trader offers. Trades can be customized via json in this feature's folder")
public class WanderingTrades extends JsonFeature {
    public static final TagKey<Structure> DESERT_TEMPLE_TAG = TagKey.create(Registries.STRUCTURE, InsaneSO.location("desert_pyramid"));
    public static final TagKey<Structure> TRAIL_RUINS_TAG = TagKey.create(Registries.STRUCTURE, InsaneSO.location("trail_ruins"));
    public static final TagKey<Structure> JUNGLE_PYRAMID_TAG = TagKey.create(Registries.STRUCTURE, InsaneSO.location("jungle_pyramid"));
    public static final TagKey<Structure> IGLOO_TAG = TagKey.create(Registries.STRUCTURE, InsaneSO.location("igloo"));
    public static final TagKey<Structure> SWAMP_HUT_TAG = TagKey.create(Registries.STRUCTURE, InsaneSO.location("swamp_hut"));

    public static final TagKey<Item> CORAL_BLOCKS = ISOItemTagsProvider.create("coral_blocks");

    public static final Supplier<ArrayList<SerializableTrade>> WANDERING_TRADER_GENERIC_TRADES_DEFAULT = () -> new ArrayList<>(List.of(
            SerializableTrade.emeraldToItems(Count.ONE, Items.BROWN_MUSHROOM, Count.ONE).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.ONE, Items.RED_MUSHROOM, Count.ONE).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.ONE, Items.VINE, Count.of(3)).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.ONE, Items.LILY_PAD, Count.of(5)).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.ONE, Items.SUGAR_CANE, Count.ONE).setMaxUses(8),
            SerializableTrade.emeraldToItems(Count.ONE, Items.PUMPKIN, Count.ONE).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.ONE, Items.SMALL_DRIPLEAF, Count.of(2)).setMaxUses(5),
            SerializableTrade.emeraldToItems(Count.ONE, Items.MOSS_BLOCK, Count.ONE).setMaxUses(5),
            SerializableTrade.emeraldToItems(Count.ONE, Items.POINTED_DRIPSTONE, Count.of(2)).setMaxUses(5),

            SerializableTrade.emeraldToItems(Count.of(2), Items.WHEAT_SEEDS, Count.ONE).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.of(2), Crops.CARROT_SEEDS.get(), Count.ONE).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.of(2), Crops.ROOTED_POTATO.get(), Count.ONE).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.of(2), Items.BEETROOT_SEEDS, Count.ONE).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.of(2), Items.SEA_PICKLE, Count.ONE).setMaxUses(3),
            SerializableTrade.emeraldToItems(Count.of(2), Items.GLOWSTONE, Count.ONE).setMaxUses(5),

            SerializableTrade.emeraldToItems(Count.of(3), Items.MELON, Count.ONE).setMaxUses(2),
            SerializableTrade.emeraldToItems(Count.of(3), Items.PUMPKIN, Count.ONE).setMaxUses(2),
            SerializableTrade.emeraldToItems(Count.of(3), Items.CACTUS, Count.ONE).setMaxUses(3),
            SerializableTrade.emeraldToItems(Count.of(3), Items.KELP, Count.ONE).setMaxUses(3),
            SerializableTrade.emeraldToItems(Count.of(3), Items.TROPICAL_FISH_BUCKET, Count.ONE).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.of(3), Items.PUFFERFISH_BUCKET, Count.ONE).setMaxUses(4),

            SerializableTrade.emeraldToItems(Count.of(4), Items.SLIME_BALL, Count.ONE).setMaxUses(5),

            SerializableTrade.emeraldToItems(Count.of(5), ItemTags.SAPLINGS, Count.ONE).setMaxUses(8),
            SerializableTrade.emeraldToItems(Count.of(5), Items.NAUTILUS_SHELL, Count.ONE).setMaxUses(5)
    ));

    public static final ArrayList<SerializableTrade> wanderingTraderGenericTrades = new ArrayList<>();

    public static final Supplier<ArrayList<SerializableTrade>> WANDERING_TRADER_RARE_TRADES_DEFAULT = () -> new ArrayList<>(List.of(
            SerializableTrade.emeraldToItems(Count.ONE, Items.GUNPOWDER, Count.of(4)).setMaxUses(2),
            SerializableTrade.emeraldToItems(Count.ONE, ItemTags.LOGS_THAT_BURN, Count.of(8)).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.ONE, Items.PACKED_ICE, Count.ONE).setMaxUses(6),

            SerializableTrade.emeraldToItems(Count.of(3), Items.BOOK, Count.ONE)
                    .enchantResult(4, 8, true),

            new SerializableTrade()
                    .setItemA(Items.EMERALD, Count.of(5), null)
                    .setResult(new SerializableTrade.Stack(
                            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LONG_INVISIBILITY).getItem(), null, Count.ONE,
                            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LONG_INVISIBILITY).getTag()
                    )),

            SerializableTrade.emeraldToItems(Count.of(6), Items.EXPERIENCE_BOTTLE, Count.ONE).setMaxUses(4),
            SerializableTrade.emeraldToItems(Count.of(6), Items.BLUE_ICE, Count.ONE).setMaxUses(6),

            SerializableTrade.emeraldToItems(Count.of(9), Items.BOOK, Count.ONE)
                    .enchantResult(12, 24, true),

            new SerializableTrade()
                    .setItemA(Items.EMERALD, Count.of(8), null)
                    .setResult(Items.MAP, Count.ONE, getHoverNameNBT(Items.MAP, 1, Component.translatable("filled_map.desert_pyramid")))
                    .explorationMap(DESERT_TEMPLE_TAG, MapDecoration.Type.MANSION, ExplorationMapFunction.DEFAULT_ZOOM, 50, false),

            new SerializableTrade()
                    .setItemA(Items.EMERALD, Count.of(8), null)
                    .setResult(Items.MAP, Count.ONE, getHoverNameNBT(Items.MAP, 1, Component.translatable("filled_map.jungle_pyramid")))
                    .explorationMap(JUNGLE_PYRAMID_TAG, MapDecoration.Type.MANSION, ExplorationMapFunction.DEFAULT_ZOOM, 50, false),

            new SerializableTrade()
                    .setItemA(Items.EMERALD, Count.of(8), null)
                    .setResult(Items.MAP, Count.ONE, getHoverNameNBT(Items.MAP, 1, Component.translatable("filled_map.igloo")))
                    .explorationMap(IGLOO_TAG, MapDecoration.Type.MANSION, ExplorationMapFunction.DEFAULT_ZOOM, 50, false),

            new SerializableTrade()
                    .setItemA(Items.EMERALD, Count.of(8), null)
                    .setResult(Items.MAP, Count.ONE, getHoverNameNBT(Items.MAP, 1, Component.translatable("filled_map.trail_ruins")))
                    .explorationMap(TRAIL_RUINS_TAG, MapDecoration.Type.TARGET_POINT, ExplorationMapFunction.DEFAULT_ZOOM, 50, false),

            new SerializableTrade()
                    .setItemA(Items.EMERALD, Count.of(8), null)
                    .setResult(Items.MAP, Count.ONE, getHoverNameNBT(Items.MAP, 1, Component.translatable("filled_map.swamp_hut")))
                    .explorationMap(SWAMP_HUT_TAG, MapDecoration.Type.TARGET_POINT, ExplorationMapFunction.DEFAULT_ZOOM, 50, false),

            new SerializableTrade()
                    .setItemA(Items.EMERALD, Count.of(8), null)
                    .setResult(Items.MAP, Count.ONE, getHoverNameNBT(Items.MAP, 1, Component.translatable("filled_map.mansion")))
                    .explorationMap(StructureTags.ON_WOODLAND_EXPLORER_MAPS, MapDecoration.Type.MANSION, ExplorationMapFunction.DEFAULT_ZOOM, 100, false),

            new SerializableTrade()
                    .setItemA(Items.EMERALD, Count.of(8), null)
                    .setResult(Items.MAP, Count.ONE, getHoverNameNBT(Items.MAP, 1, Component.translatable("filled_map.monument")))
                    .explorationMap(StructureTags.ON_OCEAN_EXPLORER_MAPS, MapDecoration.Type.MONUMENT, ExplorationMapFunction.DEFAULT_ZOOM, 50, false)
            ));

    public static final ArrayList<SerializableTrade> wanderingTraderRareTrades = new ArrayList<>();

    public static final Supplier<ArrayList<SerializableTrade>> WANDERING_TRADER_BUYING_TRADES_DEFAULT = () -> new ArrayList<>(List.of(
            SerializableTrade
                    .itemToEmeralds(Items.BAKED_POTATO, Count.of(4), Count.ONE),
            SerializableTrade
                    .itemToEmeralds(Items.FERMENTED_SPIDER_EYE, Count.ONE, Count.of(3)),
            SerializableTrade
                    .itemToEmeralds(Items.HAY_BLOCK, Count.ONE, Count.ONE),
            SerializableTrade
                    .itemToEmeralds(Items.MILK_BUCKET, Count.ONE, Count.of(2)),
            new SerializableTrade()
                    .setItemA(new SerializableTrade.Stack(
                            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER).getItem(), null, Count.ONE,
                            PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER).getTag()
                    ))
                    .setResult(Items.EMERALD, Count.ONE, null),
            SerializableTrade
                    .itemToEmeralds(Items.WATER_BUCKET, Count.ONE, Count.of(2))
    ));

    public static final ArrayList<SerializableTrade> wanderingTraderBuyingTrades = new ArrayList<>();

    @Config(description = "Vanilla is 0 pre-23w31a experimental feature, 2 otherwise")
    public static Integer buyingTrades = 2;

    @Config(description = "Vanilla is 5")
    public static Integer ordinaryTrades = 6;

    @Config(description = "Vanilla is 1")
    public static Integer rareTrades = 2;

    public WanderingTrades(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSO.CONFIG_FOLDER;
    }

    @Override
    public void loadJsonConfigs() {
        if (!this.isEnabled())
            return;
        //Load this here so no need for a Supplier for items
        if (JSON_CONFIGS.isEmpty()) {
            JSON_CONFIGS.add(new JsonConfig<>("generic_trades.json", wanderingTraderGenericTrades, WANDERING_TRADER_GENERIC_TRADES_DEFAULT.get(), SerializableTrade.LIST_TYPE));
            JSON_CONFIGS.add(new JsonConfig<>("rare_trades.json", wanderingTraderRareTrades, WANDERING_TRADER_RARE_TRADES_DEFAULT.get(), SerializableTrade.LIST_TYPE));
            JSON_CONFIGS.add(new JsonConfig<>("buying_trades.json", wanderingTraderBuyingTrades, WANDERING_TRADER_BUYING_TRADES_DEFAULT.get(), SerializableTrade.LIST_TYPE));
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

    public static CompoundTag getHoverNameNBT(Item item, int count, Component name) {
        ItemStack stack = new ItemStack(item, count);
        stack.setHoverName(name);
        return stack.getTag();
    }
}
