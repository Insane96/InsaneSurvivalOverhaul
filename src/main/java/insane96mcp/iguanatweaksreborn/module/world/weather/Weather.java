package insane96mcp.iguanatweaksreborn.module.world.weather;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.network.message.FoggySync;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Weather", description = "Thunderstorm Intensity and Foggy Weather")
@LoadFeature(module = Modules.Ids.WORLD)
public class Weather extends Feature {
    public static final GameRules.Key<GameRules.BooleanValue> RULE_THUNDERSTORMINTENSITY = GameRules.register("iguanatweaks:thunderstormIntensity", GameRules.Category.MISC, GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> RULE_FOGGYWEATHER = GameRules.register("iguanatweaks:foggyWeather", GameRules.Category.MISC, GameRules.BooleanValue.create(true));

    @Config(min = 1)
    @Label(name = "Thunderstorm Intensity.Min Intensity", description = "Minimum thunderstorm intensity.")
    public static Integer thunderstormIntensityMin = 1;
    @Config(min = 1)
    @Label(name = "Thunderstorm Intensity.Max Intensity", description = "Maximum thunderstorm intensity.")
    public static Integer thunderstormIntensityMax = 15;
    @Config(min = 1)
    @Label(name = "Thunderstorm Intensity.Base Duration", description = "Base duration of each intensity (in minutes). Lasts less and less the higher the intensity")
    public static Integer thunderstormIntensityBaseDuration = 4;

    @Config(min = 1)
    @Label(name = "Foggy Weather.Min Time", description = "Minimum time (in minutes) a foggy weather can last.")
    public static Integer foggyWeatherMinTime = 5;
    @Config(min = 1)
    @Label(name = "Foggy Weather.Max Time", description = "Maximum time (in minutes) a foggy weather can last.")
    public static Integer foggyWeatherMaxTime = 30;

    public Weather(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (!this.isEnabled()
                || event.phase != TickEvent.Phase.START
                || event.level.dimension() != Level.OVERWORLD
                || !(event.level instanceof ServerLevel serverLevel)
                /*|| event.level.tickCount % 20 != 10*/)
            return;

        tickFoggyWeather(serverLevel, WeatherSavedData.get(serverLevel));
        tickVariableThunderstorm(serverLevel, WeatherSavedData.get(serverLevel));
    }

    public static void tickFoggyWeather(ServerLevel level, WeatherSavedData wsd) {
        if (!level.getGameRules().getBoolean(RULE_FOGGYWEATHER))
            return;
        WeatherSavedData.FoggyData foggyData = wsd.foggyData;
        if (foggyData.targetTime == -1)
            foggyData.targetTime = getNewFoggyTargetTime(level.random);

        if (++foggyData.timer >= foggyData.targetTime) {
            if (foggyData.current == foggyData.target) {
                foggyData.target = Foggy.values()[level.random.nextInt(Foggy.values().length)];
                foggyData.timer = 0;
                foggyData.targetTime = getNewFoggyTargetTime(level.random);
                level.players().forEach(player ->
                        FoggySync.sync(player, foggyData));
            }
            else {
                foggyData.current = foggyData.target;
                foggyData.timer = 0;
                foggyData.targetTime = getNewFoggyTargetTime(level.random);
                foggyData.targetTime = (int) (foggyData.targetTime * foggyData.current.timerMultiplier);
            }
        }
        wsd.setDirty();
    }

    private static int getNewFoggyTargetTime(RandomSource random) {
        return (int) ((random.nextFloat() * (foggyWeatherMaxTime - foggyWeatherMinTime) + foggyWeatherMinTime) * 60 * 20);
    }

    @SubscribeEvent
    public void onPlayerJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().dimension() != Level.OVERWORLD
                || !(event.getEntity() instanceof ServerPlayer player))
            return;

        FoggySync.sync(player, WeatherSavedData.get(player.serverLevel()).foggyData);
    }

    public static void clearFoggyWeather(ServerLevel level) {
        WeatherSavedData wsd = WeatherSavedData.get(level);
        WeatherSavedData.FoggyData foggyData = wsd.foggyData;
        foggyData.current = Foggy.NONE;
        foggyData.target = Foggy.NONE;
        foggyData.timer = 0;
        foggyData.targetTime = -1;
        wsd.setDirty();
        level.players().forEach(player -> FoggySync.sync(player, foggyData));
    }

    public static WeatherSavedData.FoggyData getCurrentFoggyData(ServerLevel level) {
        return WeatherSavedData.get(level).foggyData;
    }

    public static void tickVariableThunderstorm(ServerLevel level, WeatherSavedData wsd) {
        if (!level.getGameRules().getBoolean(RULE_THUNDERSTORMINTENSITY))
            return;
        WeatherSavedData.ThunderIntensityData tid = wsd.thunderIntensityData;
        if (tid.targetIntensity == -1)
            tid.targetIntensity = getNewTargetIntensity(level.random);

        if (--tid.timer <= 0) {
            tid.timer = (int) ((thunderstormIntensityBaseDuration * 60 * 20) + level.random.nextFloat() * (thunderstormIntensityBaseDuration * 60 * 20)) / tid.getIntensity();
            int delta = tid.targetIntensity > tid.getIntensity() ? 1 : -1;
            tid.addIntensity(delta);
            if (tid.getIntensity() == tid.targetIntensity)
                tid.targetIntensity = getNewTargetIntensity(level.random);
        }
        wsd.setDirty();
    }

    private static int getNewTargetIntensity(RandomSource random) {
        return random.nextInt(thunderstormIntensityMin, thunderstormIntensityMax + 1);
    }

    public static int getLightningStrikeChance(ServerLevel level, int original) {
        if (!Feature.isEnabled(Weather.class)
                || !level.getGameRules().getBoolean(RULE_THUNDERSTORMINTENSITY))
            return original;
        return original / WeatherSavedData.get(level).thunderIntensityData.getIntensity();
    }

    public static WeatherSavedData.ThunderIntensityData getCurrentThunderIntensityData(ServerLevel level) {
        return WeatherSavedData.get(level).thunderIntensityData;
    }

}
