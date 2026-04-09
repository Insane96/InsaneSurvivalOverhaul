package insane96mcp.insanesurvivaloverhaul.data.generator.client;

import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ISOBlockStatesProvider extends BlockStateProvider {
    public ISOBlockStatesProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, modId, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        //simpleBlock(CyanFlower.FLOWER.block().get());
        simpleBlock(Crops.SOLANUM_NEOROSSII.block().get(),
                models().withExistingParent("solanum_neorossii", mcLoc("block/cross"))
                        .renderType("cutout")
                        .texture("cross", modLoc("block/solanum_neorossii")));
        //logBlock((RotatedPillarBlock) CoalFire.BURNT_LOG.block().get());
    }
}
