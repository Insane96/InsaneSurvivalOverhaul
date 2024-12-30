package insane96mcp.iguanatweaksreborn.module.world.weather;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Client Weather")
@LoadFeature(module = Modules.Ids.WORLD, canBeDisabled = false)
public class ClientWeather extends Feature {
    public ClientWeather(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!this.isEnabled()
                || event.phase != TickEvent.Phase.START
                || event.level.dimension() != Level.OVERWORLD
                /*|| event.level.tickCount % 20 != 10*/)
            return;

        tickFoggyWeather(event.level);
    }

    private static int foggyTimer = 0;
    private static int foggyTargetTime = 200;
    private static Foggy currentFoggy = Foggy.NONE;
    private static Foggy targetFoggy = Foggy.NONE;

    public static void tickFoggyWeather(Level level) {
        if (!level.isClientSide)
            return;

        //for (Player player : level.players())
        //    player.displayClientMessage(Component.literal("foggyTimer: %d, foggyTargetTime: %d, currentFoggy: %s, targetFoggy: %s".formatted(foggyTimer, foggyTargetTime, currentFoggy, targetFoggy)), true);

        if (currentFoggy != targetFoggy && ++foggyTimer >= foggyTargetTime)
            currentFoggy = targetFoggy;
    }

    public static void updateFoggy(WeatherSavedData.FoggyData foggyData) {
        foggyTimer = foggyData.timer;
        foggyTargetTime = foggyData.targetTime;
        currentFoggy = foggyData.current;
        targetFoggy = foggyData.target;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onFog(ViewportEvent.RenderFog event) {
        if (!this.isEnabled()
                || event.isCanceled()
                || event.getCamera().getFluidInCamera() != FogType.NONE
                || !(event.getCamera().getEntity() instanceof LivingEntity livingEntity)
                || livingEntity.hasEffect(MobEffects.BLINDNESS)
                || event.getMode() != FogRenderer.FogMode.FOG_TERRAIN)
            return;

        float changingRatio = 1f;
        if (currentFoggy != targetFoggy)
            changingRatio = ((float) foggyTimer / foggyTargetTime);
        event.setNearPlaneDistance(currentFoggy.getNearDistance(event.getFarPlaneDistance(), targetFoggy, changingRatio));
        event.setFarPlaneDistance(currentFoggy.getFarDistance(event.getFarPlaneDistance(), targetFoggy, changingRatio));
        event.setCanceled(true);
    }

    /*public static int getRenderDistance(int original) {
        if (!currentFoggy.flat)
            return original;
        return (int) (currentFoggy.farDistance / 16);
    }*/
}
