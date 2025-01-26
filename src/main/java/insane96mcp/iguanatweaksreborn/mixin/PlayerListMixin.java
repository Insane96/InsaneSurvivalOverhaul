package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn.Respawn;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Inject(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getLevelData()Lnet/minecraft/world/level/storage/LevelData;"))
	private void iguanatweaksreborn$tryLooseRespawnWithoutPos(ServerPlayer pPlayer, boolean pKeepEverything, CallbackInfoReturnable<ServerPlayer> cir, @Local(ordinal = 1) ServerPlayer serverPlayer) {
		Optional<Vec3> respawnPos = Respawn.tryLooseRespawn((ServerLevel) serverPlayer.level(), serverPlayer, Optional.empty());
		respawnPos.ifPresent(serverPlayer::moveTo);
	}
}
