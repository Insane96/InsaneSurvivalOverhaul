package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.iguanatweaksreborn.module.world.Fluids;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract void resetFallDistance();

    @Shadow(remap = false)
    protected Object2DoubleMap<FluidType> forgeFluidTypeHeight;

    @Shadow public abstract float getBbHeight();

    @Shadow public boolean horizontalCollision;

    @Shadow public boolean minorHorizontalCollision;

    @Shadow private Level level;

    @Shadow public abstract boolean hurt(DamageSource pSource, float pAmount);

    @Shadow public abstract DamageSources damageSources();

    @Inject(at = @At(value = "RETURN"), method = "fireImmune", cancellable = true)
    private void onFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if (Tweaks.isFireImmune((Entity) (Object) this))
            cir.setReturnValue(true);
    }

    @ModifyConstant(method = "updateInWaterStateAndDoFluidPushing", constant = @Constant(floatValue = 1f))
    private float onFluidFallModifer(float waterFallDamageModifier) {
        return Fluids.shouldOverrideWaterFallDamageModifier() && this.forgeFluidTypeHeight.object2DoubleEntrySet().stream().anyMatch(e -> e.getKey().equals(ForgeMod.WATER_TYPE.get())) ? 0.75f : waterFallDamageModifier;
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;resetFallDistance()V"))
    private void onResetFallDistanceInMove(Entity instance) {
        if (Fluids.shouldOverrideWaterFallDamageModifier())
            return;
        this.resetFallDistance();
    }

    @Redirect(method = "updateInWaterStateAndDoWaterCurrentPushing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;resetFallDistance()V"))
    private void onResetFallDistanceInUpdateInWaterState(Entity instance) {
        if (Fluids.shouldOverrideWaterFallDamageModifier())
            return;
        this.resetFallDistance();
    }
}
