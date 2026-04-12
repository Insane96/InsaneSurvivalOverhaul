package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor
    int getAttackStrengthTicker();

    @Accessor
    float getLastHurt();
    @Accessor
    void setLastHurt(float lastHurt);

    @Invoker("getSoundVolume")
    float soundVolume();
    @Invoker("getVoicePitch")
    float voicePitch();
}
