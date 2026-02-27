package insane96mcp.insanesurvivaloverhaul;

import com.mojang.logging.LogUtils;
import insane96mcp.insanelib.setup.ILModConfig;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.combat.CriticalRework;
import insane96mcp.insanesurvivaloverhaul.network.NetworkHandler;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(InsaneSurvivalOverhaul.MOD_ID)
public class InsaneSurvivalOverhaul {
    public static final String MOD_ID = "insanesurvivaloverhaul";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ILModConfig CONFIG;

    public InsaneSurvivalOverhaul(IEventBus eventBus, ModContainer modContainer) {
        CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.COMMON, eventBus, ISOModules::init, InsaneSurvivalOverhaul.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec);

        ISORegistries.REGISTRIES.forEach(r -> r.register(eventBus));

        eventBus.addListener(NetworkHandler::register);

        eventBus.addListener(CriticalRework::addAttribute);
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
