package insane96mcp.insanesurvivaloverhaul.mixin.module.movement.elytranerf;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.movement.Elytra;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin_ElytraNerf {

	/**
	 * Allows firework rockets to launch the player even when not already gliding,
	 * effectively enabling ground take-off with elytra when
	 * {@link Elytra#fireworkBoostOffGround} is enabled.
	 */
	@ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isFallFlying()Z"))
	private boolean insanesurvivaloverhaul$canTakeOff(boolean original, Level level, Player player, InteractionHand hand) {
		if (!Feature.isEnabled(Elytra.class)
				|| !Elytra.fireworkBoostOffGround)
			return original;
		return !player.onGround();
	}

	/**
	 * Launch the player if
	 * {@link Elytra#fireworkBoostOffGround} is enabled.
	 */
	@WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean insanesurvivaloverhaul$takeOff(Level instance, Entity entity, Operation<Boolean> original, Level level, Player player, InteractionHand hand) {
		boolean ret = original.call(instance, entity);
		if (!Feature.isEnabled(Elytra.class)
				|| !Elytra.fireworkBoostOffGround)
			return ret;
		player.startFallFlying();
		return ret;
	}
}
