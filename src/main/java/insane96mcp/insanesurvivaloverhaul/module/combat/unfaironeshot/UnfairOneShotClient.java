package insane96mcp.insanesurvivaloverhaul.module.combat.unfaironeshot;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import static insane96mcp.insanesurvivaloverhaul.module.combat.unfaironeshot.UnfairOneShot.HALF_HEART_TEXTURE;

public class UnfairOneShotClient {
	public static int activationTicks = 0;
	@OnlyIn(Dist.CLIENT)
	public static void registerGuiLayers(RegisterGuiLayersEvent event) {
		event.registerBelowAll(InsaneSO.location("unfair_oneshot_animation"), (guiGraphics, deltaTracker) -> {
            if (!Feature.isEnabled(UnfairOneShot.class)
					|| activationTicks == 0)
                return;

			float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
			int screenWidth = guiGraphics.guiWidth();
			int screenHeight = guiGraphics.guiHeight();
			int tick = 30 - activationTicks;
			float f = ((float)tick + partialTicks) / 30f;
			float f1 = f * f;
			float f2 = f * f1;
			float f3 = 10.25F * f2 * f1 - 24.95F * f1 * f1 + 25.5F * f2 - 13.8F * f1 + 4.0F * f;
			float f4 = f3 * (float)Math.PI;
			float f5 = 0;/*activationOffX * (float)(screenWidth / 4)*/;
			RenderSystem.enableDepthTest();
			RenderSystem.disableCull();
			PoseStack posestack = guiGraphics.pose();
			posestack.pushPose();
			posestack.translate((float)(screenWidth / 2) + f5 * Mth.abs(Mth.sin(f4 * 2f)), /*(float)(screenHeight / 2) + f6 * Mth.abs(Mth.sin(f4 * 2.0F))*/(screenHeight * f), -50.0F);
			float f7 = 120.0F * Mth.sin(f4);
			posestack.scale(f7, -f7, f7);
			posestack.mulPose(Axis.YP.rotationDegrees(180.0F * Mth.abs(Mth.sin(f4))));
            MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
			Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(HALF_HEART_TEXTURE.get()), ItemDisplayContext.FIXED, 15728880, OverlayTexture.NO_OVERLAY, posestack, multibuffersource$buffersource, Minecraft.getInstance().level, 0);
			posestack.popPose();
			multibuffersource$buffersource.endBatch();
			RenderSystem.enableCull();
			RenderSystem.disableDepthTest();
        });
	}

	@SubscribeEvent
	public void onRenderTick(LevelTickEvent.Post event) {
		if (activationTicks > 0 && event.getLevel().isClientSide && event.getLevel().dimension() == Level.OVERWORLD)
			activationTicks--;
	}
}