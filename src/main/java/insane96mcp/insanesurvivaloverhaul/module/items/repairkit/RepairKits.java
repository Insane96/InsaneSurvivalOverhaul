package insane96mcp.insanesurvivaloverhaul.module.items.repairkit;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;
import java.util.Map;

@LoadFeature(module = ISOModules.ITEMS, description = "Adds repair kits: crafted from a material, they let you repair items using that material in the crafting grid, up to a partial durability cap.")
public class RepairKits extends Feature {

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RepairKitRepairRecipe>> REPAIR_RECIPE_SERIALIZER =
            ISORegistries.RECIPE_SERIALIZERS.register("crafting_special_repair_kit", () -> new SimpleCraftingRecipeSerializer<>(RepairKitRepairRecipe::new));
    public static final DeferredHolder<Item, RepairKitItem> ITEM = ISORegistries.ITEMS.register("repair_kit", () -> new RepairKitItem(new Item.Properties().stacksTo(16)));

    /**
     * Materials the default crafting recipes (see ISORecipeProvider) produce repair kits for, and the tint each
     * one's texture gets (carried over from the 1.20.1 repair kit recipes). Reused by the creative tab so both
     * stay in sync without duplicating the material list.
     */
    public static final List<Item> DEFAULT_MATERIALS = List.of(
            Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND, Items.NETHERITE_INGOT, Items.COPPER_INGOT, Items.OAK_PLANKS
    );
    public static final Map<Item, Integer> DEFAULT_COLORS = Map.of(
            Items.IRON_INGOT, 14211288,
            Items.GOLD_INGOT, 16643423,
            Items.DIAMOND, 10615784,
            Items.NETHERITE_INGOT, 4997443,
            Items.COPPER_INGOT, 13723717,
            Items.OAK_PLANKS, 12096607
    );

    @Config(min = 1, description = "Default how many materials worth does a repair kit repair. Each repair kit repairs based on the material it was crafted with, up to Max repair.")
    public static Integer repairKitMaterialRatio = 1;
    @Config(min = 0, max = 1, name = "Max repair", description = "Maximum repair percentage of an item that repair kits can reach.")
    public static Double maxRepair = 0.8d;

    public static ItemStack of(Item material, int color) {
        ItemStack stack = new ItemStack(ITEM.get());
        stack.set(ISORegistries.REPAIR_KIT_MATERIAL.get(), BuiltInRegistries.ITEM.getKey(material));
        stack.set(ISORegistries.REPAIR_KIT_COLOR.get(), color);
        return stack;
    }
}
