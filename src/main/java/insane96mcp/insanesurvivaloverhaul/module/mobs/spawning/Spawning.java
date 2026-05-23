package insane96mcp.insanesurvivaloverhaul.module.mobs.spawning;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import java.util.List;

@LoadFeature(module = ISOModules.MOBS, description = "Add a brand new Echo Torch and some changes to mob spawn")
public class Spawning extends Feature {
    public static final ResourceLocation GUARDIAN_MODIFIER_ID = InsaneSO.id("naturally_spawned_guardian");

    public static final SimpleBlockWithItem ECHO_LANTERN = SimpleBlockWithItem.register("echo_lantern", () -> new EchoLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_LANTERN).lightLevel(state -> 7).explosionResistance(120)));

    @Config(description = "Disables Zombie Villagers")
    public static Boolean noZombieVillagers = true;
    @Config(description = "Allows mobs to spawn in the world spawn (in vanilla mobs can't spawn in a 24 blocks radius from world spawn)")
    public static Boolean allowWorldSpawnSpawn = true;
    @Config(description = "Disables normal skeletons from spawning in Fortresses.")
    public static Boolean removeSkeletonsFromFortresses = true;
    @Config(description = "Enables a data pack that makes Guardians spawn in deep oceans. These guardians have half health compared to monument guardians.")
    public static Boolean guardiansInDeepOceans = true;
    @Config(description = "Radius (in blocks) around an Echo Lantern in which monsters cannot spawn.", min = 1, max = 128)
    public static Integer echoLanternRadius = 64;

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        //InsaneSO.addServerPack("yung_better_fortresses", "Insane's Survival Overhaul Yung Better Fortresses", () -> this.isEnabled() /*&& !Packs.disableAllDataPacks*/ && ModList.get().isLoaded("betterfortresses") && removeSkeletonsFromFortresses);
        InsaneSO.addServerPack("ocean_guardians", "Insane's Survival Overhaul Ocean Guardians", () -> this.isEnabled() && /*!Packs.disableAllDataPacks &&*/ guardiansInDeepOceans);
    }

    @SubscribeEvent
    public void onPotentialSpawns(LevelEvent.PotentialSpawns event) {
        if (!this.isEnabled()
                || !removeSkeletonsFromFortresses
                || event.getMobCategory() != MobCategory.MONSTER)
            return;

        Structure fortress = ((ServerLevel)event.getLevel()).structureManager().registryAccess().registryOrThrow(Registries.STRUCTURE).get(BuiltinStructures.FORTRESS);
        if (fortress == null)
            return;

        StructureStart structureStart = ((ServerLevel)event.getLevel()).structureManager().getStructureAt(event.getPos(), fortress);
        if (structureStart.isValid())
            event.getSpawnerDataList().stream().filter(data -> data.type == EntityType.SKELETON).findFirst().ifPresent(event::removeSpawnerData);
    }

    @SubscribeEvent
    public void onZombieVillagerSpawn(FinalizeSpawnEvent event) {
        if (!this.isEnabled()
                || !noZombieVillagers
                || event.getEntity().getType() != EntityType.ZOMBIE_VILLAGER
                || event.getEntity().isAddedToLevel())
            return;

        event.setSpawnCancelled(true);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onGuardianNaturalSpawn(FinalizeSpawnEvent event) {
        if (!this.isEnabled()
                || !guardiansInDeepOceans
                || event.getEntity().getType() != EntityType.GUARDIAN
                || event.getSpawnType() != MobSpawnType.NATURAL
                || event.getEntity().isAddedToLevel())
            return;

        MCUtils.applyModifier(event.getEntity(), Attributes.MAX_HEALTH, GUARDIAN_MODIFIER_ID, -0.5f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    public static final List<MobSpawnType> BLOCKED_SPAWN_TYPES = List.of(MobSpawnType.JOCKEY, MobSpawnType.NATURAL);

    @SubscribeEvent
    public void onMobSpawn(MobSpawnEvent.SpawnPlacementCheck event) {
        if (!this.isEnabled()
                || !(event.getLevel() instanceof ServerLevel serverLevel)
                || !BLOCKED_SPAWN_TYPES.contains(event.getSpawnType())
                || event.getEntityType().getCategory() != MobCategory.MONSTER)
            return;

        if (EchoLanternSavedData.get(serverLevel).isNearLantern(event.getPos(), echoLanternRadius))
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!this.isEnabled()
                || !(event.getLevel() instanceof ServerLevel serverLevel)
                || !event.getState().is(ECHO_LANTERN.block().get()))
            return;

        EchoLanternSavedData.get(serverLevel).addPosition(event.getPos());
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!this.isEnabled()
                || !(event.getLevel() instanceof ServerLevel serverLevel)
                || !event.getState().is(ECHO_LANTERN.block().get()))
            return;

        EchoLanternSavedData.get(serverLevel).removePosition(event.getPos());
    }
}
