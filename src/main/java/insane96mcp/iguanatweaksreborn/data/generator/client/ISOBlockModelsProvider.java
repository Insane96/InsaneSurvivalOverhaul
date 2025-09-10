package insane96mcp.iguanatweaksreborn.data.generator.client;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ISOBlockModelsProvider extends BlockModelProvider {
    public ISOBlockModelsProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, modId, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        cross("cyan_flower", InsaneSO.location("block/cyan_flower")).renderType("cutout");
        flowerPotCross("potted_cyan_flower", InsaneSO.location("block/cyan_flower")).renderType("cutout");
        cross("solanum_neorossii", InsaneSO.location("block/solanum_neorossii")).renderType("cutout");
        flowerPotCross("potted_solanum_neorossii", InsaneSO.location("block/solanum_neorossii")).renderType("cutout");
        cube("fletching_table", ResourceLocation.parse("block/birch_planks"), ResourceLocation.parse("block/fletching_table_top"), ResourceLocation.parse("block/fletching_table_front"), ResourceLocation.parse("block/fletching_table_front"), ResourceLocation.parse("block/fletching_table_side"), ResourceLocation.parse("block/fletching_table_side"));
    }

    public BlockModelBuilder flowerPotCross(String name, ResourceLocation plant) {
        return singleTexture(name, ResourceLocation.tryParse(BLOCK_FOLDER + "/flower_pot_cross"), "plant", plant);
    }

    public BlockModelBuilder cubeAll(String name, String texture) {
        return super.cubeAll(InsaneSO.MOD_ID + ":" + name, InsaneSO.location(texture));
    }
}
