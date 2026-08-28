package insane96mcp.insanesurvivaloverhaul.module.misc.glowblock;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;

public class GlowBlockEntityRenderer implements BlockEntityRenderer<GlowBlockEntity> {
    static final int BORDER_COLOR = FastColor.ARGB32.color(255, 255, 140, 0); // alpha, r, g, b
    private static final int COLOR = FastColor.ARGB32.color(160, 255, 80, 0); // alpha, r, g, b

    // Positions rendered (i.e. in a visible, loaded chunk) during the current frame's block entity pass.
    // Cleared each frame by GlowThroughWallsClient right before block entities render, then read by its
    // GUI layer afterward to tell whether the player is looking at one, even through walls.
    static final Set<BlockPos> VISIBLE_THIS_FRAME = new HashSet<>();

    private static final RenderType GLOW_THROUGH_WALLS = RenderType.create(
            "iso_glow_through_walls",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    // rendertype_lines needs POSITION_COLOR_NORMAL + its dedicated shader: the shader extrudes each
    // line's two vertices into a screen-space quad using the normal as the line direction, so plain
    // POSITION_COLOR + a generic shader silently draws nothing.
    private static final RenderType GLOW_THROUGH_WALLS_LINES = RenderType.create(
            "iso_glow_through_walls_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(2.0)))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    public GlowBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(GlowBlockFeature.GLOW_BLOCK_ENTITY.get(), GlowBlockEntityRenderer::new);
    }

    @Override
    public int getViewDistance() {
        return 256; // default BlockEntityRenderer view distance is 64, too short for a block meant to be seen from afar
    }

    @Override
    public void render(GlowBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        PoseStack.Pose pose = poseStack.last();
        renderCube(bufferSource.getBuffer(GLOW_THROUGH_WALLS), pose, 0f, 0f, 0f, 1f, 1f, 1f, COLOR);
        renderCubeEdges(bufferSource.getBuffer(GLOW_THROUGH_WALLS_LINES), pose, 0f, 0f, 0f, 1f, 1f, 1f, BORDER_COLOR);
        VISIBLE_THIS_FRAME.add(be.getBlockPos());
    }

    private static void renderCube(VertexConsumer consumer, PoseStack.Pose pose, float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        // -X / +X
        quad(consumer, pose, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1, color);
        quad(consumer, pose, x1, y0, z1, x1, y1, z1, x1, y1, z0, x1, y0, z0, color);
        // -Y / +Y
        quad(consumer, pose, x0, y0, z0, x0, y0, z1, x1, y0, z1, x1, y0, z0, color);
        quad(consumer, pose, x0, y1, z1, x0, y1, z0, x1, y1, z0, x1, y1, z1, color);
        // -Z / +Z
        quad(consumer, pose, x1, y0, z0, x1, y1, z0, x0, y1, z0, x0, y0, z0, color);
        quad(consumer, pose, x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1, color);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                              float x0, float y0, float z0, float x1, float y1, float z1,
                              float x2, float y2, float z2, float x3, float y3, float z3, int color) {
        consumer.addVertex(pose, x0, y0, z0).setColor(color);
        consumer.addVertex(pose, x1, y1, z1).setColor(color);
        consumer.addVertex(pose, x2, y2, z2).setColor(color);
        consumer.addVertex(pose, x3, y3, z3).setColor(color);
    }

    private static void renderCubeEdges(VertexConsumer consumer, PoseStack.Pose pose, float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        // bottom face
        line(consumer, pose, x0, y0, z0, x1, y0, z0, color);
        line(consumer, pose, x1, y0, z0, x1, y0, z1, color);
        line(consumer, pose, x1, y0, z1, x0, y0, z1, color);
        line(consumer, pose, x0, y0, z1, x0, y0, z0, color);
        // top face
        line(consumer, pose, x0, y1, z0, x1, y1, z0, color);
        line(consumer, pose, x1, y1, z0, x1, y1, z1, color);
        line(consumer, pose, x1, y1, z1, x0, y1, z1, color);
        line(consumer, pose, x0, y1, z1, x0, y1, z0, color);
        // vertical edges
        line(consumer, pose, x0, y0, z0, x0, y1, z0, color);
        line(consumer, pose, x1, y0, z0, x1, y1, z0, color);
        line(consumer, pose, x1, y0, z1, x1, y1, z1, color);
        line(consumer, pose, x0, y0, z1, x0, y1, z1, color);
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose,
                              float x0, float y0, float z0, float x1, float y1, float z1, int color) {
        float nx = x1 - x0, ny = y1 - y0, nz = z1 - z0;
        consumer.addVertex(pose, x0, y0, z0).setColor(color).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x1, y1, z1).setColor(color).setNormal(pose, nx, ny, nz);
    }
}
