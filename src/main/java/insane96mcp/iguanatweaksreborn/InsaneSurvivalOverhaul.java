package insane96mcp.iguanatweaksreborn;

import com.google.common.collect.Lists;
import glitchcore.event.EventManager;
import insane96mcp.iguanatweaksreborn.command.ISOCommand;
import insane96mcp.iguanatweaksreborn.data.criterion.ISOTriggers;
import insane96mcp.iguanatweaksreborn.data.criterion.SeasonChangedTrigger;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.data.generator.ISODamageTypeTagsProvider;
import insane96mcp.iguanatweaksreborn.data.generator.ISOEntityTypeTagsProvider;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.data.generator.client.ISOBlockModelsProvider;
import insane96mcp.iguanatweaksreborn.data.generator.client.ISOBlockStatesProvider;
import insane96mcp.iguanatweaksreborn.data.generator.client.ISOItemModelsProvider;
import insane96mcp.iguanatweaksreborn.modifier.Modifiers;
import insane96mcp.iguanatweaksreborn.module.combat.PiercingDamage;
import insane96mcp.iguanatweaksreborn.module.combat.RegeneratingAbsorption;
import insane96mcp.iguanatweaksreborn.module.combat.UnfairOneShot;
import insane96mcp.iguanatweaksreborn.module.combat.criticalhits.CriticalRework;
import insane96mcp.iguanatweaksreborn.module.experience.anvils.AnvilRepairReloadListener;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.farming.livestock.LivestockDataReloadListener;
import insane96mcp.iguanatweaksreborn.module.farming.plantsgrowth.PlantsGrowthReloadListener;
import insane96mcp.iguanatweaksreborn.module.items.flintexpansion.FlintExpansion;
import insane96mcp.iguanatweaksreborn.module.items.misc.ItemDefinitionsReloadListener;
import insane96mcp.iguanatweaksreborn.module.mining.blockdefinition.BlockDefinitionReloadListener;
import insane96mcp.iguanatweaksreborn.module.mobs.spawning.SeasonSpawning;
import insane96mcp.iguanatweaksreborn.module.mobs.villager.villagers.VillagerTradesReloadListener;
import insane96mcp.iguanatweaksreborn.module.movement.minecarts.Minecarts;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.Cloth;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn.Respawn;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.Tiredness;
import insane96mcp.iguanatweaksreborn.module.world.CyanFlower;
import insane96mcp.iguanatweaksreborn.module.world.seasons.Seasons;
import insane96mcp.iguanatweaksreborn.module.world.spawners.capability.SpawnerData;
import insane96mcp.iguanatweaksreborn.module.world.spawners.capability.SpawnerDataAttacher;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import insane96mcp.iguanatweaksreborn.setup.ISOCommonConfig;
import insane96mcp.iguanatweaksreborn.setup.ISOPackSource;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.iguanatweaksreborn.setup.client.ClientSetup;
import insane96mcp.iguanatweaksreborn.setup.client.ISOClientConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Mod("iguanatweaksreborn")
public class InsaneSurvivalOverhaul
{
    public static final String MOD_ID = "iguanatweaksreborn";
    public static final String NEW_MOD_ID = "insanesurvivaloverhaul";
	public static final String RESOURCE_PREFIX = MOD_ID + ":";
    public static final Logger LOGGER = LogManager.getLogger();

    //TODO
    public static final String CONFIG_FOLDER = "config/" + NEW_MOD_ID;

