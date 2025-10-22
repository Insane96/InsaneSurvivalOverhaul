package insane96mcp.iguanatweaksreborn.module.client;

import insane96mcp.iguanatweaksreborn.module.ClientModules;
import insane96mcp.iguanatweaksreborn.module.world.weather.ClientWeather;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = ClientModules.Ids.CLIENT)
public class Fog extends Feature {

    @Config(name = "Better visibility in Lava with Fire Resistance Lava", description = "If true you'll be able to see better in lava when with the Fire Resistance Effect.")
    public static Boolean betterFireResistanceLavaFog = true;
    @Config(description = "If true Nether Fog is no longer limited to 12 chunks.")
    public static Boolean betterNetherFog = true;
    @Config(min = 0d, max = 1d, description = "Render distance is multiplied by this value in the Nether. Vanilla is 0.5.")
    public static Double netherFogRatio = 0.75d;
    @Config(description = "If true, overworld fog will be use the values from the config.")
    public static Boolean overworldFog = true;
    @Config(min = 0d, max = 1d, description = "Changes fog to start closer to the player. E.g. A value of 0.5 makes fog start at half the render distance. Vanilla is 1, Pre-1.18.1 was 0.75, Pre-1.7.2 was 0.25. This disables itself if Foggy Weather is enabled.")
    public static Double overworld$fogStartRatio = 0.4d;
    @Config(max = 1d, description = "Changes fog ratio when raining. Vanilla is 1. This disables itself if Foggy Weather is enabled.")
    public static Double overworld$fogStartRatioOnRain = -0.2d;

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
                || !overworldFog
                || event.isCanceled()
                || event.getCamera().getFluidInCamera() != FogType.NONE
                || (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN && event.getMode() != FogRenderer.FogMode.FOG_SKY))
            return;

        if (!(event.getCamera().getEntity() instanceof LivingEntity entity))
            return;
        float rainLevel = entity.level().getRainLevel(1f);
        float ratio = overworld$fogStartRatio.floatValue();
        if (rainLevel > 0f) {
            float skyLight = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition());
            float skyLightRatio = skyLight / 12f;
            if (skyLightRatio > 1f)
                skyLightRatio = 1f;
            float interpolatedRatio = overworld$fogStartRatio.floatValue() * (1f - skyLightRatio)
                    + overworld$fogStartRatioOnRain.floatValue() * skyLightRatio;
            event.setNearPlaneDistance(event.getFarPlaneDistance() * (ratio - (rainLevel * (ratio - interpolatedRatio))));
        }
        else
            event.setNearPlaneDistance(event.getFarPlaneDistance() * ratio);
        event.setCanceled(true);
    }

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
