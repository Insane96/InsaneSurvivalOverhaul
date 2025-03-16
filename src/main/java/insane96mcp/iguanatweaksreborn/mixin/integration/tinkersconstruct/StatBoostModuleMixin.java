package insane96mcp.iguanatweaksreborn.mixin.integration.tinkersconstruct;

import insane96mcp.iguanatweaksreborn.module.mining.blockhardness.BlockHardness;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import slimeknights.tconstruct.library.modifiers.modules.build.StatBoostModule;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

@Mixin(StatBoostModule.class)
public class StatBoostModuleMixin {
	@Shadow @Final private StatBoostModule.StatOperation operation;

	@Shadow @Final private INumericToolStat<?> stat;

	@ModifyArg(method = "addToolStats", at = @At(value = "INVOKE", target = "Lslimeknights/tconstruct/library/modifiers/modules/build/StatBoostModule$StatOperation;apply(Lslimeknights/tconstruct/library/tools/stat/ModifierStatsBuilder;Lslimeknights/tconstruct/library/tools/stat/INumericToolStat;F)V"), index = 2, remap = false)
	private float iguanatweaksreborn$miningSpeedMultiplier(float value) {
		if (operation != StatBoostModule.StatOperation.ADD)
			return value;
		if (this.stat != ToolStats.MINING_SPEED)
			return value * BlockHardness.tiConMiningSpeedModifier.floatValue();
		if (this.stat != ToolStats.ATTACK_DAMAGE)
			return value * BlockHardness.tiConMiningSpeedModifier.floatValue();
		return value;
	}
}
