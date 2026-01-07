package insane96mcp.iguanatweaksreborn.mixin.integration.tinkersconstruct;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.iguanatweaksreborn.module.combat.PlayerStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;

@Mixin(ToolAttackUtil.class)
public class ToolAttackUtilMixin {
	@ModifyExpressionValue(method = "performAttack", at = @At(value = "CONSTANT", args = "floatValue=0.2"), remap = false)
	private static float iguanatweaksreborn$noDamageWhenSpamming(float value) {
		return PlayerStats.noDamageWhenSpamming() ? 0f : value;
	}

	@ModifyExpressionValue(method = "performAttack", at = @At(value = "CONSTANT", args = "floatValue=0.8"), remap = false)
	private static float iguanatweaksreborn$noDamageWhenSpamming2(float value) {
		return PlayerStats.noDamageWhenSpamming() ? 1f : value;
	}
}
