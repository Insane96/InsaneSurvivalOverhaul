package insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.data.ISOMobEffectInstance;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
import insane96mcp.iguanatweaksreborn.utils.ISOLogHelper;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.Difficulty;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.data.IdTagValue;
import insane96mcp.insanelib.util.LogHelper;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Label(name = "Respawn", description = "Changes to respawning. Adds the doLooseRespawn gamerule that can disable the loose spawn range")
@LoadFeature(module = Modules.Ids.SLEEP_RESPAWN)
public class Respawn extends JsonFeature {
	public static final TagKey<Block> RESPAWN_OBELISK_BLOCKS_TO_ROT = ISOBlockTagsProvider.create("structures/respawn_obelisk/blocks_to_rot");

	public static final String FAIL_RESPAWN_OBELISK_LANG = InsaneSurvivalOverhaul.MOD_ID + ".fail_respawn_obelisk";

	public static final String LOOSE_RESPAWN_POINT_SET = InsaneSurvivalOverhaul.MOD_ID + ".loose_bed_respawn_point_set";
	public static final GameRules.Key<GameRules.BooleanValue> RULE_RANGEDRESPAWN = GameRules.register("iguanatweaks:doLooseRespawn", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

	public static final String DEATHS = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "deaths";
	public static final String HUNGER_ON_DEATH_TAG = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "hunger_on_death";
	public static final String SATURATION_ON_DEATH_TAG = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "saturation_on_death";

	public static final SimpleBlockWithItem RESPAWN_OBELISK = SimpleBlockWithItem.register("respawn_obelisk", () -> new RespawnObeliskBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel(RespawnObeliskBlock::lightLevel)));

	@Config(min = 0)
	@Label(name = "Loose World Spawn Range", description = "The range from world spawn where players will respawn.")
	public static MinMax looseWorldSpawnRange = new MinMax(96d, 192d);
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

	@Config
	@Label(name = "Allow obelisk spawn point overwrite with beds", description = "If disabled, beds spawn point will not overwrite obelisk spawn point")
	public static Boolean allowObeliskSpawnPointOverwriteWithBeds = true;

	public static final List<IdTagValue> RESPAWN_OBELISK_CATALYSTS_DEFAULT = List.of(
			IdTagValue.newId("minecraft:iron_block", 0.75d),
			IdTagValue.newId("minecraft:gold_block", 0.3d),
			IdTagValue.newId("iguanatweaksexpanded:durium_block", 0.075d),
			IdTagValue.newId("minecraft:diamond_block", 0.05d),
			IdTagValue.newId("iguanatweaksexpanded:keego_block", 0.05d),
			IdTagValue.newId("iguanatweaksexpanded:quaron_block", 0.25d),
			IdTagValue.newId("iguanatweaksexpanded:soul_steel_block", 0.05d),
			IdTagValue.newId("minecraft:emerald_block", 0.35d),
			IdTagValue.newId("minecraft:netherite_block", 0d)
	);

	public static final ArrayList<IdTagValue> respawnObeliskCatalysts = new ArrayList<>();

	public static final List<ISOMobEffectInstance> RESPAWN_OBELISK_EFFECTS_DEFAULT = List.of(
			new ISOMobEffectInstance.Builder(MobEffects.REGENERATION, 45 * 20)
					.ambientParticles()
					.build(),
			new ISOMobEffectInstance.Builder(MobEffects.ABSORPTION, 60 * 20)
					.setAmplifier(1)
					.ambientParticles()
					.build(),
			new ISOMobEffectInstance.Builder(MobEffects.MOVEMENT_SPEED, 60 * 20)
					.ambientParticles()
					.build()
	);

	public static final ArrayList<ISOMobEffectInstance> respawnObeliskEffects = new ArrayList<>();

