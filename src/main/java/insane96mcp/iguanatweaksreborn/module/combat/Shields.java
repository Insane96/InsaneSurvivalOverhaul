package insane96mcp.iguanatweaksreborn.module.combat;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraftforge.fml.ModList;

@Label(name = "Shields", description = "This feature disables itself if Shields+ is installed")
@LoadFeature(module = Modules.Ids.COMBAT)
public class Shields extends Feature {
	@Config(min = 0)
	@Label(description = "In vanilla when you start blocking with a shield, there's a 0.25 seconds (5 ticks) window where you are still not blocking. By default the windup is removed.")
	public static Integer shieldWindup = 0;
	@Config(min = 0d, max = Float.MAX_VALUE)
	@Label(description = "The minimum damage dealt to the player for the shield to take damage. Vanilla is 3. E.g. With this set to 3, the shield will not be damaged if damage received is lower than.")
	public static Double minShieldHurtDamage = 0d;

	public Shields(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && !ModList.get().isLoaded("shieldsplus");
	}

	public static int getShieldWindUp(int original) {
		return isEnabled(Shields.class) ? shieldWindup : original;
	}

	public static float getMinHurtDamage(float original) {
		return isEnabled(Shields.class) ? minShieldHurtDamage.floatValue() : original;
	}

}