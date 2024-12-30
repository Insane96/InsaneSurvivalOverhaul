package insane96mcp.iguanatweaksreborn.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import insane96mcp.iguanatweaksreborn.module.client.Misc;
import insane96mcp.iguanatweaksreborn.module.client.WorldBorder;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@ModifyVariable(at = @At(value = "STORE"), method = "renderWorldBorder", ordinal = 4)
	private double onWorldBorderHeight(double value) {
		if (WorldBorder.shouldShorten())
			return Math.min(WorldBorder.capHeight, value / 4d);
		return value;
	}

	@ModifyVariable(at = @At(value = "STORE", ordinal = 2), method = "renderWorldBorder", ordinal = 1)
	private double onWorldBorderAlpha(double value) {
		return value * WorldBorder.getTransparencyMultiplier();
	}

	@Inject(method = "renderHitOutline", at = @At(value = "HEAD"), cancellable = true)
	private void onRenderHitOutline(PoseStack pPoseStack, VertexConsumer pConsumer, Entity entity, double pCamX, double pCamY, double pCamZ, BlockPos pPos, BlockState state, CallbackInfo ci) {
		if (!(entity instanceof Player player)
				|| player.getAbilities().instabuild
				|| !state.requiresCorrectToolForDrops()
				|| player.hasCorrectToolForDrops(state)
				|| !Misc.shouldHideBlockBreakOutline())
			return;
		ci.cancel();
	}
}
