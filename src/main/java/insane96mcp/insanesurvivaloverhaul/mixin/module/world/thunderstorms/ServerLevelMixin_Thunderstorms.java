package insane96mcp.insanesurvivaloverhaul.mixin.module.world.thunderstorms;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.world.thunderstorms.Thunderstorms;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin_Thunderstorms extends Level {
    protected ServerLevelMixin_Thunderstorms(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, Supplier<ProfilerFiller> profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, profiler, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    /**
     * Modifies the lightning strike chance divisor to scale with thunderstorm intensity.
     */
    @ModifyExpressionValue(method = "tickChunk", at = @At(value = "CONSTANT", args = "intValue=100000"))
    private int insanesurvivaloverhaul$changeThunderChance(int original) {
        return Thunderstorms.getLightningStrikeChance((ServerLevel) (Object) this, original);
    }
}
