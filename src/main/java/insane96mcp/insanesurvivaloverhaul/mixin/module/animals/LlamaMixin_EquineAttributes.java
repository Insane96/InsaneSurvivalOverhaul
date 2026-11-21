package insane96mcp.insanesurvivaloverhaul.mixin.module.animals;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.LlamaAccessor;
import insane96mcp.insanesurvivaloverhaul.module.animals.equineattributes.EquineAttributes;
import insane96mcp.insanesurvivaloverhaul.module.animals.equineattributes.EquineStats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.horse.Llama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Llama.class)
public abstract class LlamaMixin_EquineAttributes {
	/**
	 * Uses the configured strength instead of the vanilla random one when a llama spawns
	 */
	@Inject(method = "setRandomStrength", at = @At("HEAD"), cancellable = true)
	private void insanesurvivaloverhaul$onSetRandomStrength(RandomSource random, CallbackInfo ci) {
		if (!Feature.isEnabled(EquineAttributes.class))
			return;

		Llama llama = (Llama) (Object) this;
		EquineStats stats = EquineAttributes.getStats(llama.getType());
		if (stats == null || stats.strength == null)
			return;

		((LlamaAccessor) llama).invokeSetStrength(stats.strength.getIntRandBetween(random));
		ci.cancel();
	}

	/**
	 * Bred llamas get the strongest parent's strength, with a configurable chance of +1, instead of a random value up to the strongest parent's one
	 */
	@WrapOperation(method = "getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/animal/horse/Llama;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/Llama;setStrength(I)V"))
	private void insanesurvivaloverhaul$onBredStrength(Llama baby, int strength, Operation<Void> original, @Local(argsOnly = true) AgeableMob otherParent) {
		if (Feature.isEnabled(EquineAttributes.class) && otherParent instanceof Llama otherLlama)
			strength = EquineAttributes.getBredLlamaStrength(baby, (Llama) (Object) this, otherLlama, strength);
		original.call(baby, strength);
	}
}
