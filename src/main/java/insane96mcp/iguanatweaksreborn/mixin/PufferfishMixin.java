package insane96mcp.iguanatweaksreborn.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Pufferfish.class)
public abstract class PufferfishMixin extends AbstractFish {

    public PufferfishMixin(EntityType<? extends AbstractFish> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @ModifyArg(method = "touch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;<init>(Lnet/minecraft/world/effect/MobEffect;II)V"), index = 1)
    private int iguanatweaksreborn$fixPoisonContinuousDamageMobs(int pDuration) {
        return pDuration + 10;
    }

    @ModifyArg(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;<init>(Lnet/minecraft/world/effect/MobEffect;II)V"), index = 1)
    private int iguanatweaksreborn$fixPoisonContinuousDamagePlayers(int pDuration) {
        return pDuration + 10;
    }
}
