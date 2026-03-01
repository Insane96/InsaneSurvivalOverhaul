package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor
    int getAttackStrengthTicker();

    @Accessor
    float getLastHurt();
    @Accessor
    void setLastHurt(float lastHurt);
}
