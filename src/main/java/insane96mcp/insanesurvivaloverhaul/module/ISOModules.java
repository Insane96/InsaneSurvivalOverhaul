package insane96mcp.insanesurvivaloverhaul.module;

import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ISOModules {
	public static final String COMBAT = InsaneSO.MOD_ID + ":combat";
	public static final String MOBS = InsaneSO.MOD_ID + ":mobs";

	public static void init(IEventBus eventBus, ModConfigSpec.Builder builder) {
		create(COMBAT, "Combat", eventBus, builder);
		create(MOBS, "Mobs", eventBus, builder);
	}

	public static void create(String id, String name, IEventBus eventBus, ModConfigSpec.Builder builder) {
		Module.Builder.create(ResourceLocation.parse(id), name, ModConfig.Type.COMMON, builder, eventBus).build();
	}
}
