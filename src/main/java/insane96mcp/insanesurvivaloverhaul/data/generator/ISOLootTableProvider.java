package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal.BoneMeal;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins.BigOreVeins;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.ScuteBlock;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import insane96mcp.insanesurvivaloverhaul.module.movement.minecarts.Minecarts;
import insane96mcp.insanesurvivaloverhaul.module.world.CyanFlower;
import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ISOLootTableProvider extends LootTableProvider {
    public ISOLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, Set.of(), List.of(
            new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK),
            new SubProviderEntry(GameplayLoot::new, LootContextParamSets.EMPTY)
        ), registries);
    }

    static class BlockLoot extends BlockLootSubProvider {
        BlockLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            // silk touch → itself, otherwise → charcoal
            add(CoalFire.BURNT_LOG.block().get(), createSelfDropDispatchTable(
                CoalFire.BURNT_LOG.block().get(),
                hasSilkTouch(),
                applyExplosionDecay(CoalFire.BURNT_LOG.block().get(), LootItem.lootTableItem(Items.CHARCOAL))
            ));

            // 3 echo shards + 1 pool of echo shard or nether star (equal weight)
            add(Spawning.ECHO_LANTERN.block().get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.ECHO_SHARD)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(3)))))
                .withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.ECHO_SHARD))
                    .add(LootItem.lootTableItem(Items.NETHER_STAR))));

            add(BoneMeal.RICH_FARMLAND.block().get(), createSingleItemTable(Items.DIRT));

            add(Crops.SOLANUM_NEOROSSII.block().get(), createSingleItemTable(Crops.SOLANUM_NEOROSSII.item().get()));
            add(CyanFlower.FLOWER.block().get(), createSingleItemTable(CyanFlower.FLOWER.item().get()));

            add(Minecarts.COPPER_POWERED_RAIL.block().get(), createSingleItemTable(Minecarts.COPPER_POWERED_RAIL.item().get()));
            add(Minecarts.GOLDEN_POWERED_RAIL.block().get(), createSingleItemTable(Minecarts.GOLDEN_POWERED_RAIL.item().get()));

            turtleScute();

            add(BigOreVeins.IRON_ORE_ROCK.block().get(), createSelfDropDispatchTable(
                BigOreVeins.IRON_ORE_ROCK.block().get(), hasSilkTouch(),
                applyExplosionDecay(BigOreVeins.IRON_ORE_ROCK.block().get(), LootItem.lootTableItem(Items.RAW_IRON))));
            add(BigOreVeins.GOLD_ORE_ROCK.block().get(), createSelfDropDispatchTable(
                BigOreVeins.GOLD_ORE_ROCK.block().get(), hasSilkTouch(),
                applyExplosionDecay(BigOreVeins.GOLD_ORE_ROCK.block().get(), LootItem.lootTableItem(Items.RAW_GOLD))));
            add(BigOreVeins.COPPER_ORE_ROCK.block().get(), createSelfDropDispatchTable(
                BigOreVeins.COPPER_ORE_ROCK.block().get(), hasSilkTouch(),
                applyExplosionDecay(BigOreVeins.COPPER_ORE_ROCK.block().get(), LootItem.lootTableItem(Items.RAW_COPPER))));

            add(Crops.WILD_WHEAT.get(), wildCropTable(Items.WHEAT_SEEDS, Items.WHEAT, null, null));
            add(Crops.WILD_BEETROOTS.get(), wildCropTable(Items.BEETROOT_SEEDS, Items.BEETROOT, null, null));
            add(Crops.WILD_CARROTS.get(), wildCropTable(Crops.CARROT_SEEDS.get(), Items.CARROT, Items.AZURE_BLUET, null));
            add(Crops.WILD_POTATOES.get(), wildCropTable(Crops.ROOTED_POTATO.get(), Items.POTATO, Crops.SOLANUM_NEOROSSII.item().get(), UniformGenerator.between(2, 3)));
        }

        private void turtleScute() {
            LootItem.Builder<?> entry = LootItem.lootTableItem(Items.TURTLE_SCUTE);
            for (int h = 0; h <= 15; h++) {
                entry.apply(SetItemCountFunction.setCount(ConstantValue.exactly(h + 1))
                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Tweaks.TURTLE_SCUTE.get())
                        .setProperties(StatePropertiesPredicate.Builder.properties()
                            .hasProperty(ScuteBlock.HEIGHT, h))));
            }
            add(Tweaks.TURTLE_SCUTE.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(entry)));
        }

        private LootTable.Builder wildCropTable(ItemLike seeds, ItemLike crop, @Nullable ItemLike extra, @Nullable UniformGenerator extraCount) {
            LootTable.Builder table = LootTable.lootTable()
                .withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(seeds)))
                .withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(crop)
                        .when(LootItemRandomChanceCondition.randomChance(0.75f))));
            if (extra != null) {
                LootItem.Builder<?> extraEntry = LootItem.lootTableItem(extra);
                if (extraCount != null)
                    extraEntry.apply(SetItemCountFunction.setCount(extraCount));
                table.withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(extraEntry));
            }
            return table;
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(
                CoalFire.BURNT_LOG.block().get(),
                Spawning.ECHO_LANTERN.block().get(),
                BoneMeal.RICH_FARMLAND.block().get(),
                Crops.SOLANUM_NEOROSSII.block().get(),
                CyanFlower.FLOWER.block().get(),
                Tweaks.TURTLE_SCUTE.get(),
                Crops.WILD_WHEAT.get(),
                Crops.WILD_BEETROOTS.get(),
                Crops.WILD_CARROTS.get(),
                Crops.WILD_POTATOES.get(),
                Minecarts.COPPER_POWERED_RAIL.block().get(),
                Minecarts.GOLDEN_POWERED_RAIL.block().get(),
                BigOreVeins.IRON_ORE_ROCK.block().get(),
                BigOreVeins.GOLD_ORE_ROCK.block().get(),
                BigOreVeins.COPPER_ORE_ROCK.block().get()
            );
        }
    }

    static class GameplayLoot implements LootTableSubProvider {
        @SuppressWarnings("unused")
        private final HolderLookup.Provider registries;

        GameplayLoot(HolderLookup.Provider registries) {
            this.registries = registries;
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
            output.accept(
                ResourceKey.create(Registries.LOOT_TABLE, InsaneSO.id("gameplay/pumpkin_shear")),
                LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(FoodDrinks.PUMPKIN_PULP.get())
                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))))
            );
        }
    }
}
