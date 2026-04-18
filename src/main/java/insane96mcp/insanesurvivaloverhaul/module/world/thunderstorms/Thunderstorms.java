package insane96mcp.insanesurvivaloverhaul.module.world.thunderstorms;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@LoadFeature(module = ISOModules.WORLD, canBeDisabled = false, description = "Thunderstorm Intensity. Disable it via gamerule: insanesurvivaloverhaul:thunderstormIntensity")
public class Thunderstorms extends Feature {
    public static final GameRules.Key<GameRules.BooleanValue> RULE_THUNDERSTORMINTENSITY = GameRules.register(
            "insanesurvivaloverhaul:thunderstormIntensity", GameRules.Category.MISC, GameRules.BooleanValue.create(true));

    @Config(min = 1, name = "Min Intensity", description = "Minimum thunderstorm intensity.")
    public static Integer thunderstormIntensityMin = 1;
    @Config(min = 1, name = "Max Intensity", description = "Maximum thunderstorm intensity.")
    public static Integer thunderstormIntensityMax = 15;
    @Config(min = 1, name = "Base Duration", description = "Base duration of each intensity step (in minutes). Lasts less and less the higher the intensity.")
    public static Integer thunderstormIntensityBaseDuration = 4;

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent.Pre event) {
        if (!this.isEnabled()
                || event.getLevel().dimension() != Level.OVERWORLD
                || !(event.getLevel() instanceof ServerLevel serverLevel))
            return;

        advanceVariableThunderstorm(serverLevel, 1);
    }

    public static void advanceVariableThunderstorm(ServerLevel level, int ticks) {
        if (!level.getGameRules().getBoolean(RULE_THUNDERSTORMINTENSITY)
                || !level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE))
            return;

        ThunderstormsSavedData wsd = ThunderstormsSavedData.get(level);
        ThunderstormsSavedData.ThunderIntensityData tid = wsd.thunderIntensityData;
        if (tid.targetIntensity <= 0)
            tid.targetIntensity = getNewTargetIntensity(level.random);

        tid.timer -= ticks;
        while (tid.timer <= 0) {
            tid.timer += (int) ((thunderstormIntensityBaseDuration * 60 * 20) + level.random.nextFloat() * (thunderstormIntensityBaseDuration * 60 * 20)) / tid.getIntensity();
            int delta = tid.targetIntensity > tid.getIntensity() ? 1 : -1;
            tid.addIntensity(delta);
            if (tid.getIntensity() == tid.targetIntensity)
                tid.targetIntensity = getNewTargetIntensity(level.random);
        }
        wsd.setDirty();
    }

    public static void onSkipNight(int timeSkipped, ServerLevel level) {
        if (!Feature.isEnabled(Thunderstorms.class)
                || !level.getGameRules().getBoolean(GameRules.RULE_WEATHER_CYCLE))
            return;

        if (level.getGameRules().getBoolean(RULE_THUNDERSTORMINTENSITY))
            advanceVariableThunderstorm(level, timeSkipped);
    }

    private static int getNewTargetIntensity(RandomSource random) {
        return random.nextInt(thunderstormIntensityMin, thunderstormIntensityMax + 1);
    }

    public static int getLightningStrikeChance(ServerLevel level, int original) {
        if (!Feature.isEnabled(Thunderstorms.class)
                || !level.getGameRules().getBoolean(RULE_THUNDERSTORMINTENSITY))
            return original;
        return original / ThunderstormsSavedData.get(level).thunderIntensityData.getIntensity();
    }

    public static ThunderstormsSavedData.ThunderIntensityData getCurrentThunderIntensityData(ServerLevel level) {
        return ThunderstormsSavedData.get(level).thunderIntensityData;
    }
}
