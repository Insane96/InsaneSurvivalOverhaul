package insane96mcp.insanesurvivaloverhaul.module;

import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSurvivalOverhaul;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ISOModules {
	public static final String COMBAT = InsaneSurvivalOverhaul.MOD_ID + ":combat";

	public static void init(IEventBus eventBus, ModConfigSpec.Builder builder) {
		create(COMBAT, "Combat", eventBus, builder);
	}

	public static void create(String id, String name, IEventBus eventBus, ModConfigSpec.Builder builder) {
		Module.Builder.create(ResourceLocation.parse(id), name, ModConfig.Type.COMMON, builder, eventBus).build();
	}
}
