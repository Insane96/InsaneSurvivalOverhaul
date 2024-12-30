package insane96mcp.iguanatweaksreborn.module.combat;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;

@Label(name = "Arrow Stats")
@LoadFeature(module = Modules.Ids.COMBAT)
public class ArrowStats extends Feature {
	@Config
	@Label(name = "Disable Critical Arrows bonus damage", description = "If true, Arrows from Bows and Crossbows will no longer deal more damage when fully charged.")
	public static Boolean disableCritArrowsBonusDamage = true;
	@Config
	@Label(name = "Float point damage", description = "If true, arrows will deal float damage instead of being rounded up.")
	public static Boolean floatPointDamage = true;
	@Config(min = 0d, max = 10d)
	@Label(name = "Damage Multiplier", description = "Multiplies arrow's damage by this value. (this doesn't affect mobs arrows)")
	public static Double damageMultiplier = 0.5d;
	@Config(min = 0d, max = 10d)
	@Label(name = "Bow Inaccuracy", description = "Changes bows accuracy. Vanilla is 1.0")
	public static Double bowInaccuracy = 0.5d;
	@Config(min = 0d, max = 10d)
	@Label(name = "Crossbow velocity", description = "Speed at which arrows are shot from crossbows. Vanilla is 3.15")
	public static Double crossbowVelocity = 3.5d;

	public ArrowStats(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

}