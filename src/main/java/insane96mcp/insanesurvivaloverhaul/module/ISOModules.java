package insane96mcp.insanesurvivaloverhaul.module;

import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ISOModules {
	public static final String COMBAT = InsaneSO.MOD_ID + ":combat";
	public static final String FARMING = InsaneSO.MOD_ID + ":farming";
	public static final String HUNGER_HEALTH = InsaneSO.MOD_ID + ":hunger_health";
	public static final String ITEMS = InsaneSO.MOD_ID + ":items";
	public static final String MINING = InsaneSO.MOD_ID + ":mining";
	public static final String MISC = InsaneSO.MOD_ID + ":misc";
	public static final String MOBS = InsaneSO.MOD_ID + ":mobs";
	public static final String MOVEMENT = InsaneSO.MOD_ID + ":movement";
	public static final String SLEEP = InsaneSO.MOD_ID + ":sleep";
	public static final String WORLD = InsaneSO.MOD_ID + ":world";

	public static void init(IEventBus eventBus, ModConfigSpec.Builder builder) {
		create(COMBAT, "Combat", eventBus, builder);
		create(FARMING, "Farming", eventBus, builder);
		create(HUNGER_HEALTH, "Hunger & Health", eventBus, builder);
		create(ITEMS, "Items", eventBus, builder);
		create(MINING, "Mining", eventBus, builder);
		create(MISC, "Misc", eventBus, builder);
		create(MOBS, "Mobs", eventBus, builder);
		create(MOVEMENT, "Movement", eventBus, builder);
		create(SLEEP, "Sleep", eventBus, builder);
		create(WORLD, "World", eventBus, builder);
	}

	public static void create(String id, String name, IEventBus eventBus, ModConfigSpec.Builder builder) {
		Module.Builder.create(ResourceLocation.parse(id), name, ModConfig.Type.COMMON, builder, eventBus).build();
	}
}
