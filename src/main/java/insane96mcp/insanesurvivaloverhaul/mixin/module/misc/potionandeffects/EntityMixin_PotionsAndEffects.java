package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.potionandeffects;

import insane96mcp.insanesurvivaloverhaul.module.misc.PotionsAndEffects;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin_PotionsAndEffects {
    /**
     * Makes the entity immune to fire when it has the Fire Resistance effect and the
     * {@link PotionsAndEffects#preventFireWithResistance} option is enabled, preventing
     * entities from being set on fire at all (matching Bedrock Edition behaviour).
     */
    @Inject(at = @At(value = "RETURN"), method = "fireImmune", cancellable = true)
    private void insanesurvivaloverhaul$onFireImmune(CallbackInfoReturnable<Boolean> cir) {
        if (PotionsAndEffects.isFireImmune((Entity) (Object) this))
            cir.setReturnValue(true);
    }
}
