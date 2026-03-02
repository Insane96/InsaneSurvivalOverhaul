package insane96mcp.insanesurvivaloverhaul.module.mobs;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ModifyCustomSpawnersEvent;

import javax.annotation.Nullable;

@LoadFeature(module = ISOModules.MOBS, description = "Makes zombie siege happen to the player without the need of a village")
public class ZombieSiege extends Feature {

    @SubscribeEvent
    public void onCustomSpawnersInit(ModifyCustomSpawnersEvent event) {
        if (!this.isEnabled())
            return;

        event.addCustomSpawner(new ZombieSiegeSpawner());
    }

    public static class ZombieSiegeSpawner implements CustomSpawner {
        private boolean hasSetupSiege;
        private State siegeState = State.SIEGE_DONE;
        private int zombiesToSpawn;
        private int nextSpawnTime;
        private int spawnX;
        private int spawnY;
        private int spawnZ;
        private Player targetPlayer;

        public int tick(ServerLevel level, boolean spawnHostiles, boolean spawnPassives) {
            if (level.isDay()
                    || !spawnHostiles) {
                this.siegeState = State.SIEGE_DONE;
                this.hasSetupSiege = false;
                return 0;
            }
            float timeOfDay = level.getTimeOfDay(0.0f);
            if (timeOfDay == 0.5f)
                this.siegeState = level.random.nextInt(10) == 0 ? State.SIEGE_TONIGHT : State.SIEGE_DONE;

            if (this.siegeState == State.SIEGE_DONE)
                return 0;

            if (!this.hasSetupSiege) {
                if (!this.tryToSetupSiege(level))
                    return 0;

                this.hasSetupSiege = true;
            }

            if (this.nextSpawnTime > 0) {
                --this.nextSpawnTime;
                return 0;
            }
            this.nextSpawnTime = 2;
            if (this.zombiesToSpawn > 0) {
                this.trySpawn(level);
                --this.zombiesToSpawn;
            }
            else {
                this.siegeState = State.SIEGE_DONE;
                this.hasSetupSiege = false;
            }

            return 1;
        }

        private boolean tryToSetupSiege(ServerLevel level) {
            for (Player player : level.players()) {
                if (player.isSpectator())
                    continue;
                BlockPos blockPos = player.blockPosition();
                if (level.getBiome(blockPos).is(BiomeTags.WITHOUT_ZOMBIE_SIEGES))
                    continue;
                if (level.getChunk(blockPos).getInhabitedTime() < 2 * 60 * 60 * 20) //2 hours
                    continue;
                for (int i = 0; i < 10; ++i) {
                    float f = level.random.nextFloat() * ((float)Math.PI * 2F);
                    this.spawnX = blockPos.getX() + Mth.floor(Mth.cos(f) * 48.0F);
                    this.spawnY = blockPos.getY();
                    this.spawnZ = blockPos.getZ() + Mth.floor(Mth.sin(f) * 48.0F);
                    Vec3 siegeLocation = this.findRandomSpawnPos(level, new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
                    if (siegeLocation != null) {
                        this.nextSpawnTime = 0;
                        this.zombiesToSpawn = 20;
                        this.targetPlayer = player;
                        InsaneSO.LOGGER.debug("Starting siege at {}", siegeLocation);
                        break;
                    }
                }

                return true;
            }

            return false;
        }

        private void trySpawn(ServerLevel pLevel) {
            Vec3 vec3 = this.findRandomSpawnPos(pLevel, new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
            if (vec3 == null)
                return;

            Zombie zombie;
            try {
                zombie = EntityType.ZOMBIE.create(pLevel);
                zombie.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.EVENT, null);
            } catch (Exception exception) {
                InsaneSO.LOGGER.warn("Failed to create zombie for siege at {}", vec3, exception);
                return;
            }

            zombie.moveTo(vec3.x, vec3.y, vec3.z, pLevel.random.nextFloat() * 360.0F, 0.0F);
            zombie.setTarget(this.targetPlayer);
            pLevel.addFreshEntityWithPassengers(zombie);
        }

        @Nullable
        private Vec3 findRandomSpawnPos(ServerLevel pLevel, BlockPos pPos) {
            for (int i = 0; i < 10; ++i) {
                int j = pPos.getX() + pLevel.random.nextInt(16) - 8;
                int k = pPos.getZ() + pLevel.random.nextInt(16) - 8;
                int l = pLevel.getHeight(Heightmap.Types.WORLD_SURFACE, j, k);
                BlockPos blockpos = new BlockPos(j, l, k);
                if (Monster.checkMonsterSpawnRules(EntityType.ZOMBIE, pLevel, MobSpawnType.EVENT, blockpos, pLevel.random))
                    return Vec3.atBottomCenterOf(blockpos);
            }

            return null;
        }

        enum State {
            SIEGE_CAN_ACTIVATE,
            SIEGE_TONIGHT,
            SIEGE_DONE
        }
    }
}