	public Respawn(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonFeature.JsonConfig<>("respawn_obelisk_catalysts.json", respawnObeliskCatalysts, RESPAWN_OBELISK_CATALYSTS_DEFAULT, IdTagValue.LIST_TYPE));
		JSON_CONFIGS.add(new JsonFeature.JsonConfig<>("respawn_obelisk_effects.json", respawnObeliskEffects, RESPAWN_OBELISK_EFFECTS_DEFAULT, ISOMobEffectInstance.LIST_TYPE));
		IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "respawn_obelisk", Component.literal("Insane's Survival Overhaul Respawn Obelisk"), this::isEnabled));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSurvivalOverhaul.CONFIG_FOLDER;
	}

	@Override
	public void loadJsonConfigs() {
		if (!this.isEnabled())
			return;
		super.loadJsonConfigs();
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

		applyStatsPenalty(event.getEntity());
		tryLooseRespawnAndObelisk(event);
	}

	public void applyStatsPenalty(Player player) {
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

	public void tryLooseRespawnAndObelisk(PlayerEvent.PlayerRespawnEvent event) {
		if (!event.getEntity().level().getGameRules().getBoolean(RULE_RANGEDRESPAWN))
			return;

		boolean hasRespawned = looseWorldSpawn(event);
		if (!hasRespawned)
			looseBedSpawn(event);
		tryRespawnObelisk(event);
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
		ISOLogHelper.info("Despawning %d entities", entities.size());
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
		ISOLogHelper.info("Despawning %d entities", entities.size());
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
			y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX() + x, center.getZ() + z);
			if (y < level.getSeaLevel() + 2)
				y = level.getSeaLevel() + 2;
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

	private void tryRespawnObelisk(PlayerEvent.PlayerRespawnEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		BlockPos pos = player.getRespawnPosition();
		if (pos == null
				|| !player.level().getBlockState(pos).is(RESPAWN_OBELISK.block().get()))
			return;

		if (!player.level().getBlockState(pos).getValue(RespawnObeliskBlock.ENABLED)) {
			player.sendSystemMessage(Component.translatable(FAIL_RESPAWN_OBELISK_LANG));
			RespawnObeliskBlock.trySetOldSpawn(player);
			return;
		}
		RespawnObeliskBlock.onObeliskRespawn(player, player.level(), pos);
	}

	@SubscribeEvent
	public void onSetRespawn(PlayerSetSpawnEvent event) {
		if (!this.isEnabled()
				|| event.isForced())
			return;

		onSetSpawnLooseMessage(event);
		onSetSpawnPreventObeliskOverwrite(event);
	}

	public void onSetSpawnLooseMessage(PlayerSetSpawnEvent event) {
		if (!event.getEntity().level().getGameRules().getBoolean(RULE_RANGEDRESPAWN)
				|| looseBedSpawnRange.min == 0d
				|| event.getNewSpawn() == null
				|| !event.getEntity().level().getBlockState(event.getNewSpawn()).is(BlockTags.BEDS))
			return;

		ServerPlayer player = (ServerPlayer) event.getEntity();
		if (event.getNewSpawn().equals(player.getRespawnPosition()))
			return;
		player.displayClientMessage(Component.translatable(LOOSE_RESPAWN_POINT_SET), false);
	}

	public void onSetSpawnPreventObeliskOverwrite(PlayerSetSpawnEvent event) {
		if (allowObeliskSpawnPointOverwriteWithBeds)
			return;
		ServerPlayer player = (ServerPlayer) event.getEntity();
		if (player.getRespawnPosition() != null
				&& player.level().dimension().equals(player.getRespawnDimension())
				&& player.level().getBlockState(player.getRespawnPosition()).is(RESPAWN_OBELISK.block().get())
				&& player.level().getBlockState(player.getRespawnPosition()).getValue(RespawnObeliskBlock.ENABLED)
				&& event.getNewSpawn() != null
				&& !player.level().getBlockState(event.getNewSpawn()).is(RESPAWN_OBELISK.block().get())) {
			if (RespawnObeliskBlock.saveOldSpawn(player, event.getNewSpawn(), event.isForced(), 0f, event.getSpawnLevel()))
				player.sendSystemMessage(Component.translatable("iguanatweaksreborn.set_old_respawn"));
			event.setCanceled(true);
		}
	}
}