package insane96mcp.iguanatweaksreborn.module.world.spawners;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.data.generator.ISTItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.world.spawners.capability.ISpawnerData;
import insane96mcp.iguanatweaksreborn.module.world.spawners.capability.SpawnerData;
import insane96mcp.iguanatweaksreborn.module.world.spawners.capability.SpawnerDataImpl;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import insane96mcp.iguanatweaksreborn.utils.ITRLogHelper;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.*;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.data.IdTagValue;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

@Label(name = "Spawners", description = "Spawners are now a challenge. Monsters spawning from spawners ignore light.")
@LoadFeature(module = Modules.Ids.WORLD)
public class Spawners extends JsonFeature {

	public static final TagKey<EntityType<?>> BLACKLISTED_SPAWNERS = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "blacklisted_spawners"));
	public static final TagKey<Item> SPAWNER_REACTIVATOR_TAG = ISTItemTagsProvider.create("spawner_reactivator");
	public static final String SPAWNER_REACTIVATOR = InsaneSurvivalOverhaul.MOD_ID + ".spawner_reactivator";

	@Config(min = 1)
	@Label(name = "Override Spawn Delay", description = "If true, the spawner delay is set to 'Delay' instead of using MinSpawnDelay and MaxSpawnDelay")
	public static Boolean overrideSpawnDelay = true;
	@Config(min = 1)
	@Label(name = "Delay", description = "Spawning Delay (in ticks) of the spawner. Vanilla is 200~800. Requires 'Override Spawn Delay' to be enabled.")
	public static MinMax delay = new MinMax(400, 1600);
	@Config(min = 0)
	@Label(name = "Required Players Range", description = "Range in which a player must be present for a spawner to work. Vanilla is 16.")
	public static int requiredPlayerRange = 24;
	@Config
	@Label(name = "Ignore Light", description = "If true, monsters from spawners will spawn no matter the light level.")
	public static Boolean ignoreLight = true;
	@Config
	@Label(name = "Re-enable with Spawner Reactivator", description = "If true, disabled spawners can be re-enabled with a spawner reactivator item defined in the `iguanatweaksreborn:spawner_reactivator` item tag. These items get a new tooltip mentioning that they can be used to re-enable spawners.")
	public static Boolean reEnableWithSpawnerReactivator = true;

	@Config
	@Label(name = "Disable spawners.Enabled")
	public static Boolean disableSpawnersEnabled = false;
	@Config(min = 0)
	@Label(name = "Disable spawners.Minimum Spawnable Mobs", description = "The minimum amount of spawnable mobs (when the spawner is basically in the same position as the world spawn). The amount of spawnable mobs before deactivating is equal to the distance divided by 8 (plus this value). E.g. At 160 blocks from spawn the max spawnable mobs will be 160 / 8 + 25 = 20 + 25 = 55")
	public static Integer disableSpawnersMinSpawnableMobs = 20;
	@Config(min = 0d)
	@Label(name = "Disable spawners.Spawnable mobs multiplier", description = "This multiplier increases the max mobs spawned.")
	public static Double disableSpawnersSpawnableMobsMultiplier = 1.0d;

	@Config
	@Label(name = "Empowered.Enabled", description = "If true, spawners will generate in an empowered state. When empowered, will generate mobs really fast for a while and then will slow down.")
	public static Boolean empoweredEnabled = true;
	@Config(min = 0)
	@Label(name = "Empowered.Disable on end", description = "When the spawner stops being empowered, the spawner is now disabled.")
	public static Boolean empoweredDisableOnEnd = true;
	@Config(min = 0)
	@Label(name = "Empowered.Mobs amount", description = "How many mobs are spawned before empowered ends.")
	public static Integer empoweredMobsAmount = 24;
	@Config(min = 1)
	@Label(name = "Empowered.Delay", description = "Spawning Delay (in ticks) when the Spawner is empowered.")
	public static MinMax empoweredDelay = new MinMax(150, 300);
	@Config(min = 0)
	@Label(name = "Empowered.Experience Reward", description = "When the Spawner stops being empowered, will generate this amount of experience")
	public static MinMax empoweredExperienceReward = new MinMax(150, 200);
	@Config
	@Label(name = "Empowered.Loot Reward", description = "When the Spawner stops being empowered, will generate loot from the iguanatweaksreborn:empowered_spawner loot table")
	public static Boolean empoweredLootReward = true;
	@Config
	@Label(name = "Empowered.Sound effect", description = "When the Spawner stops being empowered, will play a sound effect")
	public static Boolean empoweredSoundEffect = true;

	public static final ArrayList<IdTagValue> FIXED_SPAWNER_SPAWNABLE_DEFAULT = new ArrayList<>(List.of(
			//new IdTagValue(IdTagMatcher.newId("minecraft:blaze", "minecraft:the_nether"), 64)
	));
	public static final ArrayList<IdTagValue> fixedSpawnerSpawnable = new ArrayList<>();

	public Spawners(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("spawners.json", fixedSpawnerSpawnable, FIXED_SPAWNER_SPAWNABLE_DEFAULT, IdTagValue.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSurvivalOverhaul.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onSpawnerSpawn(MobSpawnEvent.FinalizeSpawn event) {
		if (!this.isEnabled()
				|| !event.getSpawnType().equals(MobSpawnType.SPAWNER)
				|| event.getSpawner() == null
				|| event.getSpawner().getSpawnerBlockEntity() == null)
			return;

		CompoundTag nbt = new CompoundTag();
		event.getSpawner().save(nbt);
		BlockPos spawnerPos = event.getSpawner().getSpawnerBlockEntity().getBlockPos();
		ServerLevel level = (ServerLevel) event.getLevel();
		if (!(event.getSpawner().getSpawnerBlockEntity() instanceof SpawnerBlockEntity mobSpawner)) {
			ITRLogHelper.warn("SpawnerBlockEntity is null at %s. Some mod is giving a spawner a non SpawnerBlockEntity.".formatted(spawnerPos));
			return;
		}
		mobSpawner.getCapability(SpawnerData.INSTANCE).ifPresent(spawnerCap -> {
			spawnerCap.addSpawnedMobs(1);
			disabledSpawners(mobSpawner, event.getEntity(), level, spawnerPos, spawnerCap);
			empoweredSpawner(mobSpawner, event.getEntity(), level, spawnerPos, spawnerCap);
		});
	}

	private void disabledSpawners(SpawnerBlockEntity spawnerBlockEntity, Mob mob, ServerLevel level, BlockPos spawnerPos, ISpawnerData spawnerCap) {
        if (!disableSpawnersEnabled
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
			maxSpawned = (int) ((disableSpawnersMinSpawnableMobs + (distance / 8d)) * disableSpawnersSpawnableMobsMultiplier);
		}

		if (spawnerCap.getSpawnedMobs() >= maxSpawned)
			setSpawnerDisabled(spawnerBlockEntity, true);
		spawnerBlockEntity.setChanged();
	}

	private static void empoweredSpawner(SpawnerBlockEntity spawnerBlockEntity, Mob mob, ServerLevel level, BlockPos spawnerPos, ISpawnerData spawnerCap) {
		if (!empoweredEnabled
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
				LootTable loottable = level.getServer().getLootData().getLootTable(new ResourceLocation(InsaneSurvivalOverhaul.RESOURCE_PREFIX + "empowered_spawner"));
				loottable.getRandomItems(lootParams).forEach(stack ->
						level.addFreshEntity(new ItemEntity(level, spawnerPos.getX() + 0.5f, spawnerPos.getY() + 1.1f, spawnerPos.getZ() + 0.5f, stack)));
			}
			if (empoweredDisableOnEnd)
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
		resetSpawner(spawner);
	}

	@SubscribeEvent
	public void onSpawnCheck(MobSpawnEvent.SpawnPlacementCheck event) {
		if (!this.isEnabled()
				|| !ignoreLight
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
				|| !ignoreLight
				|| event.getSpawnType() != MobSpawnType.SPAWNER
				|| event.getEntity().getType().is(BLACKLISTED_SPAWNERS))
			return;

		if (event.getEntity().checkSpawnObstruction(event.getLevel()))
			event.setResult(Event.Result.ALLOW);
	}

	/**
	 * Returns true if the spawner should not tick
	 */
	public static boolean onSpawnerServerTick(BaseSpawner spawner) {
        if (!(spawner.getSpawnerBlockEntity() instanceof SpawnerBlockEntity spawnerBlockEntity)
                || !Feature.isEnabled(Spawners.class))
			return false;
		/*spawnerBlockEntity.getCapability(SpawnerData.INSTANCE).ifPresent(spawnerData -> {
			Level level = spawnerBlockEntity.getLevel();
			if (level == null
					|| !spawner.isNearPlayer(level, spawnerBlockEntity.getBlockPos()))
				return;
			if (spawnerData.isEmpowered())
				spawner.spawnDelay = Math.max(spawner.spawnDelay - (MathHelper.getAmountWithDecimalChance(level.getRandom(), empoweredSpawningSpeed) - 1), 0);
			else if (empoweredNormalSpeed > 1)
				spawner.spawnDelay = Math.max(spawner.spawnDelay - (MathHelper.getAmountWithDecimalChance(level.getRandom(), empoweredNormalSpeed) - 1), 0);
			else if (empoweredNormalSpeed < 1 && level.getRandom().nextFloat() >= empoweredNormalSpeed){
				spawner.spawnDelay = Math.max(spawner.spawnDelay + 1, 0);
			}
		});*/
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
        if (isEmpowered(spawnerBlockEntity))
            spawner.spawnDelay = empoweredDelay.getIntRandBetween(level.getRandom());
        else if (overrideSpawnDelay)
            spawner.spawnDelay = delay.getIntRandBetween(level.getRandom());
		spawner.requiredPlayerRange = requiredPlayerRange;
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

	private static void resetSpawner(SpawnerBlockEntity spawner) {
		setSpawnerDisabled(spawner, false);
		spawner.getCapability(SpawnerData.INSTANCE).ifPresent(spawnerCap -> spawnerCap.setSpawnedMobs(0));
		spawner.setChanged();
	}

	private static boolean isDisabled(SpawnerBlockEntity spawner) {
		LazyOptional<ISpawnerData> cap = spawner.getCapability(SpawnerData.INSTANCE);
		return cap.map(ISpawnerData::isDisabled).orElse(false);
	}

	private static boolean isEmpowered(SpawnerBlockEntity spawner) {
		LazyOptional<ISpawnerData> cap = spawner.getCapability(SpawnerData.INSTANCE);
		return cap.map(ISpawnerData::isEmpowered).orElse(false);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !reEnableWithSpawnerReactivator
				|| !event.getItemStack().is(SPAWNER_REACTIVATOR_TAG))
			return;

		event.getToolTip().add(Component.translatable(SPAWNER_REACTIVATOR).withStyle(ChatFormatting.LIGHT_PURPLE));
	}
}