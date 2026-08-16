package insane96mcp.insanesurvivaloverhaul.module.misc.turtles;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.MISC, description = "Turtle helmet water breathing duration and stackable scute blocks.")
public class Turtles extends Feature {

    public static final DeferredHolder<Block, ScuteBlock> TURTLE_SCUTE = ISORegistries.BLOCKS.register("turtle_scute", () -> new ScuteBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.2F, 0.5F).offsetType(BlockBehaviour.OffsetType.XZ).dynamicShape().sound(SoundType.BONE_BLOCK)));

    @Config(description = "The ticks of Water Breathing given by the Turtle Helmet. Vanilla is 200")
    public static Integer helmetWaterBreathingTime = 900;
    @Config(description = "If true, scute will drop as a block and not as item")
    public static Boolean scuteDropsAsBlock = true;

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!this.isEnabled()
                || !scuteDropsAsBlock
                || !event.getItemStack().is(Items.TURTLE_SCUTE))
            return;

        BlockPos clickedPos = event.getPos();
        BlockState clickedState = event.getLevel().getBlockState(clickedPos);

        BlockPos placePos;
        BlockState newState;

        // Stack on an existing scute block by clicking its top face
        if (clickedState.is(TURTLE_SCUTE.get()) && event.getFace() == Direction.UP) {
            int height = clickedState.getValue(ScuteBlock.HEIGHT);
            if (height >= 15)
                return;
            placePos = clickedPos;
            newState = clickedState.setValue(ScuteBlock.HEIGHT, height + 1);
        } else {
            placePos = clickedPos.relative(event.getFace());
            if (!event.getLevel().getWorldBorder().isWithinBounds(placePos))
                return;
            if (!event.getLevel().getBlockState(placePos).canBeReplaced())
                return;
            newState = TURTLE_SCUTE.get().defaultBlockState().setValue(ScuteBlock.HEIGHT, 0);
            if (!newState.canSurvive(event.getLevel(), placePos))
                return;
        }

        if (!event.getLevel().isClientSide()) {
            event.getLevel().setBlock(placePos, newState, Block.UPDATE_ALL);
            SoundType soundType = newState.getSoundType();
            event.getLevel().playSound(null, placePos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);
            if (!event.getEntity().getAbilities().instabuild)
                event.getItemStack().shrink(1);
        }
        event.getEntity().swing(event.getHand());
        event.setCanceled(true);
    }
}
