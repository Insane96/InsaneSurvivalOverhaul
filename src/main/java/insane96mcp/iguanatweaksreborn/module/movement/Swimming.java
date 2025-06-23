package insane96mcp.iguanatweaksreborn.module.movement;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;

@LoadFeature(module = Modules.Ids.MOVEMENT)
public class Swimming extends Feature {

	@Config(description = "Prevents swimming up really fast if swimming and holding the jump key.")
	public static Boolean preventFastSwimUpWithJump = true;

	public Swimming(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static boolean shouldPreventFastSwimUpWithJump() {
		return Feature.isEnabled(Swimming.class) && preventFastSwimUpWithJump;
	}
}