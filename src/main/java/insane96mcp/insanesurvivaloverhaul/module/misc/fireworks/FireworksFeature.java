package insane96mcp.insanesurvivaloverhaul.module.misc.fireworks;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.MISC, description = """
        Enables a data pack that changes fireworks recipes:
        * Firework Rockets recipes output 5 instead of 3
        * Firework Stars recipe outputs 2 instead of 1
        * Firework Stars can be added to already crafted Firework Rockets""")
public class FireworksFeature extends Feature {

    public static final DeferredHolder<RecipeSerializer<?>, CountCraftingRecipeSerializer<CountFireworkRocketRecipe>> FIREWORK_ROCKET_SERIALIZER =
            ISORegistries.RECIPE_SERIALIZERS.register("crafting_special_firework_rocket", () -> new CountCraftingRecipeSerializer<>(CountFireworkRocketRecipe::new, CountFireworkRocketRecipe::getCount, 3));
    public static final DeferredHolder<RecipeSerializer<?>, CountCraftingRecipeSerializer<CountFireworkStarRecipe>> FIREWORK_STAR_SERIALIZER =
            ISORegistries.RECIPE_SERIALIZERS.register("crafting_special_firework_star", () -> new CountCraftingRecipeSerializer<>(CountFireworkStarRecipe::new, CountFireworkStarRecipe::getCount, 1));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<FireworkRocketAddStarRecipe>> FIREWORK_ROCKET_ADD_STAR_SERIALIZER =
            ISORegistries.RECIPE_SERIALIZERS.register("crafting_special_firework_rocket_add_star", () -> new SimpleCraftingRecipeSerializer<>(FireworkRocketAddStarRecipe::new));

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("fireworks", "Insane's Survival Overhaul Fireworks", () -> this.isEnabled() && !Packs.disableAllDataPacks);
    }
}
