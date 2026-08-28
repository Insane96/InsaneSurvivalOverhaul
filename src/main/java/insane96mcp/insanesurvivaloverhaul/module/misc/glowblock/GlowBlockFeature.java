package insane96mcp.insanesurvivaloverhaul.module.misc.glowblock;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.MISC, description = "A block that's visible through walls, for testing the see-through-walls render technique.")
public class GlowBlockFeature extends Feature {
    public static final SimpleBlockWithItem GLOW_BLOCK = SimpleBlockWithItem.register("glow_block",
            () -> new GlowBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1f)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GlowBlockEntity>> GLOW_BLOCK_ENTITY =
            ISORegistries.BLOCK_ENTITIES.register("glow_block",
                    () -> BlockEntityType.Builder.of(GlowBlockEntity::new, GLOW_BLOCK.block().get()).build(null));
}
