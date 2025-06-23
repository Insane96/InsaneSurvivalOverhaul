package insane96mcp.iguanatweaksreborn.module.movement;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;

@LoadFeature(module = Modules.Ids.MOVEMENT, description = "Makes flying with elytra outside the end dimension have stronger \"air resistance\"")
public class ElytraNerf extends Feature {

	@Config(min = 0d, max = 1d, description = "How much the player is slowed down when gliding.")
	public static Double airResistance = 0.975d;

	public ElytraNerf(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}
}