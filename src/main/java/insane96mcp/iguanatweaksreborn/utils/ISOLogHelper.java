package insane96mcp.iguanatweaksreborn.utils;

import insane96mcp.iguanatweaksreborn.InsaneSO;

public class ISOLogHelper {
	public static void error(String format, Object... args) {
		InsaneSO.LOGGER.error(String.format(format, args));
	}

	public static void warn(String format, Object... args) {
		InsaneSO.LOGGER.warn(String.format(format, args));
	}

	public static void info(String format, Object... args) {
		InsaneSO.LOGGER.info(String.format(format, args));
	}

	public static void debug(String format, Object... args) {
		InsaneSO.LOGGER.debug(String.format(format, args));
	}
}
