package insane96mcp.iguanatweaksreborn.module.world;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.RegistryObject;

@LoadFeature(module = Modules.Ids.WORLD, canBeDisabled = false)
public class CyanFlower extends Feature {

    public static final SimpleBlockWithItem FLOWER = SimpleBlockWithItem.register("cyan_flower", () -> new FlowerBlock(() -> MobEffects.MOVEMENT_SLOWDOWN, 10, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).pushReaction(PushReaction.DESTROY).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ)));
    public static final RegistryObject<Block> POTTED_FLOWER = ISORegistries.BLOCKS.register("potted_cyan_flower", () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, () -> FLOWER.block().get(), BlockBehaviour.Properties.copy(Blocks.POTTED_ALLIUM)));

    public CyanFlower(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }
}