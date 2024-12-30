package insane96mcp.iguanatweaksreborn.module.experience;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.utils.ITRLogHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Player Experience", description = "Changes the experience lost on death and xp per level required.")
@LoadFeature(module = Modules.Ids.EXPERIENCE)
public class PlayerExperience extends Feature {

	@Config
	@Label(name = "Level Scaling Formula", description = """
						The experience required to level up is calculated by this formula. The variable available is 'current_level'
						Set to empty to disable.
						This is evaluated with EvalEx https://ezylang.github.io/EvalEx/concepts/parsing_evaluation.html.
						Incompatible with Allurement's 'Remove level Scaling' or any other mod that does something similar""")
	public static String levelScalingFormula = "35";//"(2 * current_level) + 5";
	@Config(min = -1d, max = 1d)
	@Label(name = "Dropped Experience on Death", description = """
						On death, players will drop this percentage of experience instead of max 7 levels. Setting to -1 will disable this.
						Due to Minecraft limitations this is incompatible with other mods that change the level scaling (e.g. Allurement's 'Remove level Scaling').""")
	public static Double droppedExperienceOnDeath = 0.8d;
	@Config(min = -1d, max = 1d)
	@Label(name = "Pickup XP Faster", description = "Players will pick up experience faster")
	public static Boolean pickUpFaster = true;

	public PlayerExperience(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (!this.isEnabled()
				|| event.phase != TickEvent.Phase.START
				|| !pickUpFaster)
			return;

		if (event.player.takeXpDelay > 0)
			event.player.takeXpDelay--;
	}

	/**
	 * Returns -1 when the module/feature is not enabled, otherwise the calculated experience required to level up
	 */
	public static int getBetterScalingLevel(int currentLevel) {
		if (!isEnabled(PlayerExperience.class)
				|| levelScalingFormula.isEmpty())
			return -1;

		Expression expression = new Expression(levelScalingFormula);
		try {
			//noinspection ConstantConditions
			EvaluationValue result = expression
					.with("current_level", currentLevel)
					.evaluate();
			return result.getNumberValue().intValue();
		}
		catch (Exception ex) {
			ITRLogHelper.error("Failed to evaluate or parse eating speed formula: %s", expression);
			return vanillaXpNeededForNextLevel(currentLevel);
		}
	}

	//Instead of using experienceTotal, calculate the xp from the xp bar and level since experienceTotal doesn't get updated on level consume
	private static int getTotalExperience(Player player, boolean ignoreGlobalXPMultiplier) {
		int totalExp = 0;
		for (int i = 0; i < player.experienceLevel; i++) {
			totalExp += getXpNeededForNextLevel(i);
		}
		totalExp += (int) (player.getXpNeededForNextLevel() * player.experienceProgress);
		//Take into account global experience to prevent XP duping
		if (!ignoreGlobalXPMultiplier && Feature.isEnabled(Experience.class) && Experience.globalMultiplier != 1d)
			totalExp *= (int) (1d / Experience.globalMultiplier);
		//Cap to 250k XP
		if (totalExp > 250000)
			totalExp = 250000;
		return totalExp;
	}

	private static int getXpNeededForNextLevel(int currentLevel) {
		int betterScalingXp = getBetterScalingLevel(currentLevel);
		if (betterScalingXp != -1)
			return betterScalingXp;
		return vanillaXpNeededForNextLevel(currentLevel);
	}

	private static int vanillaXpNeededForNextLevel(int currentLevel) {
		if (currentLevel >= 30) {
			return 112 + (currentLevel - 30) * 9;
		} else {
			return currentLevel >= 15 ? 37 + (currentLevel - 15) * 5 : 7 + currentLevel * 2;
		}
	}

	/**
	 * Returns -1 when the module/feature is not enabled, otherwise the experience dropped on death
	 */
	public static int getExperienceOnDeath(Player player, boolean ignoreGlobalXPMultiplier) {
		if (!isEnabled(PlayerExperience.class)
				|| (droppedExperienceOnDeath < 0 && !Experience.isEnabled(Experience.class))
				|| player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
				|| player.isSpectator())
			return -1;

		return (int) (getTotalExperience(player, ignoreGlobalXPMultiplier) * droppedExperienceOnDeath);
	}
}