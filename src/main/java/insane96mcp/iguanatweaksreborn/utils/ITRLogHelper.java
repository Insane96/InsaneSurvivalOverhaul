package insane96mcp.iguanatweaksreborn.utils;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalTweaks;

public class ITRLogHelper {
	public static void error(String format, Object... args) {
		InsaneSurvivalTweaks.LOGGER.error(String.format(format, args));
	}

	public static void warn(String format, Object... args) {
		InsaneSurvivalTweaks.LOGGER.warn(String.format(format, args));
	}

	public static void info(String format, Object... args) {
		InsaneSurvivalTweaks.LOGGER.info(String.format(format, args));
	}

	public static void debug(String format, Object... args) {
		InsaneSurvivalTweaks.LOGGER.debug(String.format(format, args));
	}
}
