package insane96mcp.iguanatweaksreborn.mixin.integration.tinkersconstruct;

import insane96mcp.iguanatweaksreborn.module.misc.TinkerIntegration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;

@Mixin(HeadMaterialStats.class)
public class HeadMaterialStatsMixin {
	@ModifyArg(method = "apply", at = @At(value = "INVOKE", target = "Lslimeknights/tconstruct/library/tools/stat/FloatToolStat;update(Lslimeknights/tconstruct/library/tools/stat/ModifierStatsBuilder;Ljava/lang/Float;)V", ordinal = 0), index = 1, remap = false)
	private Float iguanatweaksreborn$durabilityMultiplier(Float value) {
		return Math.max(1f, value * TinkerIntegration.damageModifier());
	}
	@ModifyArg(method = "apply", at = @At(value = "INVOKE", target = "Lslimeknights/tconstruct/library/tools/stat/FloatToolStat;update(Lslimeknights/tconstruct/library/tools/stat/ModifierStatsBuilder;Ljava/lang/Float;)V", ordinal = 1), index = 1, remap = false)
	private Float iguanatweaksreborn$damageMultiplier(Float value) {
		return Math.max(1f, value * TinkerIntegration.damageModifier());
	}
	@ModifyArg(method = "apply", at = @At(value = "INVOKE", target = "Lslimeknights/tconstruct/library/tools/stat/FloatToolStat;update(Lslimeknights/tconstruct/library/tools/stat/ModifierStatsBuilder;Ljava/lang/Float;)V", ordinal = 2), index = 1, remap = false)
	private Float iguanatweaksreborn$miningSpeedMultiplier(Float value) {
		return Math.max(1f, value * TinkerIntegration.miningSpeedModifier());
	}
}
