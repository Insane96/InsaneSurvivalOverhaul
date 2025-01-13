package insane96mcp.iguanatweaksreborn.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

	@ModifyVariable(at = @At(value = "STORE"), method = "renderWorldBorder", ordinal = 4)
	private double onWorldBorderHeight(double value) {
		if (WorldBorder.shouldShorten())
			return Math.min(WorldBorder.capHeight, value / 4d);
		return value;
	}

	@ModifyExpressionValue(method = "renderHitOutline", at = @At(value = "CONSTANT", args = "floatValue=0.0", ordinal = 0))
	private float onHitOutline(float value, PoseStack pPoseStack, VertexConsumer pConsumer, Entity entity, double pCamX, double pCamY, double pCamZ, BlockPos pPos, BlockState state) {
		if (!(entity instanceof Player player)
				|| player.getAbilities().instabuild
				|| !state.requiresCorrectToolForDrops()
				|| player.hasCorrectToolForDrops(state))
			return value;
		return Misc.getRedOutlineAmount(value);
	}
}
