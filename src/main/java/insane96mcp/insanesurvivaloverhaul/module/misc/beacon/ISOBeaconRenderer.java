package insane96mcp.insanesurvivaloverhaul.module.misc.beacon;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.List;

public class ISOBeaconRenderer implements BlockEntityRenderer<ISOBeaconBlockEntity> {
    public ISOBeaconRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BeaconFeature.BEACON_BLOCK_ENTITY.get(), ISOBeaconRenderer::new);
    }

    @Override
    public void render(ISOBeaconBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        long gameTime = blockEntity.getLevel().getGameTime();
        List<ISOBeaconBlockEntity.BeaconBeamSection> sections = blockEntity.getBeamSections();
        int yOffset = 0;
        for (int i = 0; i < sections.size(); i++) {
            ISOBeaconBlockEntity.BeaconBeamSection section = sections.get(i);
            int height = i == sections.size() - 1 ? BeaconRenderer.MAX_RENDER_Y : section.getHeight();
            BeaconRenderer.renderBeaconBeam(poseStack, bufferSource, BeaconRenderer.BEAM_LOCATION, partialTick, 1.0F, gameTime, yOffset, height, section.getColor(), 0.2F, 0.25F);
            yOffset += section.getHeight();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(ISOBeaconBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(ISOBeaconBlockEntity blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(cameraPos.multiply(1.0, 0.0, 1.0), this.getViewDistance());
    }

    @Override
    public AABB getRenderBoundingBox(ISOBeaconBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, BeaconRenderer.MAX_RENDER_Y, pos.getZ() + 1.0);
    }
}
