package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanelib.data.FeatureEnabledCondition;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ISORecipeProvider extends RecipeProvider {
    public ISORecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        new ShapedRecipeBuilder(RecipeCategory.COMBAT, Bows.SHORTBOW.get(), 1)
                .pattern("WS")
                .pattern("WS")
                .define('S', Items.STRING)
                .define('W', Items.STICK)
                .unlockedBy("has_string", has(Items.STRING))
                .save(recipeOutput.withConditions(new FeatureEnabledCondition("Bows")));
        new ShapedRecipeBuilder(RecipeCategory.BUILDING_BLOCKS, Spawning.ECHO_LANTERN.item().get(), 1)
                .pattern(" S ")
                .pattern("SNS")
                .pattern(" S ")
                .define('S', Items.ECHO_SHARD)
                .define('N', Items.NETHER_STAR)
                .unlockedBy("has_echo_shard", has(Items.ECHO_SHARD))
                .save(recipeOutput.withConditions(new FeatureEnabledCondition("Spawning")));


    }
}
