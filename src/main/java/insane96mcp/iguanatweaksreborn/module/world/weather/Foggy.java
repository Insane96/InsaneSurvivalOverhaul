package insane96mcp.iguanatweaksreborn.module.world.weather;

import insane96mcp.insanelib.util.weightedrandom.IWeightedRandom;
import insane96mcp.insanelib.util.weightedrandom.WeightedRandom;
import net.minecraft.util.RandomSource;

import java.util.Arrays;

public enum Foggy implements IWeightedRandom {
    NONE(0.5f, 1.0f, false,1f, 10),
    LIGHT(0.4f, 0.8f, false, 1f, 10),
    MEDIUM(64, 192, true, 0.8f, 10),
    HEAVY(16, 96, true, 0.4f, 4),
    VERY_HEAVY(0, 48, true, 0.2f, 2),
    SILENT_HILL(-16, 32, true, 0.1f, 1);

    final float nearDistance;
    final float farDistance;
    final boolean flat;
    final float timerMultiplier;
    final int weight;

    Foggy(float nearDistance, float farDistance, boolean flat, float timerMultiplier, int weight) {
        this.nearDistance = nearDistance;
        this.farDistance = farDistance;
        this.flat = flat;
        this.timerMultiplier = timerMultiplier;
        this.weight = weight;
    }

    public float getNearDistance(float renderDistance, Foggy targetFoggy, float changingRatio) {
        float currentNearDistance = flat ? nearDistance : renderDistance * nearDistance;
        float newNearDistance = targetFoggy.flat ? targetFoggy.nearDistance : renderDistance * targetFoggy.nearDistance;
        float delta = newNearDistance - currentNearDistance;
        return currentNearDistance + delta * changingRatio;
    }

    public float getFarDistance(float renderDistance, Foggy targetFoggy, float changingRatio) {
        float currentFarDistance = flat ? farDistance : renderDistance * farDistance;
        float newFarDistance = targetFoggy.flat ? targetFoggy.farDistance : renderDistance * targetFoggy.farDistance;
        float delta = newFarDistance - currentFarDistance;
        return currentFarDistance + delta * changingRatio;
    }

    public static Foggy getRandom(RandomSource random) {
        return WeightedRandom.getRandomItem(random, Arrays.asList(values()));
    }

    @Override
    public int getWeight() {
        return this.weight;
    }
}
