package insane96mcp.insanesurvivaloverhaul.data.generator.client;

import insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal.BoneMeal;
import insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal.RichFarmlandBlock;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins.BigOreVeins;
import insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins.GroundRockBlock;
import insane96mcp.insanesurvivaloverhaul.module.mobs.spawning.Spawning;
import insane96mcp.insanesurvivaloverhaul.module.mobs.turtles.ScuteBlock;
import insane96mcp.insanesurvivaloverhaul.module.mobs.turtles.Turtles;
import insane96mcp.insanesurvivaloverhaul.module.respawn.echopillar.EchoPillar;
import insane96mcp.insanesurvivaloverhaul.module.respawn.echopillar.EchoPillarBlock;
import insane96mcp.insanesurvivaloverhaul.module.world.CyanFlower;
import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.stream.IntStream;

public class ISOBlockStatesProvider extends BlockStateProvider {
    public ISOBlockStatesProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, modId, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        flower("cyan_flower", CyanFlower.FLOWER.block().get(), CyanFlower.POTTED_FLOWER.get());
        flower("solanum_neorossii", Crops.SOLANUM_NEOROSSII.block().get(), Crops.POTTED_SOLANUM_NEOROSSII.get());
        wildCrop(Crops.WILD_WHEAT);
        wildCrop(Crops.WILD_CARROTS);
        wildCrop(Crops.WILD_POTATOES);
        wildCrop(Crops.WILD_BEETROOTS);

        echoLantern();
        richFarmland();
        turtleScute();

        logBlock((RotatedPillarBlock) CoalFire.BURNT_LOG.block().get());

        oreRock(BigOreVeins.IRON_ORE_ROCK, "iron_ore");
        oreRock(BigOreVeins.GOLD_ORE_ROCK, "gold_ore");
        oreRock(BigOreVeins.COPPER_ORE_ROCK, "copper_ore");

        echoPillar();
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
        MultiPartBlockStateBuilder builder = getMultipartBuilder(Turtles.TURTLE_SCUTE.get());
        for (int layer = 1; layer <= 16; layer++) {
            Integer[] heights = IntStream.rangeClosed(layer - 1, 15).boxed().toArray(Integer[]::new);
            ModelFile model = new ModelFile.UncheckedModelFile(modLoc("block/turtle_scute/stack_" + layer));
            for (int y : new int[]{0, 90, 180, 270}) {
                builder.part().modelFile(model).rotationY(y).addModel()
                    .condition(ScuteBlock.HEIGHT, heights);
            }
        }
    }

    private void flower(String name, Block flower, Block potted) {
        simpleBlock(flower,
                models().withExistingParent(name, mcLoc("block/cross"))
                        .renderType("cutout")
                        .texture("cross", modLoc("block/" + name)));
        simpleBlock(potted,
                models().withExistingParent("potted_" + name, mcLoc("block/flower_pot_cross"))
                        .texture("plant", modLoc("block/" + name)));
    }

    private void echoPillar() {
        ModelFile upperDisabled = models().cubeBottomTop("echo_pillar_upper_disabled",
            modLoc("block/echo_pillar_side_disabled"),
            modLoc("block/echo_pillar_bottom"),
            modLoc("block/echo_pillar_bottom"));
        ModelFile upperEnabled = models().cubeBottomTop("echo_pillar_upper_enabled",
            modLoc("block/echo_pillar_side_enabled"),
            modLoc("block/echo_pillar_bottom"),
            modLoc("block/echo_pillar_bottom"));
        ModelFile lowerDisabled = models().cubeBottomTop("echo_pillar_lower_disabled",
            modLoc("block/echo_pillar_side_lower_disabled"),
            modLoc("block/echo_pillar_bottom"),
            modLoc("block/echo_pillar_bottom"));
        ModelFile lowerEnabled = models().cubeBottomTop("echo_pillar_lower_enabled",
            modLoc("block/echo_pillar_side_lower_enabled"),
            modLoc("block/echo_pillar_bottom"),
            modLoc("block/echo_pillar_bottom"));
        getVariantBuilder(EchoPillar.ECHO_PILLAR.block().get())
            .partialState().with(EchoPillarBlock.ENABLED, false).with(EchoPillarBlock.HALF, DoubleBlockHalf.LOWER).modelForState().modelFile(lowerDisabled).addModel()
            .partialState().with(EchoPillarBlock.ENABLED, true).with(EchoPillarBlock.HALF, DoubleBlockHalf.LOWER).modelForState().modelFile(lowerEnabled).addModel()
            .partialState().with(EchoPillarBlock.ENABLED, false).with(EchoPillarBlock.HALF, DoubleBlockHalf.UPPER).modelForState().modelFile(upperDisabled).addModel()
            .partialState().with(EchoPillarBlock.ENABLED, true).with(EchoPillarBlock.HALF, DoubleBlockHalf.UPPER).modelForState().modelFile(upperEnabled).addModel();
    }

    private void oreRock(SimpleBlockWithItem rock, String oreTextureName) {
        String name = rock.block().getId().getPath();
        ModelFile model = models().withExistingParent(name, modLoc("block/ore_rock"))
                .texture("particle", mcLoc("block/" + oreTextureName))
                .texture("texture", mcLoc("block/" + oreTextureName));
        VariantBlockStateBuilder builder = getVariantBuilder((Block) rock.block().get());
        for (Boolean waterlogged : new Boolean[]{false, true}) {
            builder.partialState().with(GroundRockBlock.WATERLOGGED, waterlogged).setModels(
                new ConfiguredModel(model, 0, 0, false),
                new ConfiguredModel(model, 0, 90, false),
                new ConfiguredModel(model, 0, 180, false),
                new ConfiguredModel(model, 0, 270, false)
            );
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
