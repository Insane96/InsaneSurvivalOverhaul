package insane96mcp.insanesurvivaloverhaul.mixin.module.death.respawn.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.respawn.Ghostly;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin_GhostlyEffect {

	/**
	 * Returns false for entities with the Ghostly effect so the renderer picks a translucent render type.
	 */
	@Inject(method = "isBodyVisible", at = @At("RETURN"), cancellable = true)
	private void insanesurvivaloverhaul$ghostlyBodyVisible(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
		if (Feature.isEnabled(Ghostly.class) && entity.hasEffect(Ghostly.GHOSTLY))
			cir.setReturnValue(false);
	}

	/**
	 * Raises the alpha to 50% opacity for entities with the Ghostly effect instead of vanilla's 15%.
	 */
	@WrapOperation(
			method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V")
	)
	private void insanesurvivaloverhaul$ghostlyRenderColor(EntityModel<?> model, com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer, int packedLight, int packedOverlay, int color, Operation<Void> original, @Local(argsOnly = true) LivingEntity entity) {
		if (Feature.isEnabled(Ghostly.class) && entity.hasEffect(Ghostly.GHOSTLY))
			color = 0x80FFFFFF;
		original.call(model, poseStack, buffer, packedLight, packedOverlay, color);
	}
}
