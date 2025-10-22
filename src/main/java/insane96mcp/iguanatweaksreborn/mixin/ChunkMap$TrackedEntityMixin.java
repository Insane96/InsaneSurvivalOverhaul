package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.client.Death;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.TrackedEntity.class)
public abstract class ChunkMap$TrackedEntityMixin {
    @Inject(method = "removePlayer", at = @At(value = "HEAD"), cancellable = true)
    public void onRemoveTrackedEntity(ServerPlayer pPlayer, CallbackInfo ci) {
        if (!Feature.isEnabled(Death.class)
                || !Death.thirdPerson
                || pPlayer.getRemovalReason() != Entity.RemovalReason.KILLED)
            return;
        ci.cancel();
    }
}
