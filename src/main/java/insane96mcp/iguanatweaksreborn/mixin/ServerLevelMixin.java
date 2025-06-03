package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.Tiredness;
import insane96mcp.iguanatweaksreborn.module.world.seasons.Seasons;
import insane96mcp.iguanatweaksreborn.module.world.weather.Weather;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
    protected ServerLevelMixin(WritableLevelData pLevelData, ResourceKey<Level> pDimension, RegistryAccess pRegistryAccess, Holder<DimensionType> pDimensionTypeRegistration, Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed, int pMaxChainedNeighborUpdates) {
        super(pLevelData, pDimension, pRegistryAccess, pDimensionTypeRegistration, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed, pMaxChainedNeighborUpdates);
    }

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isRaining()Z"))
    private boolean iguanatweaksreborn$preventRainClear(boolean original) {
        return Tiredness.onSleepFinished((ServerLevel) (Object) this, original);
    }

    @ModifyExpressionValue(method = "tickChunk", at = @At(value = "CONSTANT", args = "intValue=100000"))
    private int iguanatweaksreborn$changeThunderChance(int original) {
        return Weather.getLightningStrikeChance((ServerLevel) (Object) this, original);
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isRandomlyTicking()Z"))
    public boolean iguanatweaksreborn$randomTickBlock(BlockState state, Operation<Boolean> original, @Local(name = "blockpos3") BlockPos pos, @Local ProfilerFiller profiler) {
        if (!ModList.get().isLoaded("sereneseasons")
                || !Feature.get(Seasons.class).isEnabled())
            return original.call(state);
        profiler.push("iguanatweaksreborn:seasons");
        Seasons.tickPlantLifeDeath(state, pos, (ServerLevel) (Object) this);
        profiler.pop();
        return original.call(state);
    }
}
