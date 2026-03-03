package insane96mcp.insanesurvivaloverhaul.mixin;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.mobs.MiscMobs;
import net.minecraft.world.entity.animal.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(IronGolem.class)
public abstract class IronGolemMixin {
	@Shadow protected abstract float getAttackDamage();

	@ModifyArg(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
	public float insanesurvivaloverhaul$onDamageAmount(float original) {
		if (!Feature.isEnabled(MiscMobs.class)
				|| !MiscMobs.fixedIronGolemDamage)
			return original;

		return this.getAttackDamage();
	}
}