    public static final ResourceLocation GUI_ICONS = new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "textures/gui/icons.png");

    public InsaneSurvivalOverhaul() {
        //TODO
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ISOClientConfig.CONFIG_SPEC,NEW_MOD_ID + "/client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ISOCommonConfig.CONFIG_SPEC,NEW_MOD_ID + "/common.toml");
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(SpawnerDataAttacher.class);
        if (ModList.get().isLoaded("sereneseasons"))
            MinecraftForge.EVENT_BUS.register(SeasonChangedTrigger.class);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::addPackFinders);
        modEventBus.addListener(PiercingDamage::addAttribute);
        modEventBus.addListener(RegeneratingAbsorption::addAttribute);
        modEventBus.addListener(CriticalRework::addAttribute);
		modEventBus.register(SpawnerData.class);
        modEventBus.register(Tiredness.class);
        modEventBus.register(UnfairOneShot.class);
        modEventBus.register(RegeneratingAbsorption.class);

        if (ModList.get().isLoaded("sereneseasons")) {
            EventManager.addListener(SeasonSpawning::onSeasonChanged);
            EventManager.addListener(Seasons::onSeasonChanged);
            EventManager.addListener(SeasonChangedTrigger::onSeasonChanged);
        }
        ISORegistries.REGISTRIES.forEach(register -> register.register(modEventBus));

        ISOTriggers.init();

        if (ModList.get().isLoaded("shieldsplus"))
            FlintExpansion.ShieldsPlusIntegration.init();

        if (FMLLoader.getDist().isClient()) {
            modEventBus.addListener(ClientSetup::onBuildCreativeModeTabContents);
            modEventBus.addListener(ClientSetup::registerEntityRenderers);
            //modEventBus.addListener(ClientSetup::registerRecipeBookCategories);
            //modEventBus.addListener(ClientSetup::registerParticleFactories);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(AnvilRepairReloadListener.INSTANCE);
        event.addListener(ItemDefinitionsReloadListener.INSTANCE);
        event.addListener(BlockDefinitionReloadListener.INSTANCE);
        event.addListener(PlantsGrowthReloadListener.INSTANCE);
        event.addListener(LivestockDataReloadListener.INSTANCE);
        event.addListener(VillagerTradesReloadListener.INSTANCE);
    }

    @SubscribeEvent
    public void remapFromITE(MissingMappingsEvent event) {
        event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:respawn_obelisk"))
                .forEach(mapping -> mapping.remap(Respawn.RESPAWN_OBELISK.item().get()));
        event.getMappings(ForgeRegistries.Keys.BLOCKS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:respawn_obelisk"))
                .forEach(mapping -> mapping.remap(Respawn.RESPAWN_OBELISK.block().get()));

        event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:golden_powered_rail"))
                .forEach(mapping -> mapping.remap(Minecarts.GOLDEN_POWERED_RAIL.item().get()));
        event.getMappings(ForgeRegistries.Keys.BLOCKS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:golden_powered_rail"))
                .forEach(mapping -> mapping.remap(Minecarts.GOLDEN_POWERED_RAIL.block().get()));
        event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:copper_powered_rail"))
                .forEach(mapping -> mapping.remap(Minecarts.COPPER_POWERED_RAIL.item().get()));
        event.getMappings(ForgeRegistries.Keys.BLOCKS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:copper_powered_rail"))
                .forEach(mapping -> mapping.remap(Minecarts.COPPER_POWERED_RAIL.block().get()));

        event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:cloth"))
                .forEach(mapping -> mapping.remap(Cloth.ITEM.get()));

        event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:flint_axe"))
                .forEach(mapping -> mapping.remap(FlintExpansion.AXE.get()));
        event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:flint_pickaxe"))
                .forEach(mapping -> mapping.remap(FlintExpansion.PICKAXE.get()));
        event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:flint_shovel"))
                .forEach(mapping -> mapping.remap(FlintExpansion.SHOVEL.get()));
        event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:flint_sword"))
                .forEach(mapping -> mapping.remap(FlintExpansion.SWORD.get()));
        event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:flint_hoe"))
                .forEach(mapping -> mapping.remap(FlintExpansion.HOE.get()));
        if (ModList.get().isLoaded("shieldsplus"))
            event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
                    .filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:flint_shield"))
                    .forEach(mapping -> mapping.remap(FlintExpansion.ShieldsPlusIntegration.SHIELD.get()));
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        ISOCommand.register(event.getDispatcher());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.init();
        Modifiers.init();

        event.enqueueWork(() -> {
            ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "cyan_flower"), CyanFlower.POTTED_FLOWER);
            ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "solanum_neorossii"), Crops.POTTED_SOLANUM_NEOROSSII);
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientSetup.init(event);
    }

    public void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        /*generator.addProvider(event.includeServer(), new SRRecipeProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new SRGlobalLootModifierProvider(generator.getPackOutput(), IguanaTweaksReborn.MOD_ID));*/
        ISOBlockTagsProvider blockTags = new ISOBlockTagsProvider(generator.getPackOutput(), lookupProvider, InsaneSurvivalOverhaul.MOD_ID, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ISOItemTagsProvider(generator.getPackOutput(), lookupProvider, blockTags.contentsGetter(), InsaneSurvivalOverhaul.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeServer(), new ISODamageTypeTagsProvider(generator.getPackOutput(), lookupProvider, InsaneSurvivalOverhaul.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeServer(), new ISOEntityTypeTagsProvider(generator.getPackOutput(), lookupProvider, InsaneSurvivalOverhaul.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeClient(), new ISOBlockStatesProvider(generator.getPackOutput(), InsaneSurvivalOverhaul.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeClient(), new ISOBlockModelsProvider(generator.getPackOutput(), InsaneSurvivalOverhaul.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeClient(), new ISOItemModelsProvider(generator.getPackOutput(), InsaneSurvivalOverhaul.MOD_ID, existingFileHelper));
    }

    public void addPackFinders(AddPackFindersEvent event)
    {
        for (IntegratedPack dataPack : IntegratedPack.INTEGRATED_PACKS) {
            if (event.getPackType() != dataPack.getPackType())
                continue;
            if (!dataPack.shouldBeEnabled())
                continue;

            Path resourcePath = ModList.get().getModFileById(MOD_ID).getFile().findResource("integrated_packs/" + dataPack.getPath());
            var pack = Pack.readMetaAndCreate(InsaneSurvivalOverhaul.RESOURCE_PREFIX + dataPack.getPath(), dataPack.getDescription(), dataPack.shouldBeEnabled(),
                    (path) -> new PathPackResources(path, resourcePath, false), PackType.SERVER_DATA, Pack.Position.TOP, dataPack.shouldBeEnabled() ? PackSource.DEFAULT : ISOPackSource.DISABLED);
            event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
        }
    }

    //Reload the data packs to disable the ones that have been disabled
    @SubscribeEvent
    public void onServerStartedEvent(ServerStartedEvent event)
    {
        boolean hasDisabledPack = false;
        PackRepository packRepository = event.getServer().getPackRepository();
        List<Pack> list = Lists.newArrayList(packRepository.getSelectedPacks());
        for (IntegratedPack dataPack : IntegratedPack.INTEGRATED_PACKS) {
            String dataPackId = InsaneSurvivalOverhaul.RESOURCE_PREFIX + dataPack.getPath();
            Pack pack = packRepository.getPack(dataPackId);
            if (pack != null && !dataPack.shouldBeEnabled()) {
                list.remove(pack);
                hasDisabledPack = true;
            }
        }
        if (hasDisabledPack)
            event.getServer().reloadResources(list.stream().map(Pack::getId).collect(Collectors.toList()));
    }

}
