package insane96mcp.iguanatweaksreborn.data.generator.client;

import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.world.CyanFlower;
import insane96mcp.iguanatweaksreborn.module.world.coalfire.CoalFire;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ISOBlockStatesProvider extends BlockStateProvider {
    public ISOBlockStatesProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, modId, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(CyanFlower.FLOWER.block().get());
        simpleBlock(Crops.SOLANUM_NEOROSSII.block().get());
        logBlock((RotatedPillarBlock) CoalFire.BURNT_LOG.block().get());
    }
}
