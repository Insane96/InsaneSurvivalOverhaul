package insane96mcp.insanesurvivaloverhaul.module.combat.fletching;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Arrow;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ISOArrowRenderer extends ArrowRenderer<Arrow> {
    private static final ResourceLocation QUARTZ = InsaneSO.id("textures/entity/projectiles/quartz_arrow.png");
    private static final ResourceLocation DIAMOND = InsaneSO.id("textures/entity/projectiles/diamond_arrow.png");
    private static final ResourceLocation EXPLOSIVE = InsaneSO.id("textures/entity/projectiles/explosive_arrow.png");
    private static final ResourceLocation TORCH = InsaneSO.id("textures/entity/projectiles/torch_arrow.png");
    private static final ResourceLocation ICE = InsaneSO.id("textures/entity/projectiles/ice_arrow.png");

    public ISOArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Arrow entity) {
        if (entity.getType() == FletchingFeature.QUARTZ_ARROW.get())
            return QUARTZ;
        if (entity.getType() == FletchingFeature.DIAMOND_ARROW.get())
            return DIAMOND;
        if (entity.getType() == FletchingFeature.EXPLOSIVE_ARROW.get())
            return EXPLOSIVE;
        if (entity.getType() == FletchingFeature.TORCH_ARROW.get())
            return TORCH;
        return ICE;
    }

    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FletchingFeature.QUARTZ_ARROW.get(), ISOArrowRenderer::new);
        event.registerEntityRenderer(FletchingFeature.DIAMOND_ARROW.get(), ISOArrowRenderer::new);
        event.registerEntityRenderer(FletchingFeature.EXPLOSIVE_ARROW.get(), ISOArrowRenderer::new);
        event.registerEntityRenderer(FletchingFeature.TORCH_ARROW.get(), ISOArrowRenderer::new);
        event.registerEntityRenderer(FletchingFeature.ICE_ARROW.get(), ISOArrowRenderer::new);
    }
}
