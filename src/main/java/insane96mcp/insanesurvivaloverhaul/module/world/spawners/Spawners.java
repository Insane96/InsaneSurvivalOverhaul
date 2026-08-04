package insane96mcp.insanesurvivaloverhaul.module.world.spawners;

import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import insane96mcp.insanelib.data.ObjTagValue;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.BaseSpawnerAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.world.spawners.capability.SpawnerDataImpl;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.Optional;

@LoadFeature(module = ISOModules.WORLD, description = "Spawners are now a challenge. Monsters spawning from spawners ignore light.")
public class Spawners extends JsonFeature {

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<SpawnerDataImpl>> SPAWNER_DATA =
			ISORegistries.ATTACHMENT_TYPES.register("spawner_data", () -> AttachmentType.builder(SpawnerDataImpl::new)
					.serialize(SpawnerDataImpl.CODEC)
					.build());
	public static final ResourceKey<LootTable> EMPOWERED_SPAWNER_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, InsaneSO.id("empowered_spawner"));

	public static final TagKey<EntityType<?>> BLACKLISTED_SPAWNERS = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.id("blacklisted_spawners"));
	public static final TagKey<Item> SPAWNER_REACTIVATOR_TAG = ISOItemTagsProvider.create("spawner_reactivator");
	public static final MutableComponent SPAWNER_REACTIVATOR_MESSAGE = Component.translatable(InsaneSO.lang("spawner_reactivator")).withStyle(ChatFormatting.LIGHT_PURPLE);

	@Config(min = 1, description = "If true, the spawner delay is set to 'Delay' instead of using MinSpawnDelay and MaxSpawnDelay")
	public static Boolean overrideSpawnDelay = true;
	@Config(min = 1, description = "Spawning Delay (in ticks) of the spawner. Vanilla is 200~800. Requires 'Override Spawn Delay' to be enabled.")
	public static MinMaxConfig delay = new MinMaxConfig(400, 1600);
	@Config(min = 0, description = "Range in which a player must be present for a spawner to work. Vanilla is 16.")
	public static int requiredPlayerRange = 24;
	@Config(description = "If true, monsters from spawners will spawn no matter the light level.")
	public static Boolean ignoreLight = true;
	@Config(name = "Re-enable with Spawner Reactivator", description = "If true, disabled spawners can be re-enabled with a spawner reactivator item defined in the `insanesurvivaloverhaul:spawner_reactivator` item tag. These items get a new tooltip mentioning that they can be used to re-enable spawners.")
	public static Boolean reEnableWithSpawnerReactivator = false;
	@Config(description = "If enabled, spawner will play a sound effect when spawning mobs")
	public static Boolean spawningSoundEffect = true;

	@Config(description = "If true, spawners will be disabled after spawning a certain amount of mobs. This is not compatible with empowered spawners.")
	public static Boolean disableSpawners$enabled = false;
	@Config(min = 0, description = "The minimum amount of spawnable mobs (when the spawner is basically in the same position as the world spawn). The amount of spawnable mobs before deactivating is equal to the distance divided by 8 (plus this value). E.g. At 160 blocks from spawn the max spawnable mobs will be 160 / 8 + 20 = 20 + 20 = 40")
	public static Integer disableSpawners$minSpawnableMobs = 20;
	@Config(min = 0d, description = "This multiplier increases the max mobs spawned.")
	public static Double disableSpawners$spawnableMobsMultiplier = 1.0d;

	@Config(description = "If true, spawners will generate in an empowered state. When empowered, will generate mobs really fast for a while and then will disable. This is not compatible with disable spawners.")
	public static Boolean empowered$enabled = true;
	@Config(min = 0, description = "When the spawner stops being empowered, it is disabled and will stop spawning mobs.")
	public static Boolean empowered$disableOnEnd = true;
	@Config(min = 0, name = "Empowered.Mobs amount", description = "How many mobs are spawned before empowered ends.")
	public static Integer empoweredMobsAmount = 20;
	@Config(min = 1, name = "Empowered.Delay", description = "Spawning Delay (in ticks) when the Spawner is empowered.")
	public static MinMaxConfig empoweredDelay = new MinMaxConfig(150, 300);
	@Config(min = 0, name = "Empowered.Experience Reward", description = "When the Spawner stops being empowered, will generate this amount of experience")
	public static MinMaxConfig empoweredExperienceReward = new MinMaxConfig(0);
	@Config(name = "Empowered.Loot Reward", description = "When the Spawner stops being empowered, will generate loot from the insanesurvivaloverhaul:empowered_spawner loot table")
	public static Boolean empoweredLootReward = false;
	@Config(name = "Empowered.Sound effect", description = "When the Spawner stops being empowered, will play a sound effect")
	public static Boolean empoweredSoundEffect = true;

	@Config(min = -1, description = "The range of a player nearby in which the spawner will be active. Setting to -1 should keep the spawners always active (untested) as long as the chunk is loaded (hostiles will still despawn in a 128 block radius).")
	public static Integer reactivatedSpawners$playerRange = 128;
	@Config(description = "How many ticks will the spawner try to summon mobs.")
	public static MinMaxConfig reactivatedSpawners$delay = new MinMaxConfig(3600, 6000);
	@Config(description = "Reactivated spawners spawn mobs no matter the light level.")
	public static Boolean reactivatedSpawners$ignoreLight = false;

	public static final ArrayList<ObjTagValue<EntityType<?>>> FIXED_SPAWNER_SPAWNABLE_DEFAULT = new ArrayList<>();
	public static final ArrayList<ObjTagValue<EntityType<?>>> fixedSpawnerSpawnable = new ArrayList<>();

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		getJsonConfigs().add(new JsonConfig<>("fixed_spawners_spawnable.json", fixedSpawnerSpawnable, FIXED_SPAWNER_SPAWNABLE_DEFAULT, ObjTagValue.LIST_TYPE).withRegistryFor(Registries.ENTITY_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onSpawnerSpawn(FinalizeSpawnEvent event) {
		if (!this.isEnabled()
				|| !event.getSpawnType().equals(MobSpawnType.SPAWNER)
				|| event.getSpawner() == null
				|| !(event.getSpawner().left().orElse(null) instanceof SpawnerBlockEntity mobSpawner))
			return;

		BlockPos spawnerPos = mobSpawner.getBlockPos();
		ServerLevel level = (ServerLevel) event.getLevel();
		if (spawningSoundEffect)
			level.playSound(null, spawnerPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 2f, 1.5f);
		SpawnerDataImpl spawnerCap = mobSpawner.getData(Spawners.SPAWNER_DATA.get());
		spawnerCap.addSpawnedMobs(1);
		disabledSpawners(mobSpawner, event.getEntity(), level, spawnerPos, spawnerCap);
		empoweredSpawner(mobSpawner, event.getEntity(), level, spawnerPos, spawnerCap);
	}

	private void disabledSpawners(SpawnerBlockEntity spawnerBlockEntity, Mob mob, ServerLevel level, BlockPos spawnerPos, SpawnerDataImpl spawnerCap) {
        if (!disableSpawners$enabled
				|| mob.getType().is(BLACKLISTED_SPAWNERS))
			return;

        int maxSpawned = 0;
		for (ObjTagValue<EntityType<?>> idTagValue : fixedSpawnerSpawnable) {
			if (idTagValue.id.matches(mob.getType())) {
				maxSpawned = (int) idTagValue.value;
				break;
			}
		}
		if (maxSpawned <= 0) {
			double distance = Math.sqrt(spawnerPos.distSqr(level.getSharedSpawnPos()));
			maxSpawned = (int) ((disableSpawners$minSpawnableMobs + (distance / 8d)) * disableSpawners$spawnableMobsMultiplier);
		}

		if (spawnerCap.getSpawnedMobs() >= maxSpawned)
			setSpawnerDisabled(spawnerBlockEntity, true);
		spawnerBlockEntity.setChanged();
	}

	private static void empoweredSpawner(SpawnerBlockEntity spawnerBlockEntity, Mob mob, ServerLevel level, BlockPos spawnerPos, SpawnerDataImpl spawnerCap) {
		if (!empowered$enabled
				|| mob.getType().is(BLACKLISTED_SPAWNERS)
				|| !spawnerCap.isEmpowered())
			return;

		if (spawnerCap.getSpawnedMobs() >= empoweredMobsAmount) {
			setSpawnerEmpowered(spawnerBlockEntity, false);
			int amount = empoweredExperienceReward.getIntRandBetween(level.random);
			ExperienceOrb.award(level, new Vec3(spawnerPos.getX() + 0.5d, spawnerPos.getY() + 1.1d, spawnerPos.getZ() + 0.5d), amount);
			if (empoweredLootReward) {
				LootParams.Builder lootParamsBuilder = (new LootParams.Builder(level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(spawnerPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_STATE, level.getBlockState(spawnerPos)).withOptionalParameter(LootContextParams.BLOCK_ENTITY, spawnerBlockEntity);
				LootParams lootParams = lootParamsBuilder.create(LootContextParamSets.EMPTY);
				LootTable loottable = level.getServer().reloadableRegistries().getLootTable(EMPOWERED_SPAWNER_LOOT_TABLE);
				loottable.getRandomItems(lootParams).forEach(stack ->
						level.addFreshEntity(new ItemEntity(level, spawnerPos.getX() + 0.5f, spawnerPos.getY() + 1.1f, spawnerPos.getZ() + 0.5f, stack)));
			}
			if (empowered$disableOnEnd)
				setSpawnerDisabled(spawnerBlockEntity, true);
			if (empoweredSoundEffect)
				level.playSound(null, spawnerPos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 3.0f, 1.5f);
		}
	}

	@SubscribeEvent
	public void onItemUse(PlayerInteractEvent.RightClickBlock event) {
		if (!reEnableWithSpawnerReactivator
				|| !event.getItemStack().is(SPAWNER_REACTIVATOR_TAG)
				|| event.getLevel().getBlockState(event.getHitVec().getBlockPos()).getBlock() != Blocks.SPAWNER)
			return;

		SpawnerBlockEntity spawner = (SpawnerBlockEntity) event.getLevel().getBlockEntity(event.getHitVec().getBlockPos());
		if (spawner == null
				|| !isDisabled(spawner))
			return;

		event.setUseItem(TriState.TRUE);
		if (!event.getEntity().getAbilities().instabuild)
			event.getItemStack().shrink(1);
		event.getEntity().swing(event.getHand(), true);
		setSpawnerDisabled(spawner, false);
		SpawnerDataImpl spawnerCap = spawner.getData(Spawners.SPAWNER_DATA.get());
		spawnerCap.setSpawnedMobs(0);
		spawnerCap.setHasBeenReactivated(true);
		spawner.setChanged();
	}

	@SubscribeEvent
	public void onSpawnCheck(MobSpawnEvent.SpawnPlacementCheck event) {
		if (!this.isEnabled()
				|| event.getSpawnType() != MobSpawnType.SPAWNER
				|| event.getEntityType().is(BLACKLISTED_SPAWNERS)
				|| event.getDefaultResult()
			/*|| !(event.getEntityType() instanceof EntityType<? extends Monster>)*/)
			return;

		//noinspection unchecked
		if (Monster.checkAnyLightMonsterSpawnRules((EntityType<? extends Monster>) event.getEntityType(), event.getLevel(), event.getSpawnType(), event.getPos(), event.getRandom()))
			event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.SUCCEED);
	}

	@SubscribeEvent
	public void onSpawnCheck(MobSpawnEvent.PositionCheck event) {
		if (!this.isEnabled()
				|| event.getSpawnType() != MobSpawnType.SPAWNER
				|| event.getEntity().getType().is(BLACKLISTED_SPAWNERS)
				|| event.getSpawner() == null
				|| !(event.getSpawner().getOwner() != null && event.getSpawner().getOwner().left().orElse(null) instanceof SpawnerBlockEntity spawnerBlockEntity))
			return;
		if (hasBeenReactivated(spawnerBlockEntity)) {
			if (reactivatedSpawners$ignoreLight) {
				event.setResult(MobSpawnEvent.PositionCheck.Result.SUCCEED);
			}
		}
		else if (ignoreLight && event.getEntity().checkSpawnObstruction(event.getLevel())) {
			event.setResult(MobSpawnEvent.PositionCheck.Result.SUCCEED);
		}
	}

	/**
	 * Returns true if the spawner should not tick
	 */
	public static boolean onSpawnerServerTick(BaseSpawner spawner) {
		if (spawner.getOwner() == null
				|| !(spawner.getOwner().left().orElse(null) instanceof SpawnerBlockEntity spawnerBlockEntity)
				|| !Feature.isEnabled(Spawners.class))
			return false;
		return isDisabled(spawnerBlockEntity);
	}

	/**
	 * Returns true if the spawner should not tick
	 */
	public static boolean onSpawnerClientTick(BaseSpawner spawner, Level level) {
		if (!Feature.isEnabled(Spawners.class)
			|| spawner.getOwner() == null
			|| !(spawner.getOwner().left().orElse(null) instanceof SpawnerBlockEntity spawnerBlockEntity)
			|| ((BaseSpawnerAccessor) spawner).getNextSpawnData() == null)
			return false;
		Optional<EntityType<?>> optional = EntityType.by(((BaseSpawnerAccessor) spawner).getNextSpawnData().entityToSpawn());
		if (optional.isEmpty())
			return false;
		clientTickOnEmpowered(spawner, level, spawnerBlockEntity);
		clientTickOnDisabled(spawner, level, spawnerBlockEntity);
		return isDisabled(spawnerBlockEntity);
	}

	public static void onSpawnerDelaySet(BaseSpawner spawner, Level level, BlockPos pos) {
		if (!Feature.isEnabled(Spawners.class)
				|| spawner.getOwner() == null
				|| !(spawner.getOwner().left().orElse(null) instanceof SpawnerBlockEntity spawnerBlockEntity))
			return;
		int playerRange = requiredPlayerRange;
        if (isEmpowered(spawnerBlockEntity))
            ((BaseSpawnerAccessor) spawner).setSpawnDelay(empoweredDelay.getIntRandBetween(level.getRandom()));
		else if (hasBeenReactivated(spawnerBlockEntity)) {
			((BaseSpawnerAccessor) spawner).setSpawnDelay(reactivatedSpawners$delay.getIntRandBetween(level.getRandom()));
			playerRange = reactivatedSpawners$playerRange;
		}
        else if (overrideSpawnDelay)
            ((BaseSpawnerAccessor) spawner).setSpawnDelay(delay.getIntRandBetween(level.getRandom()));
		((BaseSpawnerAccessor) spawner).setRequiredPlayerRange(playerRange);
		syncSpawnerData(spawnerBlockEntity);
    }

	private static void clientTickOnEmpowered(BaseSpawner spawner, Level level, SpawnerBlockEntity spawnerBlockEntity) {
		if (!isEmpowered(spawnerBlockEntity))
			return;
		BlockPos blockpos = spawnerBlockEntity.getBlockPos();
		for (int i = 0; i < 5; i++)
			level.addParticle(ParticleTypes.FLAME, blockpos.getX() + level.random.nextDouble(), blockpos.getY() + level.random.nextDouble(), blockpos.getZ() + level.random.nextDouble(), 0.0D, 0.0D, 0.0D);
		if (((BaseSpawnerAccessor) spawner).getSpawnDelay() > 0 && ((BaseSpawnerAccessor) spawner).getSpawnDelay() % 10 == 0)
			level.addParticle(ParticleTypes.ANGRY_VILLAGER, blockpos.getX() + level.random.nextDouble(), blockpos.getY() + level.random.nextDouble() + 0.2f, blockpos.getZ() + level.random.nextDouble(), 0.0D, 0.0D, 0.0D);
	}

	private static void clientTickOnDisabled(BaseSpawner spawner, Level level, SpawnerBlockEntity spawnerBlockEntity) {
		if (!isDisabled(spawnerBlockEntity))
			return;
		BlockPos blockpos = spawnerBlockEntity.getBlockPos();
		for (int i = 0; i < 8; i++) {
			level.addParticle(ParticleTypes.SMOKE, blockpos.getX() + level.random.nextDouble(), blockpos.getY() + level.random.nextDouble(), blockpos.getZ() + level.random.nextDouble(), 0.0D, 0.0D, 0.0D);
		}
	}

	private static void setSpawnerEmpowered(SpawnerBlockEntity spawner, boolean empowered) {
		spawner.getData(Spawners.SPAWNER_DATA.get()).setEmpowered(empowered);
		spawner.setChanged();
		syncSpawnerData(spawner);
	}

	private static void setSpawnerDisabled(SpawnerBlockEntity spawner, boolean disabled) {
		spawner.getData(Spawners.SPAWNER_DATA.get()).setDisabled(disabled);
		spawner.setChanged();
		syncSpawnerData(spawner);
	}

	private static void syncSpawnerData(SpawnerBlockEntity spawner) {
		//noinspection ConstantConditions
        if (!spawner.hasLevel()
				|| spawner.getLevel().isClientSide)
            return;

		BaseSpawnerAccessor baseSpawner = (BaseSpawnerAccessor) spawner.getSpawner();
		SpawnerStatusSync msg = new SpawnerStatusSync(spawner.getBlockPos(), spawner.getData(Spawners.SPAWNER_DATA.get()), baseSpawner.getSpawnDelay(), baseSpawner.getRequiredPlayerRange());
		for (Player player : spawner.getLevel().players()) {
			PacketDistributor.sendToPlayer((ServerPlayer) player, msg);
		}
    }

	private static boolean isDisabled(SpawnerBlockEntity spawner) {
		return spawner.getData(Spawners.SPAWNER_DATA.get()).isDisabled();
	}

	private static boolean isEmpowered(SpawnerBlockEntity spawner) {
		return empowered$enabled && spawner.getData(Spawners.SPAWNER_DATA.get()).isEmpowered();
	}

	private static boolean hasBeenReactivated(SpawnerBlockEntity spawner) {
		return spawner.getData(Spawners.SPAWNER_DATA.get()).hasBeenReactivated();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !reEnableWithSpawnerReactivator
				|| !event.getItemStack().is(SPAWNER_REACTIVATOR_TAG))
			return;

		event.getToolTip().add(SPAWNER_REACTIVATOR_MESSAGE);
	}
}