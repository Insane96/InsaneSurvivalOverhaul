package insane96mcp.insanesurvivaloverhaul.mixin.module.combat;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanesurvivaloverhaul.module.combat.PiercingDamage;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin_PiercingDamage {

    /**
     * After {@code actuallyHurt} completes, reads {@link PiercingDamage#SHOULD_STOP_HURT} to decide
     * whether the {@code hurt} call should return true or false, then clears the tag.
     * This ensures the correct result is propagated when piercing damage triggers
     * multiple sequential {@code hurt} calls on the same entity.
     */
    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V", shift = At.Shift.AFTER), cancellable = true)
    public void insanesurvivaloverhaul$cancelOnPiercingDead(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
        if (ModNBTData.contains(self(), PiercingDamage.SHOULD_STOP_HURT)) {
            cir.setReturnValue(ModNBTData.get(self(), PiercingDamage.SHOULD_STOP_HURT, Boolean.class));
            ModNBTData.remove(self(), PiercingDamage.SHOULD_STOP_HURT);
        }
    }

    private LivingEntity self() {
        return (LivingEntity) (Object) this;
    }
}
