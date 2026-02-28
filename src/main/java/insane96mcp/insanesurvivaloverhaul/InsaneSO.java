package insane96mcp.insanesurvivaloverhaul;

import com.mojang.logging.LogUtils;
import insane96mcp.insanelib.setup.ILModConfig;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOBlockTagsProvider;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISODamageTypeTagsProvider;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOEntityTypeTagsProvider;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.combat.CriticalRework;
import insane96mcp.insanesurvivaloverhaul.module.combat.RegeneratingAbsorption;
import insane96mcp.insanesurvivaloverhaul.network.NetworkHandler;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(InsaneSO.MOD_ID)
public class InsaneSO {
    public static final String MOD_ID = "insanesurvivaloverhaul";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ILModConfig CONFIG;
    public static final String CONFIG_FOLDER = "config/" + MOD_ID;

    public InsaneSO(IEventBus eventBus, ModContainer modContainer) {
        CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.COMMON, eventBus, ISOModules::init, InsaneSO.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec, MOD_ID + "/common.toml");

        ISORegistries.REGISTRIES.forEach(r -> r.register(eventBus));

        eventBus.addListener(InsaneSO::gatherData);
        eventBus.addListener(NetworkHandler::register);

        eventBus.addListener(CriticalRework::addAttribute);
        eventBus.addListener(RegeneratingAbsorption::addAttribute);
        eventBus.addListener(RegeneratingAbsorption::registerGuiOverlays);
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        ISOBlockTagsProvider blockTagsProvider = new ISOBlockTagsProvider(output, lookupProvider, MOD_ID, existingFileHelper);
        event.getGenerator().addProvider(
                event.includeServer(),
                blockTagsProvider
        );
        event.getGenerator().addProvider(
                event.includeServer(),
                new ISODamageTypeTagsProvider(output, lookupProvider, MOD_ID, existingFileHelper)
        );
        event.getGenerator().addProvider(
                event.includeServer(),
                new ISOEntityTypeTagsProvider(output, lookupProvider, MOD_ID, existingFileHelper)
        );
        event.getGenerator().addProvider(
                event.includeServer(),
                new ISOItemTagsProvider(output, lookupProvider, blockTagsProvider.contentsGetter(), MOD_ID, existingFileHelper)
        );
    }

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String lang(String path) {
        return MOD_ID + "." + path;
    }
}
