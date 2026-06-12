package insane96mcp.insanesurvivaloverhaul;

import com.mojang.logging.LogUtils;
import insane96mcp.insanelib.setup.ILModConfig;
import insane96mcp.insanelib.util.IntegratedPack;
import insane96mcp.insanesurvivaloverhaul.command.ISOCommand;
import insane96mcp.insanesurvivaloverhaul.data.generator.*;
import insane96mcp.insanesurvivaloverhaul.data.generator.client.ISOBlockStatesProvider;
import insane96mcp.insanesurvivaloverhaul.data.generator.client.ISOItemModelsProvider;
import insane96mcp.insanesurvivaloverhaul.data.modifier.Modifiers;
import insane96mcp.insanesurvivaloverhaul.module.ISOClientModules;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.client.hudinfos.HudInfos;
import insane96mcp.insanesurvivaloverhaul.module.combat.CriticalRework;
import insane96mcp.insanesurvivaloverhaul.module.combat.PiercingDamage;
import insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption.RegeneratingAbsorption;
import insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption.RegeneratingAbsorptionClient;
import insane96mcp.insanesurvivaloverhaul.module.combat.unfaironeshot.UnfairOneShotClient;
import insane96mcp.insanesurvivaloverhaul.module.farming.crops.Crops;
import insane96mcp.insanesurvivaloverhaul.module.farming.livestock.LivestockDataReloadListener;
import insane96mcp.insanesurvivaloverhaul.module.farming.plantsgrowth.PlantsGrowthReloadListener;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.HungerAndHealthRegen;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.exhaustion.Exhaustion;
import insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting.AnvilRecipeReloadListener;
import insane96mcp.insanesurvivaloverhaul.module.mining.blockdefinition.BlockDefinitionReloadListener;
import insane96mcp.insanesurvivaloverhaul.module.world.CyanFlower;
import insane96mcp.insanesurvivaloverhaul.setup.ClientSetup;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.NetworkHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

@Mod(InsaneSO.MOD_ID)
public class InsaneSO {
    public static final String MOD_ID = "insanesurvivaloverhaul";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ILModConfig CONFIG;
    public static ILModConfig CLIENT_CONFIG;
    public static final String CONFIG_FOLDER = "config/" + MOD_ID;

    public InsaneSO(IEventBus eventBus, ModContainer modContainer) {
        CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.COMMON, eventBus, ISOModules::init, InsaneSO.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec, MOD_ID + "/common.toml");
        CLIENT_CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.CLIENT, eventBus, ISOClientModules::init, InsaneSO.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG.spec, MOD_ID + "/client.toml");

        ISORegistries.REGISTRIES.forEach(r -> r.register(eventBus));

        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::commonSetup);

        eventBus.addListener(InsaneSO::gatherData);
        eventBus.addListener(NetworkHandler::register);

        eventBus.addListener(CriticalRework::addAttribute);
        eventBus.addListener(RegeneratingAbsorption::addAttribute);
        eventBus.addListener(PiercingDamage::addAttribute);
        eventBus.addListener(HungerAndHealthRegen::addAttribute);
        eventBus.addListener(Exhaustion::addAttribute);

        if (FMLLoader.getDist().isClient()) {
            eventBus.addListener(RegeneratingAbsorptionClient::registerGuiOverlays);
            eventBus.addListener(UnfairOneShotClient::registerGuiLayers);
            eventBus.addListener(ClientSetup::onBuildCreativeModeTabContents);
            eventBus.addListener(ClientSetup::registerTooltips);
            eventBus.addListener(HudInfos::registerGuiLayers);
        }

        NeoForgeMod.enableMergedAttributeTooltips();
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(ISOCommand::register);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        Modifiers.init();

        event.enqueueWork(() -> {
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(id("cyan_flower"), CyanFlower.POTTED_FLOWER);
            ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(id("solanum_neorossii"), Crops.POTTED_SOLANUM_NEOROSSII);

            //DispenserBlock.registerBehavior(Fletching.QUARTZ_ARROW_ITEM.get(), new ISOArrowDispenseBehaviour());
            //DispenserBlock.registerBehavior(Fletching.DIAMOND_ARROW_ITEM.get(), new ISOArrowDispenseBehaviour());
            //DispenserBlock.registerBehavior(Fletching.EXPLOSIVE_ARROW_ITEM.get(), new ISOArrowDispenseBehaviour());
            //DispenserBlock.registerBehavior(Fletching.TORCH_ARROW_ITEM.get(), new ISOArrowDispenseBehaviour());
            //DispenserBlock.registerBehavior(Fletching.ICE_ARROW_ITEM.get(), new ISOArrowDispenseBehaviour());
        });
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(BlockDefinitionReloadListener.INSTANCE);
        event.addListener(LivestockDataReloadListener.INSTANCE);
        event.addListener(PlantsGrowthReloadListener.INSTANCE);
        event.addListener(AnvilRecipeReloadListener.INSTANCE);
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
        event.getGenerator().addProvider(
                event.includeServer(),
                new ISORecipeProvider(output, lookupProvider)
        );
        event.getGenerator().addProvider(
                event.includeServer(),
                new ISOLootTableProvider(output, lookupProvider)
        );
        event.getGenerator().addProvider(
                event.includeServer(),
                new ISOAdvancementProvider(output, lookupProvider)
        );
        event.getGenerator().addProvider(
                event.includeClient(),
                new ISOBlockStatesProvider(output, MOD_ID, existingFileHelper)
        );
        event.getGenerator().addProvider(
                event.includeClient(),
                new ISOItemModelsProvider(output, MOD_ID, existingFileHelper)
        );
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientSetup.init(event);
    }

    public static void addServerPack(String path, String description, BooleanSupplier enabled) {
        IntegratedPack.addServerPack(MOD_ID, path, description, enabled);
    }

    public static void addServerPack(int priority, String path, String description, BooleanSupplier enabled) {
        IntegratedPack.addServerPack(priority, MOD_ID, path, description, enabled);
    }

    public static void addClientPack(String path, String description, BooleanSupplier enabled) {
        IntegratedPack.addClientPack(MOD_ID, path, description, enabled);
    }

    public static void addClientPack(int priority, String path, String description, BooleanSupplier enabled) {
        IntegratedPack.addClientPack(priority, MOD_ID, path, description, enabled);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String lang(String path) {
        return MOD_ID + "." + path;
    }

    public static MutableComponent translatableLang(String path) {
        return Component.translatable(lang(path));
    }
}
