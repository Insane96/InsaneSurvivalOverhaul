package insane96mcp.insanesurvivaloverhaul.data.generator.client;

import insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal.BoneMeal;
import insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal.RichFarmlandBlock;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.ScuteBlock;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.stream.IntStream;

public class ISOBlockStatesProvider extends BlockStateProvider {
    public ISOBlockStatesProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, modId, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        //simpleBlock(CyanFlower.FLOWER.block().get());
        simpleBlock(Crops.POTTED_SOLANUM_NEOROSSII.get(),
                models().withExistingParent("potted_solanum_neorossii", mcLoc("block/flower_pot_cross"))
                        .texture("plant", modLoc("block/solanum_neorossii")));
        simpleBlock(Crops.SOLANUM_NEOROSSII.block().get(),
                models().withExistingParent("solanum_neorossii", mcLoc("block/cross"))
                        .renderType("cutout")
                        .texture("cross", modLoc("block/solanum_neorossii")));
        wildCrop(Crops.WILD_WHEAT);
        wildCrop(Crops.WILD_CARROTS);
        wildCrop(Crops.WILD_POTATOES);
        wildCrop(Crops.WILD_BEETROOTS);

        echoLantern();
        richFarmland();
        turtleScute();

        //logBlock((RotatedPillarBlock) CoalFire.BURNT_LOG.block().get());
        logBlock((RotatedPillarBlock) CoalFire.BURNT_LOG.block().get());
    }

    private void echoLantern() {
        ModelFile standing = models().withExistingParent("echo_lantern", mcLoc("block/template_lantern"))
            .renderType("cutout")
            .texture("lantern", modLoc("block/echo_lantern"));
        ModelFile hanging = models().withExistingParent("echo_lantern_hanging", mcLoc("block/template_hanging_lantern"))
            .renderType("cutout")
            .texture("lantern", modLoc("block/echo_lantern"));
        getVariantBuilder(Spawning.ECHO_LANTERN.block().get())
            .partialState().with(LanternBlock.HANGING, false).modelForState().modelFile(standing).addModel()
            .partialState().with(LanternBlock.HANGING, true).modelForState().modelFile(hanging).addModel();
    }

    private void richFarmland() {
        ModelFile dry = models().withExistingParent("rich_farmland", mcLoc("block/template_farmland"))
            .texture("dirt", mcLoc("block/dirt"))
            .texture("top", modLoc("block/rich_farmland"));
        ModelFile moist = models().withExistingParent("rich_farmland_moist", mcLoc("block/template_farmland"))
            .texture("dirt", mcLoc("block/dirt"))
            .texture("top", modLoc("block/rich_farmland_moist"));
        VariantBlockStateBuilder builder = getVariantBuilder(BoneMeal.RICH_FARMLAND.block().get());
        for (int m = 0; m <= 7; m++) {
            for (int r = 0; r <= 7; r++) {
                builder.partialState()
                    .with(RichFarmlandBlock.MOISTURE, m)
                    .with(RichFarmlandBlock.RICHNESS, r)
                    .modelForState().modelFile(m == 7 ? moist : dry).addModel();
            }
        }
    }

    private void turtleScute() {
        MultiPartBlockStateBuilder builder = getMultipartBuilder(Tweaks.TURTLE_SCUTE.get());
        for (int layer = 1; layer <= 16; layer++) {
            Integer[] heights = IntStream.rangeClosed(layer - 1, 15).boxed().toArray(Integer[]::new);
            ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/turtle_scute/stack_" + layer));
            for (int y : new int[]{0, 90, 180, 270}) {
                builder.part().modelFile(model).rotationY(y).addModel()
                    .condition(ScuteBlock.HEIGHT, heights);
            }
        }
    }

    private void wildCrop(DeferredHolder<Block, ?> block) {
        String name = block.getId().getPath();
        simpleBlock(block.get(),
            models().withExistingParent(name, mcLoc("block/crop"))
                .renderType("cutout")
                .texture("crop", modLoc("block/" + name)));
    }
}
