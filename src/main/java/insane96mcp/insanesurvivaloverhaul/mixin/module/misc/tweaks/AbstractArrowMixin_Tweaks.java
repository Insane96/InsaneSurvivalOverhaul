package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin_Tweaks extends Projectile {
    protected AbstractArrowMixin_Tweaks(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Sends the arrow-hit-player sound event to the shooter when the arrow hits a non-player
     * entity beyond the configured ding distance, providing audio feedback for long-range hits.
     */
    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;doPostHurtEffects(Lnet/minecraft/world/entity/LivingEntity;)V", shift = At.Shift.AFTER))
    public void insanesurvivaloverhaul$onHitEntity(EntityHitResult pResult, CallbackInfo ci) {
        if (!(pResult.getEntity() instanceof Player) && this.getOwner() instanceof ServerPlayer serverPlayer && serverPlayer.distanceToSqr(pResult.getEntity()) >= Tweaks.dingDistance * Tweaks.dingDistance)
            serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
    }
}
