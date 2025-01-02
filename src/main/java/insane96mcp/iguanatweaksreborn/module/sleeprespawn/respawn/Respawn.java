package insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn;

import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.utils.ITRLogHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.util.LogHelper;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

@Label(name = "Respawn", description = "Changes to respawning. Adds the doLooseRespawn gamerule that can disable the loose spawn range")
@LoadFeature(module = Modules.Ids.SLEEP_RESPAWN)
public class Respawn extends Feature {

	public static final String LOOSE_RESPAWN_POINT_SET = IguanaTweaksReborn.MOD_ID + ".loose_bed_respawn_point_set";
	public static final GameRules.Key<GameRules.BooleanValue> RULE_RANGEDRESPAWN = GameRules.register("iguanatweaks:doLooseRespawn", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

	public static final String DEATHS = IguanaTweaksReborn.RESOURCE_PREFIX + "deaths";
	public static final String HUNGER_ON_DEATH_TAG = IguanaTweaksReborn.RESOURCE_PREFIX + "hunger_on_death";
	public static final String SATURATION_ON_DEATH_TAG = IguanaTweaksReborn.RESOURCE_PREFIX + "saturation_on_death";

	@Config(min = 0)
	@Label(name = "Loose World Spawn Range", description = "The range from world spawn where players will respawn.")
	public static MinMax looseWorldSpawnRange = new MinMax(128d, 192d);
	@Config(min = 0)
	@Label(name = "Despawn mobs on world respawn", description = "Mobs in this range from the player will be despawned when respawning at world spawn.")
	public static Integer despawnMobsOnWorldRespawn = 64;

	@Config(min = 0)
	@Label(name = "Loose Bed Spawn Range", description = "The range from beds where players will respawn.")
	public static MinMax looseBedSpawnRange = new MinMax(64d, 128d);
	@Config(min = 0)
	@Label(name = "Despawn mobs on bed respawn", description = "Mobs in this range from the player will be despawned when respawning at bed spawn.")
	public static Integer despawnMobsOnBedRespawn = 32;

	@Config(min = 0, max = 20)
	@Label(name = "Stats Penalty.Health.Minimum", description = "Min Health of respawning players")
	public static Difficulty minHealthOnRespawn = new Difficulty(10, 10, 6);
	@Config(min = 0, max = 20)
	@Label(name = "Stats Penalty.Health.Per Death", description = "How much health respawning players lose on respawn (not max health)")
	public static Difficulty perDeathHealthOnRespawn = new Difficulty(1, 2, 2);
	@Config(min = 0, max = 20)
	@Label(name = "Stats Penalty.Hunger.Minimum", description = "Min Hunger of respawning players")
	public static Difficulty hungerOnRespawn = new Difficulty(14, 14, 10);
	@Config(min = 0, max = 20)
	@Label(name = "Stats Penalty.Saturation.Minimum", description = "Min Saturation of respawning players")
	public static Difficulty saturationOnRespawn = new Difficulty(10, 10, 6);
	@Config
	@Label(name = "Stats Penalty.Only if below", description = "If hunger or saturation were above the values on death, they will not be reduced.")
	public static Boolean respawnFoodOnlyIfBelow = true;

	public Respawn(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Player player))
			return;

