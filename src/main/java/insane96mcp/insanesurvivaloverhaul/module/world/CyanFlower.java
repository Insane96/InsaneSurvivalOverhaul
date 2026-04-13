package insane96mcp.insanesurvivaloverhaul.module.world;

import com.mojang.serialization.MapCodec;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.WORLD, canBeDisabled = false)
public class CyanFlower extends Feature {

    public static final SimpleBlockWithItem FLOWER = SimpleBlockWithItem.register("cyan_flower", () -> new FlowerBlock(MobEffects.MOVEMENT_SLOWDOWN, 10, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)));
    public static final DeferredHolder<Block, FlowerPotBlock> POTTED_FLOWER = ISORegistries.BLOCKS.register("potted_cyan_flower", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, () -> FLOWER.block().get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_ALLIUM)));

    public static class CyanFlowerSeedBlock extends BushBlock {

        protected CyanFlowerSeedBlock(Properties properties) {
            super(properties);
        }

        @Override
        protected MapCodec<? extends BushBlock> codec() {
            return null;
        }
    }
}