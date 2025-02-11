package insane96mcp.iguanatweaksreborn.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.client.Misc;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VanillaGuiOverlay.class)
public abstract class VanillaGuiOverlayMixin {
	@Definition(id = "playerRideableJumping", local = @Local(type = PlayerRideableJumping.class))
	@Expression("playerRideableJumping != null")
	@WrapOperation(method = "lambda$static$13", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
	private static boolean iguanatweaksreborn$onRenderJumpBar(Object left, Object right, Operation<Boolean> original, ForgeGui gui) {
		if (!Misc.fixMountsGui())
			return original.call(left, right);
		//noinspection DataFlowIssue
		return original.call(left, right) && gui.getMinecraft().player.getJumpRidingScale() > 0;
	}

	@Definition(id = "gui", local = @Local(type = ForgeGui.class, argsOnly = true))
	@Definition(id = "getMinecraft", method = "Lnet/minecraftforge/client/gui/overlay/ForgeGui;getMinecraft()Lnet/minecraft/client/Minecraft;")
	@Definition(id = "player", field = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;")
	@Definition(id = "jumpableVehicle", method = "Lnet/minecraft/client/player/LocalPlayer;jumpableVehicle()Lnet/minecraft/world/entity/PlayerRideableJumping;")
	@Expression("gui.getMinecraft().player.jumpableVehicle() == null")
	@WrapOperation(method = "lambda$static$14", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
	private static boolean iguanatweaksreborn$onRenderExperienceBar(Object left, Object right, Operation<Boolean> original, ForgeGui gui) {
		if (!Misc.fixMountsGui())
			return original.call(left, right);
		//noinspection DataFlowIssue
		return original.call(left, right) || gui.getMinecraft().player.getJumpRidingScale() == 0;
	}
}
