package insane96mcp.iguanatweaksreborn;

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
import insane96mcp.iguanatweaksreborn.module.experience.DroppedExperience;
import insane96mcp.iguanatweaksreborn.module.experience.anvils.AnvilRepairReloadListener;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.farming.livestock.Livestock;
import insane96mcp.iguanatweaksreborn.module.farming.livestock.LivestockDataReloadListener;
import insane96mcp.iguanatweaksreborn.module.farming.plantsgrowth.PlantsGrowthReloadListener;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.healthregen.HealthRegenHunger;
import insane96mcp.iguanatweaksreborn.module.items.flintexpansion.FlintExpansion;
import insane96mcp.iguanatweaksreborn.module.items.misc.ItemDefinitionsReloadListener;
import insane96mcp.iguanatweaksreborn.module.mining.blockdefinition.BlockDefinitionReloadListener;
import insane96mcp.iguanatweaksreborn.module.mobs.spawning.SeasonSpawning;
import insane96mcp.iguanatweaksreborn.module.mobs.villager.villagers.VillagerTradesReloadListener;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.Tiredness;
import insane96mcp.iguanatweaksreborn.module.world.CyanFlower;
import insane96mcp.iguanatweaksreborn.module.world.seasons.Seasons;
import insane96mcp.iguanatweaksreborn.module.world.spawners.capability.SpawnerData;
import insane96mcp.iguanatweaksreborn.module.world.spawners.capability.SpawnerDataAttacher;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import insane96mcp.iguanatweaksreborn.setup.ISOCommonConfig;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.setup.client.ClientSetup;
import insane96mcp.iguanatweaksreborn.setup.client.ISOClientConfig;
import insane96mcp.insanelib.util.IntegratedPack;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

@Mod("iguanatweaksreborn")
public class InsaneSO
{
    public static final String MOD_ID = "iguanatweaksreborn";
    public static final String NEW_MOD_ID = "insanesurvivaloverhaul";
    @Deprecated(forRemoval = true)
	public static final String RESOURCE_PREFIX = MOD_ID + ":";
    public static final Logger LOGGER = LogManager.getLogger();

    //TODO ISO
    public static final String CONFIG_FOLDER = "config/" + NEW_MOD_ID;

    public InsaneSO(FMLJavaModLoadingContext context) {
        //TODO ISO
        context.registerConfig(ModConfig.Type.CLIENT, ISOClientConfig.CONFIG_SPEC,NEW_MOD_ID + "/client.toml");
        context.registerConfig(ModConfig.Type.COMMON, ISOCommonConfig.CONFIG_SPEC,NEW_MOD_ID + "/common.toml");
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(SpawnerDataAttacher.class);
        if (ModList.get().isLoaded("sereneseasons"))
            MinecraftForge.EVENT_BUS.register(SeasonChangedTrigger.class);
        IEventBus modEventBus = context.getModEventBus();
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

    public static HashMap<String, ResourceLocation> DATA_MIGRATION = new HashMap<>();

    @SubscribeEvent
    public void migrateData(ServerStartingEvent event) {
        if (!DATA_MIGRATION.isEmpty())
            return;
        DATA_MIGRATION.put(InsaneSO.RESOURCE_PREFIX + "age", Livestock.AGE);
        DATA_MIGRATION.put(InsaneSO.RESOURCE_PREFIX + "max_age", Livestock.MAX_AGE);
        DATA_MIGRATION.put(InsaneSO.RESOURCE_PREFIX + "last_fed", Livestock.LAST_FED);
        DATA_MIGRATION.put(InsaneSO.RESOURCE_PREFIX + "milk_cooldown", Livestock.MILK_COOLDOWN);
        DATA_MIGRATION.put(InsaneSO.RESOURCE_PREFIX + "xp_processed", DroppedExperience.XP_PROCESSED);
        DATA_MIGRATION.put(InsaneSO.RESOURCE_PREFIX + "passive_regen_ticks", HealthRegenHunger.PASSIVE_REGEN_TICK);
    }

    @SubscribeEvent
    public void migrateData(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity))
            return;

