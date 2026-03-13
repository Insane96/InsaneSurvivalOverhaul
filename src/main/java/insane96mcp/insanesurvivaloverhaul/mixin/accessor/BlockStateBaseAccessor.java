package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.BlockStateBase.class)
public interface BlockStateBaseAccessor {
    @Accessor
    float getDestroySpeed();
    @Accessor
    @Mutable
    void setDestroySpeed(float destroySpeed);

    @Accessor
    boolean getRequiresCorrectToolForDrops();
    @Accessor
    @Mutable
    void setRequiresCorrectToolForDrops(boolean requiresCorrectToolForDrops);

    @Accessor
    NoteBlockInstrument getInstrument();
    @Accessor
    @Mutable
    void setInstrument(NoteBlockInstrument instrument);
}
