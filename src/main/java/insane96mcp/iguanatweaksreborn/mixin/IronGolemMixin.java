package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.mobs.MiscMobs;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.animal.IronGolem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(IronGolem.class)
public abstract class IronGolemMixin {
	@Shadow protected abstract float getAttackDamage();

	@ModifyArg(method = "doHurtTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"), index = 1)
	public float iguanatweaksreborn$onDamageAmount(float original) {
		if (!Feature.isEnabled(MiscMobs.class)
				|| !MiscMobs.fixedIronGolemDamage)
			return original;

		return this.getAttackDamage();
	}
}