        for (String key : DATA_MIGRATION.keySet()) {
            migrateData(entity, key, DATA_MIGRATION.get(key));
        }
    }

    public static void migrateData(LivingEntity entity, String oldKey, ResourceLocation newKey) {
        if (entity.getPersistentData().contains(oldKey)) {
            Tag tag = entity.getPersistentData().get(oldKey);
            if (tag instanceof ByteTag byteTag)
                ModNBTData.put(entity, newKey, byteTag.getAsByte());
            else if (tag instanceof ShortTag shortTag)
                ModNBTData.put(entity, newKey, shortTag.getAsShort());
            else if (tag instanceof IntTag intTag)
                ModNBTData.put(entity, newKey, intTag.getAsInt());
            else if (tag instanceof LongTag longTag)
                ModNBTData.put(entity, newKey, longTag.getAsLong());
            else if (tag instanceof FloatTag floatTag)
                ModNBTData.put(entity, newKey, floatTag.getAsFloat());
            else if (tag instanceof DoubleTag doubleTag)
                ModNBTData.put(entity, newKey, doubleTag.getAsDouble());
            else if (tag instanceof ByteArrayTag byteArrayTag)
                ModNBTData.put(entity, newKey, byteArrayTag.getAsByteArray());
            else if (tag instanceof StringTag stringTag)
                ModNBTData.put(entity, newKey, stringTag.getAsString());
            else if (tag instanceof ListTag listTag)
                ModNBTData.put(entity, newKey, listTag);
            else if (tag instanceof CompoundTag compoundTag)
                ModNBTData.put(entity, newKey, compoundTag);
            else if (tag instanceof IntArrayTag intArrayTag)
                ModNBTData.put(entity, newKey, intArrayTag.getAsIntArray());
            else if (tag instanceof LongArrayTag longArrayTag)
                ModNBTData.put(entity, newKey, longArrayTag.getAsLongArray());
            entity.getPersistentData().remove(oldKey);
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
    public void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        player.getStats().sendStats(player);
    }

    @SubscribeEvent
    public void remapFromITE(MissingMappingsEvent event) {
        /*InsaneLib.handleMissingMappings(event, MOD_ID, Registries.BLOCK, name -> switch (name) {
            case "cyan_flower" -> CyanFlower.FLOWER.block().get();
            default -> null;
        });*/
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        ISOCommand.register(event.getDispatcher());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.init();
        Modifiers.init();

        event.enqueueWork(() -> {
            ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(location("cyan_flower"), CyanFlower.POTTED_FLOWER);
            ((FlowerPotBlock)Blocks.FLOWER_POT).addPlant(location("solanum_neorossii"), Crops.POTTED_SOLANUM_NEOROSSII);
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
        ISOBlockTagsProvider blockTags = new ISOBlockTagsProvider(generator.getPackOutput(), lookupProvider, InsaneSO.MOD_ID, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ISOItemTagsProvider(generator.getPackOutput(), lookupProvider, blockTags.contentsGetter(), InsaneSO.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeServer(), new ISODamageTypeTagsProvider(generator.getPackOutput(), lookupProvider, InsaneSO.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeServer(), new ISOEntityTypeTagsProvider(generator.getPackOutput(), lookupProvider, InsaneSO.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeClient(), new ISOBlockStatesProvider(generator.getPackOutput(), InsaneSO.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeClient(), new ISOBlockModelsProvider(generator.getPackOutput(), InsaneSO.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeClient(), new ISOItemModelsProvider(generator.getPackOutput(), InsaneSO.MOD_ID, existingFileHelper));
    }

    public void addPackFinders(AddPackFindersEvent event)
    {
        IntegratedPack.onAddPackFinders(event);
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

    public static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
