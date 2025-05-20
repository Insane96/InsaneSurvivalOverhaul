package insane96mcp.iguanatweaksreborn.module.client;

import insane96mcp.iguanatweaksreborn.module.ClientModules;
import insane96mcp.iguanatweaksreborn.module.world.weather.ClientWeather;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Fog", description = "Makes fog less invasive in some contexts")
@LoadFeature(module = ClientModules.Ids.CLIENT)
public class Fog extends Feature {

    @Config
    @Label(name = "Better visibility in Lava with Fire Resistance Lava", description = "If true you'll be able to see better in lava when with the Fire Resistance Effect.")
    public static Boolean betterFireResistanceLavaFog = true;
    @Config
    @Label(name = "Better Nether Fog", description = "If true Nether Fog is no longer limited to 12 chunks.")
    public static Boolean betterNetherFog = true;
    @Config(min = 0d, max = 1d)
    @Label(name = "Nether Fog Ratio", description = "Render distance is multiplied by this value in the Nether. Vanilla is 0.5.")
    public static Double netherFogRatio = 0.75d;
    @Config(min = 0d, max = 1d)
    @Label(description = "Changes fog to start closer to the player. E.g. A value of 0.5 makes fog start at half the render distance. Vanilla is 1, Pre-1.18.1 was 0.75, Pre-1.7.2 was 0.25. This disables itself if Foggy Weather is enabled.")
    public static Double overworld$fogStartRatio = 0.4d;
    @Config(max = 1d)
    @Label(description = "Changes fog ratio when raining. Vanilla is 1. This disables itself if Foggy Weather is enabled.")
    public static Double overworld$fogStartRatioOnRain = -0.2d;

    public Fog(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onFog(ViewportEvent.RenderFog event) {
        if (!this.isEnabled())
            return;

        lavaFog(event);
        netherFog(event);
        overworldFog(event);
    }

    private void overworldFog(ViewportEvent.RenderFog event) {
        if (Feature.get(ClientWeather.class).isEnabled()
                || overworld$fogStartRatio == 1d && overworld$fogStartRatioOnRain == 1d
                || event.isCanceled()
                || event.getCamera().getFluidInCamera() != FogType.NONE
                || event.getMode() != FogRenderer.FogMode.FOG_TERRAIN)
            return;

        if (!(event.getCamera().getEntity() instanceof LivingEntity entity))
            return;
        float rainLevel = entity.level().getRainLevel(1f);
        float ratio = overworld$fogStartRatio.floatValue();
        if (rainLevel > 0f) {
            float ratioOnRain = overworld$fogStartRatioOnRain.floatValue();
            event.setNearPlaneDistance(event.getFarPlaneDistance() * (ratio - (rainLevel * (ratio - ratioOnRain))));
        }
        else
            event.setNearPlaneDistance(event.getFarPlaneDistance() * ratio);
        event.setCanceled(true);
    }

    /*private void seasonFog(ViewportEvent.RenderFog event) {
        if (!fogChangeOnRain
                || event.isCanceled()
                || event.getCamera().getFluidInCamera() != FogType.NONE
                || event.getMode() != FogRenderer.FogMode.FOG_TERRAIN)
            return;

        if (!(event.getCamera().getEntity() instanceof LivingEntity entity))
            return;
        if (entity.getEyeInFluidType() != ForgeMod.EMPTY_TYPE.get()
                || entity.level().dimension() != Level.OVERWORLD
                || !entity.level().isRaining()
                || entity.hasEffect(MobEffects.BLINDNESS)
                //TODO Smooth out
                || entity.getY() < entity.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, entity.blockPosition()).getY() - 16d)
            return;

        float renderDistance = Minecraft.getInstance().gameRenderer.getRenderDistance();
        float rainLevel = entity.level().getRainLevel(1f);
        //Lower than 1 means a percentage of current render distance, higher means a fixed render distance
        float near = switch (SeasonHelper.getSeasonState(entity.level()).getSeason()) {
            case SPRING -> 0.7F;
            case SUMMER -> 0.85F;
            case AUTUMN -> 0.6F;
            case WINTER -> 24F;
        };
        float far = switch (SeasonHelper.getSeasonState(entity.level()).getSeason()) {
            case SPRING -> 0.8F;
            case SUMMER -> 0.9F;
            case AUTUMN -> 0.75F;
            case WINTER -> 48F;
        };
        if (near <= 1f) event.setNearPlaneDistance(renderDistance * near);
        else event.setNearPlaneDistance(near);
        if (far <= 1f) event.setFarPlaneDistance(renderDistance * far);
        else event.setFarPlaneDistance(far);
        event.setCanceled(true);
    }*/

    public void lavaFog(ViewportEvent.RenderFog event) {
        if (!betterFireResistanceLavaFog
                || event.getCamera().getFluidInCamera() != FogType.LAVA
                || event.getCamera().getEntity().isSpectator())
            return;

        Entity entity = event.getCamera().getEntity();
        if (entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            event.setNearPlaneDistance(-16);
            event.setFarPlaneDistance(16f);
            event.setCanceled(true);
        }
    }

    public void netherFog(ViewportEvent.RenderFog event) {
        if (!betterNetherFog
                || event.isCanceled())
            return;

        Entity entity = event.getCamera().getEntity();
        if (entity.getEyeInFluidType() == ForgeMod.EMPTY_TYPE.get() && entity.level().dimension() == Level.NETHER) {
            float renderDistance = Minecraft.getInstance().gameRenderer.getRenderDistance();
            event.setNearPlaneDistance((float) (renderDistance * netherFogRatio / 10f));
            event.setFarPlaneDistance((float) (renderDistance * netherFogRatio));
            event.setCanceled(true);
        }
    }
}
