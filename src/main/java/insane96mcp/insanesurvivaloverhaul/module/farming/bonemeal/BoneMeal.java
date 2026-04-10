package insane96mcp.insanesurvivaloverhaul.module.farming.bonemeal;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.criterion.MakeRichFarmlandTrigger;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOBlockTagsProvider;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.mining.blockdefinition.BlockDefinition;
import insane96mcp.insanesurvivaloverhaul.module.mining.blockdefinition.BlockDefinitionReloadListener;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@LoadFeature(module = ISOModules.FARMING, description = "Increase uses for bone meal and nerf its use on some plants")
public class BoneMeal extends Feature {

    public static final SimpleBlockWithItem RICH_FARMLAND = SimpleBlockWithItem.register("rich_farmland", () -> new RichFarmlandBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).randomTicks().strength(0.6F).sound(SoundType.GRAVEL).isViewBlocking((state, blockGetter, pos) -> true).isSuffocating((state, blockGetter, pos) -> true)));
    public static final Supplier<MakeRichFarmlandTrigger> MAKE_RICH_FARMLAND =
            ISORegistries.registerTrigger("make_rich_farmland", MakeRichFarmlandTrigger::new);

    public static final TagKey<Item> ITEM_BLACKLIST = ISOItemTagsProvider.create("bone_meal_blacklist");
    public static final TagKey<Block> BLOCK_BLACKLIST = ISOBlockTagsProvider.create("bone_meal_blacklist");

    @Config(description = "Bone meal used on Farmland (or crouch right clicked on crops) transforms it into Rich Farmland.")
    public static Boolean richFarmland$enable = false;
    @Config(min = 1, description = "How many extra random ticks does Rich Farmland give to the crop sitting on top?")
    public static Integer richFarmland$extraTicks = 4;
    @Config(min = 0d, max = 1d, description = "Chance for a Rich farmland to decay back to farmland")
    public static Double richFarmland$chanceToDecay = 0.35d;
    @Config(description = "Prevents bone meal from working on crops and will just create rich farmland on right-click")
    public static Boolean richFarmland$disableBoneMeal = true;

    @Config(min = 0, max = 25, description = "How many stages will bone meal make stuff grow?")
    public static MinMaxConfig stageGrowth = new MinMaxConfig(1);

    @Config(min = 0, description = "Vanilla is 20")
    public static Integer composterTimeToProduce = 50;

    @Config(description = "If true, you can bone meal dirt that's near a grass block (or below tall grass) to get grass block.")
    public static Boolean boneMealDirtToGrass = true;

    @Config(min = 0, description = "How many stages will cactus and sugar canes grow with one bone meal. Set to 0 to disable.")
    public static MinMaxConfig boneMealCanesAndCactus = new MinMaxConfig(1, 4);

    @Config(description = "Makes bone meal have a chance to fail on Cave Vines, Saplings and Sweet Berry Bushes. Also makes rotten flesh compostable.")
    public static Boolean dataPack = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("bone_meal", "Insane's Survival Overhaul Bone Meal", () -> this.isEnabled() && !Packs.disableAllDataPacks && dataPack);
    }

    @SubscribeEvent
    public void onBoneMeal(BonemealEvent event) {
        if (event.isCanceled()
                || !this.isEnabled()
                || event.getLevel().isClientSide
                || event.getStack().is(ITEM_BLACKLIST)
                || event.getState().is(BLOCK_BLACKLIST))
            return;

        tryMakeRichFarmland(event);
        if (event.isCanceled())
            return;
        tryConsumeWithFail(event);
        if (event.isCanceled())
            return;
        applyNerfedBoneMeal(event);

        if (!event.isCanceled()) {
            if (boneMealDirtToGrass)
                tryBoneMealDirt(event, event.getLevel(), event.getState(), event.getPos());

            tryBoneMealCanesAndCactus(event, event.getLevel(), event.getState(), event.getPos());
        }
    }

    public static int composterTimeToProduce(int original) {
        return Feature.isEnabled(BoneMeal.class) ? BoneMeal.composterTimeToProduce : original;
    }

    private void tryMakeRichFarmland(BonemealEvent event) {
        if (!richFarmland$enable)
            return;
        BlockPos farmlandPos = null;
        if (event.getState().is(Blocks.FARMLAND))
            farmlandPos = event.getPos();
        else if (event.getLevel().getBlockState(event.getPos().below()).is(Blocks.FARMLAND) && (event.getPlayer() != null && event.getPlayer().isCrouching() || richFarmland$disableBoneMeal))
            farmlandPos = event.getPos().below();
        else if (richFarmland$disableBoneMeal && event.getLevel().getBlockState(event.getPos().below()).is(RICH_FARMLAND.block().get())) {
            event.setCanceled(true);
        }
        if (farmlandPos != null) {
            event.getLevel().setBlockAndUpdate(farmlandPos, RICH_FARMLAND.block().get().defaultBlockState().setValue(FarmBlock.MOISTURE, event.getLevel().getBlockState(farmlandPos).getValue(FarmBlock.MOISTURE)));
            if (event.getPlayer() != null) {
                event.getPlayer().swing(event.getPlayer().getMainHandItem().getItem() == event.getStack().getItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
                MAKE_RICH_FARMLAND.get().trigger((ServerPlayer) event.getPlayer());
            }
            event.getStack().shrink(1);
            event.setSuccessful(true);
        }
    }

    private void tryConsumeWithFail(BonemealEvent event) {
        Optional<IntegerProperty> oAgeProperty = getAgeProperty(event.getState());
        if (oAgeProperty.isPresent()) {
            int age = event.getState().getValue(oAgeProperty.get());
            int maxAge = AGE_PROPERTIES.get(oAgeProperty.get());
            if (age == maxAge)
                return;
        }
        failFromBlockDefinitions(event);
    }

    private void failFromBlockDefinitions(BonemealEvent event) {
        for (BlockDefinition blockDefinition : BlockDefinitionReloadListener.DEFINITIONS) {
            if (!blockDefinition.matches(event.getState())
                    || blockDefinition.boneMealFailChance == null)
                continue;
            if (event.getLevel().random.nextFloat() < blockDefinition.boneMealFailChance) {
                event.getStack().shrink(1);
                event.setSuccessful(true);
                return;
            }
        }
    }

    private void applyNerfedBoneMeal(BonemealEvent event) {
        BlockState state = event.getState();
        if (state.getBlock() instanceof BushBlock) {
            Optional<IntegerProperty> oAgeProperty = getAgeProperty(state);
            if (oAgeProperty.isEmpty()) {
                InsaneSO.LOGGER.debug("No vanilla age property found for state {}}", state);
                return;
            }
            int age = state.getValue(oAgeProperty.get());
            int maxAge = AGE_PROPERTIES.get(oAgeProperty.get());
            if (age == maxAge)
                return;

            age = Mth.clamp(age + stageGrowth.getIntRandBetween(event.getLevel().random), 0, maxAge);
            event.getLevel().setBlockAndUpdate(event.getPos(), state.setValue(oAgeProperty.get(), age));
            if (state.getBlock() instanceof StemBlock && age == maxAge)
                state.randomTick((ServerLevel) event.getLevel(), event.getPos(), event.getLevel().random);
            event.getStack().shrink(1);
            event.setSuccessful(true);
        }
    }

    private void tryBoneMealDirt(BonemealEvent event, Level level, BlockState state, BlockPos pos) {
        if (!state.is(Blocks.DIRT)
                || (!level.getBlockState(pos.above()).isAir() && !level.getBlockState(pos.above()).is(ISOBlockTagsProvider.TALL_GRASS)))
            return;

        if (level.getBlockState(pos.above()).is(ISOBlockTagsProvider.TALL_GRASS)) {
            growGrassBlock(level, pos, event.getPlayer(), event.getStack(), event);
            return;
        }
        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP || direction == Direction.DOWN)
                continue;
            if (level.getBlockState(pos.relative(direction)).is(Blocks.GRASS_BLOCK)) {
                growGrassBlock(level, pos, event.getPlayer(), event.getStack(), event);
                break;
            }
        }
    }

    private void growGrassBlock(Level level, BlockPos pos, Player player, ItemStack stack, BonemealEvent event) {
        level.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
        if (player != null)
            player.swing(player.getMainHandItem().getItem() == stack.getItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
        stack.shrink(1);
        event.setSuccessful(true);
    }

    private void tryBoneMealCanesAndCactus(BonemealEvent event, Level level, BlockState state, BlockPos pos) {
        if (!state.is(Blocks.SUGAR_CANE) && !state.is(Blocks.CACTUS))
            return;
        if (!level.isEmptyBlock(pos.above()))
            return;
        int growthAmount = boneMealCanesAndCactus.getIntRandBetween(level.random);
        if (growthAmount == 0)
            return;
        IntegerProperty ageProperty = state.is(Blocks.SUGAR_CANE) ? SugarCaneBlock.AGE : CactusBlock.AGE;
        int height = 1;
        while (level.getBlockState(pos.below(height)).is(Blocks.SUGAR_CANE) || level.getBlockState(pos.below(height)).is(Blocks.CACTUS)) {
            height++;
        }
        if (height >= 3)
            return;

        int age = state.getValue(ageProperty);
        age += growthAmount;
        if (age >= 15) {
            level.setBlockAndUpdate(pos.above(), state.getBlock().defaultBlockState());
            level.setBlock(pos, state.setValue(ageProperty, age - 15), 4);
        }
        else {
            level.setBlock(pos, state.setValue(ageProperty, age), 4);
        }
        if (event.getPlayer() != null)
            event.getPlayer().swing(event.getPlayer().getMainHandItem().getItem() == event.getStack().getItem() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, true);
        event.getStack().shrink(1);
        event.setSuccessful(true);
    }

    private static final Map<IntegerProperty, Integer> AGE_PROPERTIES = Map.of(
            BlockStateProperties.AGE_1, 1,
            BlockStateProperties.AGE_2, 2,
            BlockStateProperties.AGE_3, 3,
            BlockStateProperties.AGE_4, 4,
            BlockStateProperties.AGE_5, 5,
            BlockStateProperties.AGE_7, 7,
            BlockStateProperties.AGE_15, 15,
            BlockStateProperties.AGE_25, 25
    );

    public static Optional<IntegerProperty> getAgeProperty(BlockState state) {
        for (var ageProperty : AGE_PROPERTIES.entrySet()) {
            if (state.hasProperty(ageProperty.getKey()))
                return Optional.of(ageProperty.getKey());
        }
        return Optional.empty();
    }

    public static boolean isFullyGrown(BlockState state) {
        for (var ageProperty : AGE_PROPERTIES.entrySet()) {
            if (state.hasProperty(ageProperty.getKey()) && state.getValue(ageProperty.getKey()).equals(ageProperty.getValue()))
                return true;
        }
        return false;
    }
}
