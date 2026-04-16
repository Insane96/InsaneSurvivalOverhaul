package insane96mcp.insanesurvivaloverhaul.mixin.module.world;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.world.spawners.Spawners;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin_Spawners {

	@ModifyExpressionValue(
			method = "serverTick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BaseSpawner;isNearPlayer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z")
	)
	private boolean serverTick(boolean isPlayerNearby, ServerLevel level, BlockPos blockPos) {
		if (isPlayerNearby && Spawners.onSpawnerServerTick((BaseSpawner) (Object) this))
			return false;
		return isPlayerNearby;
	}

	@ModifyExpressionValue(
			method = "clientTick",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BaseSpawner;isNearPlayer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z")
	)
	private boolean clientTick(boolean isPlayerNearby, Level level, BlockPos blockPos) {
		if (isPlayerNearby && Spawners.onSpawnerClientTick((BaseSpawner) (Object) this, level))
			return false;
		return isPlayerNearby;
	}

	@Inject(method = "delay", at = @At("RETURN"))
	private void onDelay(Level level, BlockPos pos, CallbackInfo ci) {
		Spawners.onSpawnerDelaySet((BaseSpawner) (Object) this, level, pos);
	}
}