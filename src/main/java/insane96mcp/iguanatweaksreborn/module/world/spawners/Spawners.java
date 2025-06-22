package insane96mcp.iguanatweaksreborn.module.world.spawners;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.world.spawners.capability.ISpawnerData;
import insane96mcp.iguanatweaksreborn.module.world.spawners.capability.SpawnerData;
import insane96mcp.iguanatweaksreborn.module.world.spawners.capability.SpawnerDataImpl;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import insane96mcp.iguanatweaksreborn.utils.ISOLogHelper;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.data.IdTagValue;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@LoadFeature(module = Modules.Ids.WORLD, description = "Spawners are now a challenge. Monsters spawning from spawners ignore light.")
public class Spawners extends JsonFeature {

	public static final TagKey<EntityType<?>> BLACKLISTED_SPAWNERS = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.location("blacklisted_spawners"));
	public static final TagKey<Item> SPAWNER_REACTIVATOR_TAG = ISOItemTagsProvider.create("spawner_reactivator");
	public static final String SPAWNER_REACTIVATOR_LANG = InsaneSO.lang("spawner_reactivator");

	@Config(min = 1, description = "If true, the spawner delay is set to 'Delay' instead of using MinSpawnDelay and MaxSpawnDelay")
	public static Boolean overrideSpawnDelay = true;
	@Config(min = 1, description = "Spawning Delay (in ticks) of the spawner. Vanilla is 200~800. Requires 'Override Spawn Delay' to be enabled.")
	public static MinMax delay = new MinMax(400, 1600);
	@Config(min = 0, description = "Range in which a player must be present for a spawner to work. Vanilla is 16.")
	public static int requiredPlayerRange = 24;
	@Config(description = "If true, monsters from spawners will spawn no matter the light level.")
	public static Boolean ignoreLight = true;
	@Config(name = "Re-enable with Spawner Reactivator", description = "If true, disabled spawners can be re-enabled with a spawner reactivator item defined in the `iguanatweaksreborn:spawner_reactivator` item tag. These items get a new tooltip mentioning that they can be used to re-enable spawners.")
	public static Boolean reEnableWithSpawnerReactivator = true;
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
	public static MinMax empoweredDelay = new MinMax(150, 300);
	@Config(min = 0, name = "Empowered.Experience Reward", description = "When the Spawner stops being empowered, will generate this amount of experience")
	public static MinMax empoweredExperienceReward = new MinMax(150, 200);
	@Config(name = "Empowered.Loot Reward", description = "When the Spawner stops being empowered, will generate loot from the iguanatweaksreborn:empowered_spawner loot table")
	public static Boolean empoweredLootReward = true;
	@Config(name = "Empowered.Sound effect", description = "When the Spawner stops being empowered, will play a sound effect")
	public static Boolean empoweredSoundEffect = true;

	@Config(min = -1, description = "The range of a player nearby in which the spawner will be active. Setting to -1 should keep the spawners always active (untested) as long as the chunk is loaded (hostiles will still despawn in a 128 block radius).")
	public static Integer reactivatedSpawners$playerRange = 128;
	@Config(description = "How many ticks will the spawner try to summon mobs.")
	public static MinMax reactivatedSpawners$delay = new MinMax(3600, 6000);
	@Config(description = "Reactivated spawners spawn mobs no matter the light level.")
	public static Boolean reactivatedSpawners$ignoreLight = false;

	public static final ArrayList<IdTagValue> FIXED_SPAWNER_SPAWNABLE_DEFAULT = new ArrayList<>(List.of(
			//new IdTagValue(IdTagMatcher.newId("minecraft:blaze", "minecraft:the_nether"), 64)
	));
	public static final ArrayList<IdTagValue> fixedSpawnerSpawnable = new ArrayList<>();

	public Spawners(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("fixed_spawners_spawnable.json", fixedSpawnerSpawnable, FIXED_SPAWNER_SPAWNABLE_DEFAULT, IdTagValue.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onSpawnerSpawn(MobSpawnEvent.FinalizeSpawn event) {
		if (!this.isEnabled()
				|| !event.getSpawnType().equals(MobSpawnType.SPAWNER)
				|| event.getSpawner() == null
				|| event.getSpawner().getSpawnerBlockEntity() == null)
			return;

		BlockPos spawnerPos = event.getSpawner().getSpawnerBlockEntity().getBlockPos();
		ServerLevel level = (ServerLevel) event.getLevel();
		if (!(event.getSpawner().getSpawnerBlockEntity() instanceof SpawnerBlockEntity mobSpawner)) {
			ISOLogHelper.warn("SpawnerBlockEntity is null at %s. Some mod is giving a spawner a non SpawnerBlockEntity.".formatted(spawnerPos));
			return;
		}
		if (spawningSoundEffect)
			level.playSound(null, spawnerPos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 2f, 1.5f);
		mobSpawner.getCapability(SpawnerData.INSTANCE).ifPresent(spawnerCap -> {
			spawnerCap.addSpawnedMobs(1);
			disabledSpawners(mobSpawner, event.getEntity(), level, spawnerPos, spawnerCap);
			empoweredSpawner(mobSpawner, event.getEntity(), level, spawnerPos, spawnerCap);
		});
	}

	private void disabledSpawners(SpawnerBlockEntity spawnerBlockEntity, Mob mob, ServerLevel level, BlockPos spawnerPos, ISpawnerData spawnerCap) {
        if (!disableSpawners$enabled
				|| mob.getType().is(BLACKLISTED_SPAWNERS))
			return;

        int maxSpawned = 0;
		for (IdTagValue idTagValue : fixedSpawnerSpawnable) {
			if (idTagValue.id.matchesEntity(mob, level.dimension().location())) {
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

	private static void empoweredSpawner(SpawnerBlockEntity spawnerBlockEntity, Mob mob, ServerLevel level, BlockPos spawnerPos, ISpawnerData spawnerCap) {
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
				LootTable loottable = level.getServer().getLootData().getLootTable(InsaneSO.location("empowered_spawner"));
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

		event.setUseItem(Event.Result.ALLOW);
		if (!event.getEntity().getAbilities().instabuild)
			event.getItemStack().shrink(1);
		event.getEntity().swing(event.getHand(), true);
		setSpawnerDisabled(spawner, false);
		spawner.getCapability(SpawnerData.INSTANCE).ifPresent(spawnerCap -> {
			spawnerCap.setSpawnedMobs(0);
			spawnerCap.setHasBeenReactivated(true);
		});
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
			event.setResult(Event.Result.ALLOW);
	}

	@SubscribeEvent
	public void onSpawnCheck(MobSpawnEvent.PositionCheck event) {
		if (!this.isEnabled()
				|| event.getSpawnType() != MobSpawnType.SPAWNER
				|| event.getEntity().getType().is(BLACKLISTED_SPAWNERS)
				|| event.getSpawner() == null
				|| !(event.getSpawner().getSpawnerBlockEntity() instanceof SpawnerBlockEntity spawnerBlockEntity))
			return;
		if (hasBeenReactivated(spawnerBlockEntity)) {
			if (reactivatedSpawners$ignoreLight) {
				event.setResult(Event.Result.ALLOW);
			}
		}
		else if (ignoreLight && event.getEntity().checkSpawnObstruction(event.getLevel())) {
			event.setResult(Event.Result.ALLOW);
		}
	}

	/**
	 * Returns true if the spawner should not tick
	 */
	public static boolean onSpawnerServerTick(BaseSpawner spawner) {
        if (!(spawner.getSpawnerBlockEntity() instanceof SpawnerBlockEntity spawnerBlockEntity)
                || !Feature.isEnabled(Spawners.class))
			return false;
		return isDisabled(spawnerBlockEntity);
	}

	/**
	 * Returns true if the spawner should not tick
	 */
	public static boolean onSpawnerClientTick(BaseSpawner spawner, Level level) {
		if (!Feature.isEnabled(Spawners.class)
			|| !(spawner.getSpawnerBlockEntity() instanceof SpawnerBlockEntity spawnerBlockEntity)
			|| spawner.nextSpawnData == null)
			return false;
		Optional<EntityType<?>> optional = EntityType.by(spawner.nextSpawnData.entityToSpawn());
		if (optional.isEmpty())
			return false;
		clientTickOnEmpowered(spawner, level, spawnerBlockEntity);
		clientTickOnDisabled(spawner, level, spawnerBlockEntity);
		return isDisabled(spawnerBlockEntity);
	}

	public static void onSpawnerDelaySet(BaseSpawner spawner, Level level, BlockPos pos) {
		if (!Feature.isEnabled(Spawners.class)
				|| !(spawner.getSpawnerBlockEntity() instanceof SpawnerBlockEntity spawnerBlockEntity))
			return;
		int playerRange = requiredPlayerRange;
        if (isEmpowered(spawnerBlockEntity))
            spawner.spawnDelay = empoweredDelay.getIntRandBetween(level.getRandom());
		else if (hasBeenReactivated(spawnerBlockEntity)) {
			spawner.spawnDelay = reactivatedSpawners$delay.getIntRandBetween(level.getRandom());
			playerRange = reactivatedSpawners$playerRange;
		}
        else if (overrideSpawnDelay)
            spawner.spawnDelay = delay.getIntRandBetween(level.getRandom());
		spawner.requiredPlayerRange = playerRange;
		syncSpawnerData(spawnerBlockEntity);
    }

	private static void clientTickOnEmpowered(BaseSpawner spawner, Level level, SpawnerBlockEntity spawnerBlockEntity) {
		if (!isEmpowered(spawnerBlockEntity))
			return;
		BlockPos blockpos = spawnerBlockEntity.getBlockPos();
		for (int i = 0; i < 5; i++)
			level.addParticle(ParticleTypes.FLAME, blockpos.getX() + level.random.nextDouble(), blockpos.getY() + level.random.nextDouble(), blockpos.getZ() + level.random.nextDouble(), 0.0D, 0.0D, 0.0D);
		if (spawner.spawnDelay > 0 && spawner.spawnDelay % 10 == 0)
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
		spawner.getCapability(SpawnerData.INSTANCE).ifPresent(spawnerCap -> spawnerCap.setEmpowered(empowered));
		spawner.setChanged();
		syncSpawnerData(spawner);
	}

	private static void setSpawnerDisabled(SpawnerBlockEntity spawner, boolean disabled) {
		spawner.getCapability(SpawnerData.INSTANCE).ifPresent(spawnerCap -> spawnerCap.setDisabled(disabled));
		spawner.setChanged();
		syncSpawnerData(spawner);
	}

	private static void syncSpawnerData(SpawnerBlockEntity spawner) {
		//noinspection ConstantConditions
        if (!spawner.hasLevel()
				|| spawner.getLevel().isClientSide)
            return;

        LazyOptional<ISpawnerData> spawnerDataLazy = spawner.getCapability(SpawnerData.INSTANCE);
		spawnerDataLazy.ifPresent(spawnerData -> {
			Object msg = new SpawnerStatusSync(spawner.getBlockPos(), (SpawnerDataImpl) spawnerData, spawner.getSpawner().spawnDelay, spawner.getSpawner().requiredPlayerRange);
			for (Player player : spawner.getLevel().players()) {
				NetworkHandler.CHANNEL.sendTo(msg, ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			}
		});
    }

	private static boolean isDisabled(SpawnerBlockEntity spawner) {
		LazyOptional<ISpawnerData> cap = spawner.getCapability(SpawnerData.INSTANCE);
		return cap.map(ISpawnerData::isDisabled).orElse(false);
	}

	private static boolean isEmpowered(SpawnerBlockEntity spawner) {
		if (!empowered$enabled)
			return false;
		LazyOptional<ISpawnerData> cap = spawner.getCapability(SpawnerData.INSTANCE);
		return cap.map(ISpawnerData::isEmpowered).orElse(false);
	}

	private static boolean hasBeenReactivated(SpawnerBlockEntity spawner) {
		LazyOptional<ISpawnerData> cap = spawner.getCapability(SpawnerData.INSTANCE);
		return cap.map(ISpawnerData::hasBeenReactivated).orElse(false);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !reEnableWithSpawnerReactivator
				|| !event.getItemStack().is(SPAWNER_REACTIVATOR_TAG))
			return;

		event.getToolTip().add(Component.translatable(SPAWNER_REACTIVATOR_LANG).withStyle(ChatFormatting.LIGHT_PURPLE));
	}
}