package insane96mcp.insanesurvivaloverhaul.module.items.repairkit;

import com.mojang.datafixers.util.Either;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.data.ObjTag;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.List;

@LoadFeature(module = ISOModules.ITEMS, description = "Adds repair kits: crafted from a material, they let you repair items using that material in the crafting grid.")
public class RepairKits extends Feature {

    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<RepairKitRepairRecipe>> REPAIR_RECIPE_SERIALIZER =
            ISORegistries.RECIPE_SERIALIZERS.register("crafting_special_repair_kit", () -> new SimpleCraftingRecipeSerializer<>(RepairKitRepairRecipe::new));
    public static final DeferredHolder<Item, RepairKitItem> ITEM = ISORegistries.ITEMS.register("repair_kit", () -> new RepairKitItem(new Item.Properties().stacksTo(16)));

    /**
     * A material the default crafting recipes (see ISORecipeProvider) produce a repair kit for: the crafting
     * ingredient tag (any item in it can craft the kit, e.g. any plank type for the "planks" kit), the tag the
     * resulting kit is valid for repairing with (e.g. any plank type repairs a wooden tool), the texture tint,
     * and optional per-material overrides of {@link #repairKitMaterialRatio}/{@link #maxRepair} (carried over
     * from the 1.20.1 repair kits, whose recipe generator allowed the same overrides per material). Uses
     * NeoForge's common convention tags so items from other mods (e.g. another mod's iron ingot) work too.
     */
    public record RepairKitMaterial(String name, Either<Item, TagKey<Item>> ingredient, ObjTag<Item> material, int color,
                                     @Nullable Double materialRatio, @Nullable Double maxRepair) {
        public static RepairKitMaterial of(String name, TagKey<Item> ingredient, TagKey<Item> material, int color) {
            return new RepairKitMaterial(name, Either.right(ingredient), ObjTag.tagOf(material, BuiltInRegistries.ITEM), color, null, null);
        }

        public static RepairKitMaterial of(String name, Item ingredient, int color) {
            return new RepairKitMaterial(name, Either.left(ingredient), ObjTag.objOf(ingredient, BuiltInRegistries.ITEM), color, null, null);
        }
    }

    public static final List<RepairKitMaterial> DEFAULT_MATERIALS = List.of(
            RepairKitMaterial.of("planks", ItemTags.PLANKS, ItemTags.PLANKS, 12096607),
            RepairKitMaterial.of("iron_ingot", Tags.Items.INGOTS_IRON, Tags.Items.INGOTS_IRON, 14211288),
            RepairKitMaterial.of("gold_ingot", Tags.Items.INGOTS_GOLD, Tags.Items.INGOTS_GOLD, 16643423),
            RepairKitMaterial.of("diamond", Tags.Items.GEMS_DIAMOND, Tags.Items.GEMS_DIAMOND, 10615784),
            RepairKitMaterial.of("netherite_ingot", Tags.Items.INGOTS_NETHERITE, Tags.Items.INGOTS_NETHERITE, 4997443),
            RepairKitMaterial.of("copper_ingot", Tags.Items.INGOTS_COPPER, Tags.Items.INGOTS_COPPER, 13723717),
            RepairKitMaterial.of("chain", Items.CHAIN, 10395294)
    );

    @Config(min = 0, description = "Default repair bonus multiplier applied to the durability a repair kit restores per material used. Each repair kit repairs based on the material it was crafted with, up to Max repair. Can be overridden by item component.")
    public static Double repairKitMaterialRatio = 1.2d;
    @Config(min = 0, max = 1, name = "Max repair", description = "Maximum repair percentage of an item that repair kits can reach. Can be overridden by item component.")
    public static Double maxRepair = 1d;

    public static ItemStack of(ObjTag<Item> material, int color) {
        return of(material, color, null, null);
    }

    /**
     * @param materialRatio override of {@link #repairKitMaterialRatio} for this specific kit, or null to use the (live-configurable) default
     * @param maxRepairOverride override of {@link #maxRepair} for this specific kit, or null to use the (live-configurable) default
     */
    public static ItemStack of(ObjTag<Item> material, int color, @Nullable Double materialRatio, @Nullable Double maxRepairOverride) {
        ItemStack stack = new ItemStack(ITEM.get());
        stack.set(ISORegistries.REPAIR_KIT_MATERIAL.get(), material.toSerializedString());
        stack.set(ISORegistries.REPAIR_KIT_COLOR.get(), color);
        if (materialRatio != null)
            stack.set(ISORegistries.REPAIR_KIT_AMOUNT.get(), materialRatio);
        if (maxRepairOverride != null)
            stack.set(ISORegistries.REPAIR_KIT_MAX_REPAIR.get(), maxRepairOverride);
        return stack;
    }
}
