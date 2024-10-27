package insane96mcp.iguanatweaksreborn.module.mobs.spawning;

import com.google.common.collect.ImmutableSet;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.ITRRegistries;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

@Label(name = "Spawning", description = "Add a brand new Echo Torch and some changes to mob spawn")
@LoadFeature(module = Modules.Ids.MOBS)
public class Spawning extends Feature {

    public static final SimpleBlockWithItem ECHO_LANTERN = SimpleBlockWithItem.register("echo_lantern", () -> new EchoLanternBlock(BlockBehaviour.Properties.copy(Blocks.SOUL_LANTERN).lightLevel(state -> 7)));
    public static final RegistryObject<PoiType> ECHO_LANTERN_POI = ITRRegistries.POI_TYPES.register("echo_lantern", () -> new PoiType(ImmutableSet.copyOf(ECHO_LANTERN.block().get().getStateDefinition().getPossibleStates()), 1, 64));

    @Config
    @Label(name = "No Zombie Villagers", description = "Disables Zombie Villagers")
    public static Boolean noZombieVillagers = true;
    @Config
    @Label(name = "Allow world spawn spawn", description = "Allows mobs to spawn in the world spawn (in vanilla mobs can't spawn in a 24 blocks radius from world spawn)")
    public static Boolean allowWorldSpawnSpawn = true;
    @Config
    @Label(name = "Remove skeletons from Fortresses", description = "Disables normal skeletons from spawning in Fortresses. Requires a minecraft restart")
    public static Boolean removeSkeletonsFromFortresses = true;

    public Spawning(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "yung_better_fortresses", Component.literal("IguanaTweaks Reborn Yung Better Fortresses"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && ModList.get().isLoaded("betterfortresses") && removeSkeletonsFromFortresses));
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (!this.isEnabled()
                || !phantomsInTheEnd)
            return;

        event.getServer().getGameRules().getRule(GameRules.RULE_DOINSOMNIA).set(false, event.getServer());
    }

    @SubscribeEvent
    public void onPotentialSpawns(LevelEvent.PotentialSpawns event) {
        if (!this.isEnabled()
                || removeSkeletonsFromFortresses)
            return;

        Structure structure = ((ServerLevel)event.getLevel()).structureManager().registryAccess().registryOrThrow(Registries.STRUCTURE).get(BuiltinStructures.FORTRESS);
        if (structure != null)
            event.getSpawnerDataList().stream().filter(data -> data.type == EntityType.SKELETON).findFirst().ifPresent(event::removeSpawnerData);
    }

    @SubscribeEvent
    public void onZombieVillagerSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (!this.isEnabled()
                || !noZombieVillagers
                || event.getEntity().getType() != EntityType.ZOMBIE_VILLAGER
                || event.getEntity().isAddedToWorld())
            return;

        event.setSpawnCancelled(true);
        event.setCanceled(true);
    }

    public static final List<MobSpawnType> BLOCKED_SPAWN_TYPES = List.of(MobSpawnType.JOCKEY, MobSpawnType.NATURAL);

    @SubscribeEvent
    public void onMobSpawn(MobSpawnEvent.SpawnPlacementCheck event) {
        if (!this.isEnabled()
                || !(event.getLevel() instanceof ServerLevel serverLevel)
                || !BLOCKED_SPAWN_TYPES.contains(event.getSpawnType())
                || event.getEntityType().getCategory() != MobCategory.MONSTER)
            return;

        boolean theresTorch = serverLevel.getPoiManager().findAll(poiTypeHolder -> poiTypeHolder.is(ECHO_LANTERN_POI.getKey()), blockPos -> true, event.getPos(), 64, PoiManager.Occupancy.ANY)
                .findAny().isPresent();
        if (theresTorch)
            event.setResult(Event.Result.DENY);
    }
}
