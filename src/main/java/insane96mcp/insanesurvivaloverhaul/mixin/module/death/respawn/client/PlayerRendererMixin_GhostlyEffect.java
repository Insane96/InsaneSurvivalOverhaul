package insane96mcp.insanesurvivaloverhaul.mixin.module.death.respawn.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.death.respawn.Ghostly;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin_GhostlyEffect {

	/**
	 * Switches the arm skin from the opaque entitySolid render type to entityTranslucent
	 * so the alpha channel in the color is respected for ghostly players.
	 */
	@WrapOperation(
			method = "renderHand",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entitySolid(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;")
	)
	private RenderType insanesurvivaloverhaul$ghostlyArmRenderType(ResourceLocation texture, Operation<RenderType> original, @Local(argsOnly = true) AbstractClientPlayer player) {
		if (Feature.isEnabled(Ghostly.class) && player.hasEffect(Ghostly.GHOSTLY))
			return RenderType.entityTranslucent(texture);
		return original.call(texture);
	}

	/**
	 * Applies 50% alpha to both the arm and sleeve renders for ghostly players.
	 */
	@WrapOperation(
			method = "renderHand",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V")
	)
	private void insanesurvivaloverhaul$ghostlyArmColor(ModelPart model, com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer, int packedLight, int packedOverlay, Operation<Void> original, @Local(argsOnly = true) AbstractClientPlayer player) {
		if (Feature.isEnabled(Ghostly.class) && player.hasEffect(Ghostly.GHOSTLY)) {
			model.render(poseStack, buffer, packedLight, packedOverlay, 0x80FFFFFF);
			return;
		}
		original.call(model, poseStack, buffer, packedLight, packedOverlay);
	}
}
