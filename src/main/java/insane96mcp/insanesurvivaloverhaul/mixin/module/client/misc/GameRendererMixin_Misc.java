package insane96mcp.insanesurvivaloverhaul.mixin.module.client.misc;

import insane96mcp.insanesurvivaloverhaul.module.client.Misc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class GameRendererMixin_Misc {

    /**
     * Suppresses the screen-tilt effect for non-directional damage types (magic, wither, fire,
     * cramming, thorns, drowning) since they have no meaningful direction to tilt towards.
     */
    @ModifyVariable(method = "bobHurt", at = @At(value = "STORE"), ordinal = 3)
    public float insanesurvivaloverhaul$suppressNonDirectionalTilt(float tilt) {
        if (!Misc.shouldDisableTiltingWithNonDirectionalDamageTypes())
            return tilt;
        LivingEntity livingEntity = (LivingEntity) Minecraft.getInstance().cameraEntity;
        DamageSource lastDamageSource = livingEntity.getLastDamageSource();
        if (lastDamageSource == null)
            return tilt;
        if (lastDamageSource.is(DamageTypes.MAGIC)
                || lastDamageSource.is(DamageTypes.WITHER)
                || lastDamageSource.is(DamageTypes.ON_FIRE)
                || lastDamageSource.is(DamageTypes.CRAMMING)
                || lastDamageSource.is(DamageTypes.THORNS)
                || lastDamageSource.is(DamageTypes.DROWN))
            return 0f;
        return tilt;
    }
}
