package insane96mcp.insanesurvivaloverhaul.module.misc.glowblock;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class GlowBlockClient {
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Fires once per frame, right before block entities (and thus GlowBlockEntityRenderer) render:
        // clear last frame's set so it only ever holds positions rendered (i.e. visible/loaded) this frame.
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES)
            GlowBlockEntityRenderer.VISIBLE_THIS_FRAME.clear();
    }

    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(InsaneSO.id("glow_through_walls"), (guiGraphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null || mc.options.hideGui || !Feature.isEnabled(GlowBlockFeature.class))
                return;

            BlockPos target = getLookedAtGlowBlock(player, deltaTracker.getGameTimeDeltaPartialTick(true));
            if (target == null)
                return;

            Component message = getAlignmentMessage(player, target);
            int width = mc.font.width(message);
            guiGraphics.drawString(mc.font, message, guiGraphics.guiWidth() - width - 4, 4, GlowBlockEntityRenderer.BORDER_COLOR);
        });
    }

    /**
     * The glow block closest to the crosshair (smallest angle between the view vector and the direction
     * to its center), among those within {@link GlowBlockFeature#lookAngle} degrees or whose AABB the view
     * ray crosses directly (so close blocks still count when looking at their edges). Works through walls
     * since it's not a real clip against level collision. Null if none.
     */
    private static BlockPos getLookedAtGlowBlock(Player player, float partialTick) {
        if (GlowBlockEntityRenderer.VISIBLE_THIS_FRAME.isEmpty())
            return null;

        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 look = player.getViewVector(partialTick);
        Vec3 end = eye.add(look.scale(256));
        double minCos = Math.cos(Math.toRadians(GlowBlockFeature.lookAngle));

        BlockPos closest = null;
        double bestCos = -1;
        for (BlockPos pos : GlowBlockEntityRenderer.VISIBLE_THIS_FRAME) {
            double cos = pos.getCenter().subtract(eye).normalize().dot(look);
            if (cos < minCos && new AABB(pos).clip(eye, end).isEmpty())
                continue;
            if (cos > bestCos) {
                bestCos = cos;
                closest = pos;
            }
        }
        return closest;
    }

    /**
     * Which of the player's eye X/Y/Z coordinates match the target block's (e.g. "XZ" when standing
     * directly above/below it), or "not aligned" if none match.
     */
    private static Component getAlignmentMessage(Player player, BlockPos target) {
        BlockPos eyePos = BlockPos.containing(player.getEyePosition());

        StringBuilder axes = new StringBuilder();
        if (eyePos.getX() == target.getX())
            axes.append("X");
        if (eyePos.getY() == target.getY())
            axes.append("Y");
        if (eyePos.getZ() == target.getZ())
            axes.append("Z");

        return axes.isEmpty()
                ? Component.translatable("hud_info.glow_block_not_aligned")
                : Component.translatable("hud_info.glow_block_aligned", axes.toString());
    }
}
