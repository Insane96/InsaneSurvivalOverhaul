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
            IntProvider.NON_NEGATIVE_CODEC.fieldOf("radius").forGetter((configuration) -> configuration.radius),
            Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter((configuration) -> configuration.targetStates),
            Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter((configuration) -> configuration.discardChanceOnAirExposure),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("center_falloff", 0.0F).forGetter((configuration) -> configuration.centerFalloff),
            RandomPatchConfiguration.CODEC.fieldOf("surface_patch_config").forGetter((configuration) -> configuration.patchConfiguration)
    ).apply(p_67849_, OreWithRandomPatchConfiguration::new));

    public final IntProvider radius;
    public final List<OreConfiguration.TargetBlockState> targetStates;
    public final float discardChanceOnAirExposure;
    public final float centerFalloff;
    public final RandomPatchConfiguration patchConfiguration;

    public OreWithRandomPatchConfiguration(IntProvider radius, List<OreConfiguration.TargetBlockState> targetStates, float discardChanceOnAirExposure, float centerFalloff, RandomPatchConfiguration patchConfiguration) {
        this.radius = radius;
        this.targetStates = targetStates;
        this.discardChanceOnAirExposure = discardChanceOnAirExposure;
        this.centerFalloff = centerFalloff;
        this.patchConfiguration = patchConfiguration;
    }
}
