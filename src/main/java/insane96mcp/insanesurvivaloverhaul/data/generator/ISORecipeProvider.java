package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanelib.data.FeatureEnabledCondition;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.Pouch;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import insane96mcp.insanesurvivaloverhaul.module.world.CyanFlower;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
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
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                .save(recipeOutput.withConditions(new FeatureEnabledCondition("Spawning")));

        new ShapedRecipeBuilder(RecipeCategory.TOOLS, Pouch.ITEM.get(), 1)
                .pattern("LSL")
                .pattern("I I")
                .pattern("LLL")
                .define('S', Items.STRING)
                .define('I', Items.IRON_INGOT)
                .define('L', Items.LEATHER)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .unlockedBy("has_string", has(Items.STRING))
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput.withConditions(new FeatureEnabledCondition("Pouch")));

        new ShapelessRecipeBuilder(RecipeCategory.MISC, Items.FLINT, 1)
                .requires(Items.GRAVEL, 3)
                .unlockedBy("has_gravel", has(Items.GRAVEL))
                .save(recipeOutput.withConditions(new FeatureEnabledCondition("Coal & Fire")));

        new ShapelessRecipeBuilder(RecipeCategory.MISC, Items.CYAN_DYE, 1)
                .requires(CyanFlower.FLOWER.item().get(), 1)
                .unlockedBy("has_flower", has(CyanFlower.FLOWER.item().get()))
                .save(recipeOutput);

        new ShapelessRecipeBuilder(RecipeCategory.MISC, Items.PURPLE_DYE, 1)
                .requires(Crops.SOLANUM_NEOROSSII.item().get(), 1)
                .unlockedBy("has_flower", has(Crops.SOLANUM_NEOROSSII.item().get()))
                .save(recipeOutput);

    }
}
