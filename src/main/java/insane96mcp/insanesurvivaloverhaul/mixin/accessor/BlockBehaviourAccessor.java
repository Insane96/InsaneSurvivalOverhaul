package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccessor {
    @Accessor
    @Mutable
    void setIsRandomlyTicking(boolean isRandomlyTicking);

    @Accessor
    float getExplosionResistance();
    @Accessor
    @Mutable
    void setExplosionResistance(float explosionResistance);

    @Accessor
    float getFriction();
    @Accessor
    @Mutable
    void setFriction(float friction);

    @Accessor
    float getSpeedFactor();
    @Accessor
    @Mutable
    void setSpeedFactor(float speedFactor);

    @Accessor
    float getJumpFactor();
    @Accessor
    @Mutable
    void setJumpFactor(float jumpFactor);
}
