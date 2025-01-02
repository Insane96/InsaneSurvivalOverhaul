package insane96mcp.iguanatweaksreborn.module;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.setup.ITRCommonConfig;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.fml.config.ModConfig;

public class Modules {

	public static Module combat;
	public static Module experience;
	public static Module farming;
	public static Module hungerHealth;
	public static Module items;
	public static Module mining;
	public static Module misc;
	public static Module mobs;
	public static Module movement;
	public static Module sleepRespawn;
	public static Module world;

	public static void init() {
		combat = Module.Builder.create(Ids.COMBAT, "Combat", ModConfig.Type.COMMON, ITRCommonConfig.builder).build();
		experience = Module.Builder.create(Ids.EXPERIENCE, "Experience", ModConfig.Type.COMMON, ITRCommonConfig.builder).build();
		farming = Module.Builder.create(Ids.FARMING, "Farming", ModConfig.Type.COMMON, ITRCommonConfig.builder).build();
		hungerHealth = Module.Builder.create(Ids.HUNGER_HEALTH, "Hunger & Health", ModConfig.Type.COMMON, ITRCommonConfig.builder).build();
		hungerHealth = Module.Builder.create(Ids.ITEMS, "Items", ModConfig.Type.COMMON, ITRCommonConfig.builder).build();
		mining = Module.Builder.create(Ids.MINING, "Mining", ModConfig.Type.COMMON, ITRCommonConfig.builder).build();
		misc = Module.Builder.create(Ids.MISC, "Miscellaneous", ModConfig.Type.COMMON, ITRCommonConfig.builder).build();
		mobs = Module.Builder.create(Ids.MOBS, "Mobs", ModConfig.Type.COMMON, ITRCommonConfig.builder).build();
		movement = Module.Builder.create(Ids.MOVEMENT, "Movement", ModConfig.Type.COMMON, ITRCommonConfig.builder).build();
		sleepRespawn = Module.Builder.create(Ids.SLEEP_RESPAWN, "Sleep & Respawn", ModConfig.Type.COMMON, ITRCommonConfig.builder).build();
		world = Module.Builder.create(Ids.WORLD, "World", ModConfig.Type.COMMON, ITRCommonConfig.builder).build();
	}

	public static class Ids {
		public static final String COMBAT = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "combat";
		public static final String EXPERIENCE = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "experience";
		public static final String FARMING = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "farming";
		public static final String HUNGER_HEALTH = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "hunger_health";
		public static final String ITEMS = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "items";
		public static final String MINING = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "mining";
		public static final String MISC = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "misc";
		public static final String MOBS = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "mobs";
		public static final String MOVEMENT = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "movement";
		public static final String SLEEP_RESPAWN = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "sleep_respawn";
		public static final String WORLD = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "world";
	}
}
