package insane96mcp.insanesurvivaloverhaul.mixin.module.world.sponges;

import insane96mcp.insanesurvivaloverhaul.module.world.Sponges;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin_Sponges {
    /**
     * Delegates block tick events to {@link Sponges#onSpongeTick} to handle weather-based
     * sponge state changes: dry sponges become wet when rained on (with sky access), and
     * wet sponges dry out during daytime without rain.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    public void insanesurvivaloverhaul$onBlockTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        Sponges.onSpongeTick(state, level, pos, random);
    }
}
