package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.module.base.items.ItemComponentsReloadListener;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.Map;

@LoadFeature(module = ISOModules.ITEMS, description = "Make food, items and blocks less stackable. Items and Blocks are disabled by default. Changing stuff requires a /reload, but might require a Minecraft restart.")
public class StackSizes extends Feature {
    public static final TagKey<Item> NO_ITEM_STACK_SIZE_CHANGES = ISOItemTagsProvider.create("no_item_stack_size_changes");
    public static final TagKey<Item> NO_BLOCK_STACK_SIZE_CHANGES = ISOItemTagsProvider.create("no_block_stack_size_changes");
    public static final TagKey<Item> NO_FOOD_STACK_SIZE_CHANGES = ISOItemTagsProvider.create("no_food_stack_size_changes");

    @Config(description = "The formula to calculate the stack size of a food item. Variables as nutrition, saturation, eat_seconds as numbers and fast_food as boolean can be used. Set to empty to disable this. This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html. Stack sizes are limited to 99 by the game.")
    public static String foodStackReductionFormula = "ROUND(MAX(64 / MAX(nutrition, 1), 1), 0)";
    @Config(min = 0.01d, max = 1.546875d, name = "Item Stack Multiplier", description = "Items max stack sizes (excluding blocks and foods) will be multiplied by this value. Setting to 1 will disable this. Stack sizes are limited to 99 by the game.")
    public static Double itemStackMultiplier = 1.546875;
    @Config(min = 0.01d, max = 1.546875d, description = "All the blocks max stack sizes will be multiplied by this value. Setting to 1 will disable this. Stack sizes are limited to 99 by the game.")
    public static Double blockStackMultiplier = 1.546875;

    @Config(description = "Enables a data pack that increases item stacks of Horse Armors, Minecarts and Saddles")
    public static Boolean dataPack = true;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("item_stacks", "Insane's Survival Overhaul Item Stacks", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
        ItemComponentsReloadListener.PROGRAMMATIC_PROVIDERS.add((registryAccess, patches) -> {
            if (!this.isEnabled()) return;
            processItemStackSizes(patches);
            processBlockStackSizes(patches);
            processFoodStackSizes(patches);
        });
    }

    private static void processItemStackSizes(Map<Item, DataComponentPatch> patches) {
        if (itemStackMultiplier == 1d)
            return;

        for (Item item : BuiltInRegistries.ITEM) {
            int base = item.components().getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
            if (item instanceof BlockItem
                    || base == 1
                    || item.components().has(DataComponents.FOOD)
                    || item.builtInRegistryHolder().is(NO_ITEM_STACK_SIZE_CHANGES))
                continue;

            int stackSize = Mth.clamp((int) Math.round(base * itemStackMultiplier), 1, 99);
            patches.put(item, DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, stackSize).build());
        }
    }

    private static void processBlockStackSizes(Map<Item, DataComponentPatch> patches) {
        if (blockStackMultiplier == 1d)
            return;

        for (Item item : BuiltInRegistries.ITEM) {
            int base = item.components().getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
            if (!(item instanceof BlockItem)
                    || base == 1
                    || item.builtInRegistryHolder().is(NO_BLOCK_STACK_SIZE_CHANGES))
                continue;

            int stackSize = Mth.clamp((int) Math.round(base * blockStackMultiplier), 1, 99);
            patches.put(item, DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, stackSize).build());
        }
    }

    private static void processFoodStackSizes(Map<Item, DataComponentPatch> patches) {
        if (foodStackReductionFormula.isBlank())
            return;

        for (Item item : BuiltInRegistries.ITEM) {
            FoodProperties food = item.components().get(DataComponents.FOOD);
            if (food == null || item.builtInRegistryHolder().is(NO_FOOD_STACK_SIZE_CHANGES))
                continue;

            int stackSize = Mth.clamp((int) MCUtils.computeFoodFormula(food, foodStackReductionFormula), 1, 99);
            patches.put(item, DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, stackSize).build());
        }
    }
}