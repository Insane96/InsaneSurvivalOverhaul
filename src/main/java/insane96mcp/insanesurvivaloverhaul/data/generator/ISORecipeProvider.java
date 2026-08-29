package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanelib.data.FeatureEnabledCondition;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.items.dagger.DaggerEquipment;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.Pouch;
import insane96mcp.insanesurvivaloverhaul.module.misc.glowblock.GlowBlockFeature;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import insane96mcp.insanesurvivaloverhaul.module.world.CyanFlower;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
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

        new ShapedRecipeBuilder(RecipeCategory.BUILDING_BLOCKS, GlowBlockFeature.GLOW_BLOCK.item().get(), 1)
                .pattern("ASA")
                .pattern("SCS")
                .pattern("ASA")
                .define('A', Items.AMETHYST_SHARD)
                .define('S', Items.GLOW_BERRIES)
                .define('C', Items.COPPER_BLOCK)
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .unlockedBy("has_glow_berries", has(Items.GLOW_BERRIES))
                .unlockedBy("has_copper_block", has(Items.COPPER_BLOCK))
                .save(recipeOutput.withConditions(new FeatureEnabledCondition("Glow through walls")));

        RecipeOutput daggerOutput = recipeOutput.withConditions(new FeatureEnabledCondition("Daggers"));
        new ShapedRecipeBuilder(RecipeCategory.COMBAT, DaggerEquipment.WOODEN_DAGGER.get(), 1)
                .pattern("I ")
                .pattern(" S")
                .define('I', ItemTags.PLANKS)
                .define('S', Items.STICK)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(daggerOutput);
        new ShapedRecipeBuilder(RecipeCategory.COMBAT, DaggerEquipment.STONE_DAGGER.get(), 1)
                .pattern("I ")
                .pattern(" S")
                .define('I', ItemTags.STONE_TOOL_MATERIALS)
                .define('S', Items.STICK)
                .unlockedBy("has_stone_tool_materials", has(ItemTags.STONE_TOOL_MATERIALS))
                .save(daggerOutput);
        new ShapedRecipeBuilder(RecipeCategory.COMBAT, DaggerEquipment.IRON_DAGGER.get(), 1)
                .pattern("I ")
                .pattern(" S")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(daggerOutput);
        new ShapedRecipeBuilder(RecipeCategory.COMBAT, DaggerEquipment.GOLDEN_DAGGER.get(), 1)
                .pattern("I ")
                .pattern(" S")
                .define('I', Items.GOLD_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(daggerOutput);
        new ShapedRecipeBuilder(RecipeCategory.COMBAT, DaggerEquipment.DIAMOND_DAGGER.get(), 1)
                .pattern("I ")
                .pattern(" S")
                .define('I', Items.DIAMOND)
                .define('S', Items.STICK)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(daggerOutput);
        new ShapedRecipeBuilder(RecipeCategory.COMBAT, DaggerEquipment.COPPER_DAGGER.get(), 1)
                .pattern("I ")
                .pattern(" S")
                .define('I', Items.COPPER_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_copper_ingot", has(Items.COPPER_INGOT))
                .save(daggerOutput);
        netheriteSmithing(daggerOutput, DaggerEquipment.DIAMOND_DAGGER.get(), RecipeCategory.COMBAT, DaggerEquipment.NETHERITE_DAGGER.get());
    }
}
