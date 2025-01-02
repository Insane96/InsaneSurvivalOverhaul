package insane96mcp.iguanatweaksreborn.data.generator.client;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ITRBlockModelsProvider extends BlockModelProvider {
    public ITRBlockModelsProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, modId, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        cross("cyan_flower", new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "block/cyan_flower")).renderType("cutout");
        flowerPotCross("potted_cyan_flower", new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "block/cyan_flower")).renderType("cutout");
        cross("solanum_neorossii", new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "block/solanum_neorossii")).renderType("cutout");
        flowerPotCross("potted_solanum_neorossii", new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "block/solanum_neorossii")).renderType("cutout");

        cubeAll("soul_sand_hellish_coal_ore", "block/soul_sand_hellish_coal_ore");
        cubeAll("soul_soil_hellish_coal_ore", "block/soul_soil_hellish_coal_ore");
    }

    public BlockModelBuilder flowerPotCross(String name, ResourceLocation plant) {
        return singleTexture(name, ResourceLocation.tryParse(BLOCK_FOLDER + "/flower_pot_cross"), "plant", plant);
    }

    public BlockModelBuilder cubeAll(String name, String texture) {
        return super.cubeAll(InsaneSurvivalOverhaul.RESOURCE_PREFIX + name, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, texture));
    }
}
