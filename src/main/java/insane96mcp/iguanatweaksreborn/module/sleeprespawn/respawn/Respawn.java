package insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.data.ISOMobEffectInstance;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
	@Config
	@Label(name = "Don't respawn on fluid", description = "If enabled, respawning will try to place you on land and not in fluids")
	public static Boolean dontRespawnOnFluid = true;

	@Config(min = 0, max = 20)
	@Label(name = "Stats Penalty.Health.Minimum", description = "Min Health of respawning players")
	public static Difficulty minHealthOnRespawn = new Difficulty(10, 10, 6);
	@Config(min = 0, max = 20)
	@Label(name = "Stats Penalty.Health.Per Death", description = "How much health respawning players lose on respawn (not max health)")
	public static Difficulty perDeathHealthOnRespawn = new Difficulty(1, 2, 2);
	@Config(min = 0, max = 20)
	@Label(name = "Stats Penalty.Hunger.Minimum", description = "Min Hunger of respawning players. If below this value on death will be set to this value")
	public static Difficulty hungerOnRespawnMin = new Difficulty(6, 6, 6);
	@Config(min = 0, max = 20)
	@Label(name = "Stats Penalty.Hunger.Maximum", description = "Max Hunger of respawning players. If above this value on death will be set to this value")
	public static Difficulty hungerOnRespawnMax = new Difficulty(14, 14, 10);
	@Config(min = 0, max = 20)
	@Label(name = "Stats Penalty.Saturation.Minimum", description = "Min Saturation of respawning players. If below this value on death will be set to this value")
	public static Difficulty saturationOnRespawnMin = new Difficulty(6, 6, 6);
	@Config(min = 0, max = 20)
	@Label(name = "Stats Penalty.Saturation.Maximum", description = "Max Saturation of respawning players. If above this value on death will be set to this value")
	public static Difficulty saturationOnRespawnMax = new Difficulty(10, 10, 6);

	@Config
	@Label(name = "Allow obelisk spawn point overwrite with beds when sneaking", description = "If enabled, sleeping a bed when sneaking will overwrite obelisk spawn point")
	public static Boolean allowObeliskSpawnPointOverwriteWithBedsSneaking = true;

	public static final List<IdTagValue> RESPAWN_OBELISK_CATALYSTS_DEFAULT = List.of(
			IdTagValue.newId("minecraft:iron_block", 0.75d),
			IdTagValue.newId("minecraft:gold_block", 0.3d),
			IdTagValue.newId("caverns_and_chasms:silver_block", 0.3d),
			IdTagValue.newId("caverns_and_chasms:sanguine_block", 0.25d),
			IdTagValue.newId("iguanatweaksexpanded:durium_block", 0.075d),
			IdTagValue.newId("minecraft:diamond_block", 0.05d),
			IdTagValue.newId("iguanatweaksexpanded:keego_block", 0.05d),
			IdTagValue.newId("iguanatweaksexpanded:quaron_block", 0.25d),
			IdTagValue.newId("iguanatweaksexpanded:soul_steel_block", 0.05d),
			IdTagValue.newId("minecraft:emerald_block", 0.35d),
			IdTagValue.newId("minecraft:netherite_block", 0d),
			IdTagValue.newId("caverns_and_chasms:necromium_block", 0d)
	);

	public static final ArrayList<IdTagValue> respawnObeliskCatalysts = new ArrayList<>();

	public static final List<ISOMobEffectInstance> RESPAWN_OBELISK_EFFECTS_DEFAULT = List.of(
			new ISOMobEffectInstance.Builder(MobEffects.REGENERATION, 45 * 20)
					.noParticles()
					.build(),
			new ISOMobEffectInstance.Builder(MobEffects.ABSORPTION, 60 * 20)
					.setAmplifier(1)
					.noParticles()
					.build(),
			new ISOMobEffectInstance.Builder(MobEffects.MOVEMENT_SPEED, 60 * 20)
					.noParticles()
					.build(),
			new ISOMobEffectInstance.Builder(MobEffects.SATURATION, 20 * 20)
					.noParticles()
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
		tryRespawnObelisk(event);
	}

	public void applyStatsPenalty(Player player) {
		int hunger = MCUtils.getOrCreatePersistedData(player).getInt(HUNGER_ON_DEATH_TAG);
		int maxHunger = (int) hungerOnRespawnMax.getByDifficulty(player.level());
		int minHunger = (int) hungerOnRespawnMin.getByDifficulty(player.level());
		hunger = Mth.clamp(hunger, minHunger, maxHunger);
		player.getFoodData().foodLevel = hunger;

		float saturation = MCUtils.getOrCreatePersistedData(player).getFloat(SATURATION_ON_DEATH_TAG);
		float maxSaturation = (float) saturationOnRespawnMax.getByDifficulty(player.level());
		float minSaturation = (float) saturationOnRespawnMin.getByDifficulty(player.level());
		saturation = Mth.clamp(saturation, minSaturation, maxSaturation);
		player.getFoodData().saturationLevel = saturation;

		double healthOnRespawn = player.getMaxHealth() - (perDeathHealthOnRespawn.getByDifficulty(player.level()) * MCUtils.getOrCreatePersistedData(player).getInt(DEATHS));
		double minHealth = minHealthOnRespawn.getByDifficulty(player.level());
		player.setHealth((float) Math.max(healthOnRespawn, minHealth));
	}

	public static Optional<Vec3> tryLooseRespawn(ServerLevel level, ServerPlayer player, Optional<Vec3> originalRespawn) {
		if (!level.getGameRules().getBoolean(RULE_RANGEDRESPAWN))
			return originalRespawn;

		Optional<Vec3> newRespawn = looseWorldSpawn(level, player);
		if (newRespawn.isEmpty())
			newRespawn = looseBedSpawn(level, player);
		return newRespawn.or(() -> originalRespawn);
	}

	private static Optional<Vec3> looseWorldSpawn(ServerLevel level, ServerPlayer player) {
		if (looseWorldSpawnRange.min == 0d
				|| player.isSpectator())
			return Optional.empty();
		BlockPos pos = player.getRespawnPosition();
		if (pos != null)
			return Optional.empty();

		BlockPos respawnPos = getSpawnPositionInRange(level.getSharedSpawnPos(), looseWorldSpawnRange, level, level.random);
		if (respawnPos == null)
			return Optional.empty();

		//event.getEntity().teleportToWithTicket(respawnPos.getX() + 0.5d, respawnPos.getY() + 0.5d, respawnPos.getZ() + 0.5d);
		//List<Entity> entities = player.level().getEntities(player, new AABB(respawnPos).inflate(despawnMobsOnWorldRespawn), entity -> entity instanceof Monster monster && !monster.isPersistenceRequired());
		//ISOLogHelper.info("Despawning %d entities", entities.size());
		//entities.forEach(Entity::discard);
		return Optional.of(new Vec3(respawnPos.getX() + 0.5d, respawnPos.getY() + 0.5d, respawnPos.getZ() + 0.5d));
	}

	private static Optional<Vec3> looseBedSpawn(ServerLevel level, ServerPlayer player) {
		if (looseBedSpawnRange.min == 0d
				|| player.isSpectator())
			return Optional.empty();
		BlockPos pos = player.getRespawnPosition();
		if (pos == null
				|| !level.getBlockState(pos).is(BlockTags.BEDS))
			return Optional.empty();

		BlockPos respawnPos = getSpawnPositionInRange(pos, looseBedSpawnRange, level, level.random);
		if (respawnPos == null)
			return Optional.empty();

		//event.getEntity().teleportToWithTicket(respawnPos.getX() + 0.5d, respawnPos.getY() + 0.5d, respawnPos.getZ() + 0.5d);
		//List<Entity> entities = player.level().getEntities(player, new AABB(respawnPos).inflate(despawnMobsOnBedRespawn), entity -> entity instanceof Monster monster && !monster.isPersistenceRequired());
		//ISOLogHelper.info("Despawning %d entities", entities.size());
		//entities.forEach(Entity::discard);
		return Optional.of(new Vec3(respawnPos.getX() + 0.5d, respawnPos.getY() + 0.5d, respawnPos.getZ() + 0.5d));
	}

	@Nullable
	private static BlockPos getSpawnPositionInRange(BlockPos center, MinMax minMax, Level level, RandomSource random) {
		double minSqr = minMax.min * minMax.min;
		double maxSqr = minMax.max * minMax.max;
		int x, y, z;
		BlockState stateBelow;
		BlockPos.MutableBlockPos respawn = new BlockPos.MutableBlockPos();
		boolean foundValidY = false;
		int triesLeft = 1024;
		do {
			do {
				x = random.nextInt((int) -minMax.max, (int) minMax.max);
				z = random.nextInt((int) -minMax.max, (int) minMax.max);
			} while (x * x + z * z > maxSqr || x * x + z * z < minSqr);
			y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX() + x, center.getZ() + z);
			if (y < level.getSeaLevel() + 2)
				y = level.getSeaLevel() + 2;
			while (level.getBlockState(respawn.set(x + center.getX(), y, z + center.getZ())).blocksMotion()) {
				y++;
			}
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
			if (dontRespawnOnFluid && !stateBelow.getFluidState().isEmpty())
				foundValidY = false;
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

		if (onSetSpawnPreventObeliskOverwrite(event))
			return;
		onSetSpawnLooseMessage(event);
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

	/// Returns true if the spawn point was prevented from being overwritten
	public boolean onSetSpawnPreventObeliskOverwrite(PlayerSetSpawnEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		if (player.getRespawnPosition() == null
				|| !player.level().dimension().equals(player.getRespawnDimension())
				|| !player.level().getBlockState(player.getRespawnPosition()).is(RESPAWN_OBELISK.block().get())
				|| !player.level().getBlockState(player.getRespawnPosition()).getValue(RespawnObeliskBlock.ENABLED)
				|| event.getNewSpawn() == null
				|| player.level().getBlockState(event.getNewSpawn()).is(RESPAWN_OBELISK.block().get()))
			return false;
		if (allowObeliskSpawnPointOverwriteWithBedsSneaking) {
            if (!event.getEntity().isCrouching())
                player.sendSystemMessage(Component.translatable("iguanatweaksreborn.sneak_to_overwrite"));
			else
            	return false;
        }

		if (RespawnObeliskBlock.saveOldSpawn(player, event.getNewSpawn(), event.isForced(), 0f, event.getSpawnLevel()))
			player.sendSystemMessage(Component.translatable("iguanatweaksreborn.set_old_respawn"));
		event.setCanceled(true);
		return true;
	}
}