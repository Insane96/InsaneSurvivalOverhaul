package insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

import java.util.List;

public class OreWithRandomPatchConfiguration implements FeatureConfiguration {
    public static final Codec<OreWithRandomPatchConfiguration> CODEC = RecordCodecBuilder.create((p_67849_) -> p_67849_.group(
            IntProvider.NON_NEGATIVE_CODEC.fieldOf("width").forGetter((configuration) -> configuration.width),
            IntProvider.NON_NEGATIVE_CODEC.fieldOf("height").forGetter((configuration) -> configuration.height),
            Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter((configuration) -> configuration.targetStates),
            Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter((configuration) -> configuration.discardChanceOnAirExposure),
            RandomPatchConfiguration.CODEC.fieldOf("surface_patch_config").forGetter((configuration) -> configuration.patchConfiguration)
    ).apply(p_67849_, OreWithRandomPatchConfiguration::new));

    public IntProvider width;
    public IntProvider height;
    public final List<OreConfiguration.TargetBlockState> targetStates;
    public final float discardChanceOnAirExposure;
    public RandomPatchConfiguration patchConfiguration;

    public OreWithRandomPatchConfiguration(IntProvider width, IntProvider height, List<OreConfiguration.TargetBlockState> targetStates, float discardChanceOnAirExposure, RandomPatchConfiguration patchConfiguration) {
        this.width = width;
        this.height = height;
        this.targetStates = targetStates;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.patchConfiguration = patchConfiguration;
    }
}