		MCUtils.getOrCreatePersistedData(player).putInt(DEATHS, MCUtils.getOrCreatePersistedData(player).getInt(DEATHS) + 1);
		MCUtils.getOrCreatePersistedData(player).putInt(HUNGER_ON_DEATH_TAG, player.getFoodData().foodLevel);
		MCUtils.getOrCreatePersistedData(player).putFloat(SATURATION_ON_DEATH_TAG, player.getFoodData().saturationLevel);
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!this.isEnabled()
				|| event.isEndConquered())
			return;

		Player player = event.getEntity();
		int hunger = MCUtils.getOrCreatePersistedData(player).getInt(HUNGER_ON_DEATH_TAG);
		int hOnRespawn = (int) hungerOnRespawn.getByDifficulty(player.level());
		if (!respawnFoodOnlyIfBelow || hunger < hOnRespawn)
			player.getFoodData().foodLevel = hOnRespawn;
		else
			player.getFoodData().foodLevel = hunger;
		float saturation = MCUtils.getOrCreatePersistedData(player).getFloat(SATURATION_ON_DEATH_TAG);
		float sOnRespawn = (float) saturationOnRespawn.getByDifficulty(player.level());
		if (!respawnFoodOnlyIfBelow || saturation < sOnRespawn)
			player.getFoodData().saturationLevel = sOnRespawn;
		else
			player.getFoodData().saturationLevel = saturation;
		double healthOnRespawn = player.getMaxHealth() - (perDeathHealthOnRespawn.getByDifficulty(player.level()) * MCUtils.getOrCreatePersistedData(player).getInt(DEATHS));
		double minHealth = minHealthOnRespawn.getByDifficulty(player.level());
		player.setHealth((float) Math.max(healthOnRespawn, minHealth));
	}

	//Run before ITE Respawn Obelisk
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!this.isEnabled()
				|| event.isEndConquered()
				|| !event.getEntity().level().getGameRules().getBoolean(RULE_RANGEDRESPAWN))
			return;

		boolean hasRespawned = looseWorldSpawn(event);
		if (!hasRespawned)
			looseBedSpawn(event);
	}

	private boolean looseWorldSpawn(PlayerEvent.PlayerRespawnEvent event) {
		if (looseWorldSpawnRange.min == 0d
				|| event.getEntity().isSpectator())
			return false;
		ServerPlayer player = (ServerPlayer) event.getEntity();
		BlockPos pos = player.getRespawnPosition();
		if (pos != null)
			return false;

		BlockPos respawnPos = getSpawnPositionInRange(player.level().getSharedSpawnPos(), looseWorldSpawnRange, player.level(), player.level().random);
		if (respawnPos == null)
			return false;

		event.getEntity().teleportToWithTicket(respawnPos.getX() + 0.5d, respawnPos.getY() + 0.5d, respawnPos.getZ() + 0.5d);
		List<Entity> entities = player.level().getEntities(player, new AABB(respawnPos).inflate(despawnMobsOnWorldRespawn), entity -> entity instanceof Monster monster && !monster.isPersistenceRequired());
		ITRLogHelper.debug("Despawning %d entities", entities.size());
		entities.forEach(Entity::discard);
		return true;
	}

	private boolean looseBedSpawn(PlayerEvent.PlayerRespawnEvent event) {
		if (looseBedSpawnRange.min == 0d
				|| event.getEntity().isSpectator())
			return false;
		ServerPlayer player = (ServerPlayer) event.getEntity();
		BlockPos pos = player.getRespawnPosition();
		if (pos == null
				|| !event.getEntity().level().getBlockState(pos).is(BlockTags.BEDS))
			return false;

		BlockPos respawnPos = getSpawnPositionInRange(pos, looseBedSpawnRange, player.level(), player.level().random);
		if (respawnPos == null)
			return false;

		event.getEntity().teleportToWithTicket(respawnPos.getX() + 0.5d, respawnPos.getY() + 0.5d, respawnPos.getZ() + 0.5d);
		List<Entity> entities = player.level().getEntities(player, new AABB(respawnPos).inflate(despawnMobsOnBedRespawn), entity -> entity instanceof Monster monster && !monster.isPersistenceRequired());
		ITRLogHelper.debug("Despawning %d entities", entities.size());
		entities.forEach(Entity::discard);
		return true;
	}

	@Nullable
	private BlockPos getSpawnPositionInRange(BlockPos center, MinMax minMax, Level level, RandomSource random) {
		double minSqr = minMax.min * minMax.min;
		double maxSqr = minMax.max * minMax.max;
		int x, y, z;
		BlockState stateBelow;
		BlockPos.MutableBlockPos respawn = new BlockPos.MutableBlockPos();
		boolean foundValidY = false;
		int triesLeft = 1000;
		do {
			do {
				x = random.nextInt((int) -minMax.max, (int) minMax.max);
				z = random.nextInt((int) -minMax.max, (int) minMax.max);
			} while (x * x + z * z > maxSqr || x * x + z * z < minSqr);
			y = level.getMaxBuildHeight();
			do {
				respawn.set(x + center.getX(), y, z + center.getZ());
				stateBelow = level.getBlockState(respawn.below());
				//Discard if there's lava below
				if (stateBelow.getFluidState().is(FluidTags.LAVA))
					break;
				if (stateBelow.blocksMotion() || !stateBelow.getFluidState().isEmpty()) {
					foundValidY = true;
					break;
				}
				y--;
			} while (y > level.getMinBuildHeight());
			triesLeft--;
		} while (!foundValidY && triesLeft > 0);
		if (triesLeft <= 0) {
			LogHelper.warn("Failed to find a respawn point within %s", center);
			return null;
		}

		return respawn.immutable();
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onSetSpawnLooseMessage(PlayerSetSpawnEvent event) {
		if (!this.isEnabled()
				|| event.isForced()
				|| !event.getEntity().level().getGameRules().getBoolean(RULE_RANGEDRESPAWN)
				|| looseBedSpawnRange.min == 0d
				|| event.getNewSpawn() == null
				|| !event.getEntity().level().getBlockState(event.getNewSpawn()).is(BlockTags.BEDS))
			return;

		ServerPlayer player = (ServerPlayer) event.getEntity();
		if (event.getNewSpawn().equals(player.getRespawnPosition()))
			return;
		player.displayClientMessage(Component.translatable(LOOSE_RESPAWN_POINT_SET), false);
	}
}