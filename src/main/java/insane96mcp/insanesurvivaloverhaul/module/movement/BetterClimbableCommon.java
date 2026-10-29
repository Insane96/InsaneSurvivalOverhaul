package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.neoforged.fml.config.ModConfig;

@ConfigType(ModConfig.Type.COMMON)
@LoadFeature(module = ISOModules.MOVEMENT, description = "Data packs of Better Climbable.")
public class BetterClimbableCommon extends Feature {
	@Config(description = "If true, a data pack will be enabled that will make players take damage when falling on climbable blocks.")
	public static Boolean fallDamageOnClimbable = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("fall_damage_on_climbable", "Insane's Survival Overhaul Fall damage on climbable", BetterClimbableCommon::fallDamageOnClimbable);
	}

	/**
	 * If the fall damage on climbable data pack is enabled
	 */
	public static boolean fallDamageOnClimbable() {
		return !Packs.disableAllDataPacks && isEnabled(BetterClimbableCommon.class) && fallDamageOnClimbable;
	}
}
