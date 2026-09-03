package insane96mcp.insanesurvivaloverhaul.data.generator;

import insane96mcp.insanelib.data.FeatureEnabledCondition;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingFeature;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingRecipe;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.items.dagger.DaggerEquipment;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.Pouch;
import insane96mcp.insanesurvivaloverhaul.module.items.repairkit.RepairKitRepairRecipe;
import insane96mcp.insanesurvivaloverhaul.module.items.repairkit.RepairKits;
import insane96mcp.insanesurvivaloverhaul.module.misc.glowblock.GlowBlockFeature;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import insane96mcp.insanesurvivaloverhaul.module.sleep.Cloth;
import insane96mcp.insanesurvivaloverhaul.module.world.CyanFlower;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import javax.annotation.Nullable;
import java.util.Optional;
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
                .save(recipeOutput.withConditions(new FeatureEnabledCondition("Glow block")));

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

        RecipeOutput repairKitOutput = recipeOutput.withConditions(new FeatureEnabledCondition("Repair Kits"));
        SpecialRecipeBuilder.special(RepairKitRepairRecipe::new)
                .save(repairKitOutput, InsaneSO.id("repair_kit_repairing"));

        for (Item material : RepairKits.DEFAULT_MATERIALS) {
            int color = RepairKits.DEFAULT_COLORS.getOrDefault(material, 0xFFFFFF);
            if (material == Items.OAK_PLANKS)
                repairKit(repairKitOutput, "oak_planks", ItemTags.PLANKS, Items.OAK_PLANKS, color);
            else
                repairKit(repairKitOutput, BuiltInRegistries.ITEM.getKey(material).getPath(), material, material, color);
        }

        RecipeOutput fletchingOutput = recipeOutput.withConditions(new FeatureEnabledCondition("Fletching"));
        fletchingRecipe(fletchingOutput, "quartz_arrow", Items.ARROW, 2, Items.QUARTZ,
                new ItemStack(FletchingFeature.QUARTZ_ARROW_ITEM.get(), 2));
        fletchingRecipe(fletchingOutput, "diamond_arrow", Items.ARROW, 12, Items.DIAMOND,
                new ItemStack(FletchingFeature.DIAMOND_ARROW_ITEM.get(), 12));
        fletchingRecipe(fletchingOutput, "explosive_arrow", Items.ARROW, 4, Items.TNT,
                new ItemStack(FletchingFeature.EXPLOSIVE_ARROW_ITEM.get(), 4));
        fletchingRecipe(fletchingOutput, "torch_arrow", Items.ARROW, 1, Items.TORCH,
                new ItemStack(FletchingFeature.TORCH_ARROW_ITEM.get(), 1));
        fletchingRecipe(fletchingOutput, "ice_arrow", Items.ARROW, 4, Items.BLUE_ICE,
                new ItemStack(FletchingFeature.ICE_ARROW_ITEM.get(), 4));

        // Additional (non-overriding) ways to make vanilla arrows and spectral arrows via the fletching table,
        // on top of their normal crafting table recipes.
        fletchingRecipe(fletchingOutput, "arrow_from_feather", Items.STICK, 1, Items.FEATHER, Items.FLINT,
                new ItemStack(Items.ARROW, 6));
        fletchingRecipe(fletchingOutput, "spectral_arrow", Items.ARROW, 1, Items.GLOWSTONE_DUST,
                new ItemStack(Items.SPECTRAL_ARROW, 1));

        fletchingRecipe(fletchingOutput.withConditions(new FeatureEnabledCondition("Cloth")), "arrow_from_cloth", Items.STICK, 1, Cloth.ITEM.get(), Items.FLINT,
                new ItemStack(Items.ARROW, 3));
    }

    /**
     * A fletching recipe upgrading {@code ingredientCount} vanilla arrows into an equal count of one of the
     * mod's arrows, using one of {@code catalyst} as the second ingredient. Quantities are carried over from
     * the 1.20.1 port.
     */
    private static void fletchingRecipe(RecipeOutput recipeOutput, String name, Item ingredient, int ingredientCount, Item catalyst, ItemStack result) {
        fletchingRecipe(recipeOutput, name, ingredient, ingredientCount, catalyst, null, result);
    }

    private static void fletchingRecipe(RecipeOutput recipeOutput, String name, Item ingredient, int ingredientCount, Item catalyst1, @Nullable Item catalyst2, ItemStack result) {
        ResourceLocation id = InsaneSO.id("fletching/" + name);
        FletchingRecipe recipe = new FletchingRecipe(
                SizedIngredient.of(ingredient, ingredientCount),
                SizedIngredient.of(catalyst1, 1),
                Optional.ofNullable(catalyst2).map(item -> SizedIngredient.of(item, 1)),
                result);
        Advancement.Builder advancement = recipeOutput.advancement()
                .addCriterion("has_ingredient", has(ingredient))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        recipeOutput.accept(id, recipe, advancement.build(id.withPrefix("recipes/combat/")));
    }

    /**
     * A repair kit is crafted from an amethyst shard plus its target material; the resulting stack carries
     * the material's id in its {@link ISORegistries#REPAIR_KIT_MATERIAL} component and a tint color in its
     * {@link ISORegistries#REPAIR_KIT_COLOR} component.
     */
    private static void repairKit(RecipeOutput recipeOutput, String materialName, ItemLike ingredient, Item material, int color) {
        new ShapelessRecipeBuilder(RecipeCategory.TOOLS, RepairKits.of(material, color))
                .requires(Items.AMETHYST_SHARD)
                .requires(ingredient)
                .unlockedBy("has_" + materialName, has(ingredient))
                .save(recipeOutput, InsaneSO.id("repair_kit/from_" + materialName));
    }

    private static void repairKit(RecipeOutput recipeOutput, String materialName, TagKey<Item> ingredient, Item material, int color) {
        new ShapelessRecipeBuilder(RecipeCategory.TOOLS, RepairKits.of(material, color))
                .requires(Items.AMETHYST_SHARD)
                .requires(ingredient)
                .unlockedBy("has_" + materialName, has(ingredient))
                .save(recipeOutput, InsaneSO.id("repair_kit/from_" + materialName));
    }
}
