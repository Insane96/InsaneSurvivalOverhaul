package insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.ISOMobEffectInstance;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.Packs;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.MinMax;
import insane96mcp.insanelib.data.IdTagValue;
import insane96mcp.insanelib.util.LogHelper;
import insane96mcp.insanelib.world.effect.ILMobEffect;
import insane96mcp.insanelib.world.scheduled.ScheduledTasks;
import insane96mcp.insanelib.world.scheduled.ScheduledTickTask;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@LoadFeature(module = Modules.Ids.SLEEP_RESPAWN, description = "Changes to respawning. Adds the doLooseRespawn gamerule that can disable the loose spawn range")
public class Respawn extends JsonFeature {
	public static final TagKey<Block> RESPAWN_OBELISK_BLOCKS_TO_ROT = ISOBlockTagsProvider.create("structures/respawn_obelisk/blocks_to_rot");
	public static final RegistryObject<MobEffect> GHOSTLY = ISORegistries.MOB_EFFECTS.register("ghostly", () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x857965, true));

	public static final String FAIL_RESPAWN_OBELISK_LANG = InsaneSO.MOD_ID + ".fail_respawn_obelisk";

	public static final String LOOSE_WORLD_RESPAWN_POINT = InsaneSO.lang("loose_bed_respawn_point");
	public static final String LOOSE_BED_RESPAWN_POINT = InsaneSO.lang("loose_bed_respawn_point");
	public static final GameRules.Key<GameRules.BooleanValue> RULE_RANGEDRESPAWN = GameRules.register("iguanatweaks:doLooseRespawn", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

	public static final SimpleBlockWithItem RESPAWN_OBELISK = SimpleBlockWithItem.register("respawn_obelisk", () -> new RespawnObeliskBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel(RespawnObeliskBlock::lightLevel)));

	@Config(min = 0, description = "The range from world spawn where players will respawn.")
	public static MinMax looseWorldSpawnRange = new MinMax(128d, 256d);
	@Config(min = 0, description = "The range from beds where players will respawn.")
	public static MinMax looseBedSpawnRange = new MinMax(80d, 160d);
	@Config(min = 0, description = "How many ticks of the Ghostly effect is given to the player on respawn.")
	public static Integer ghostlyEffect = 120;
	@Config(description = "If enabled, respawning will try to place you on land and not in fluids")
	public static Boolean dontRespawnOnFluid = true;

	@Config(description = "Data pack that makes respawn obelisks generate in the world")
	public static Boolean respawnObelisks = true;
    @Config(description = "If true, sleeping a bed when sneaking will overwrite obelisk spawn point")
    public static Boolean allowObeliskSpawnPointOverwriteWithBedsSneaking = true;
    @Config(description = "If true, respawn obelisks will require (and consume) precious blocks to let you respawn")
    public static Boolean respawnObelisksRequireCatalystBlocks = false;

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
		InsaneSO.addServerPack("respawn_obelisk", "Insane's Survival Overhaul Respawn Obelisk", () -> this.isEnabled() && !Packs.disableAllDataPacks && respawnObelisks);
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
	}


	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!this.isEnabled()
				|| event.isEndConquered())
			return;

		tryRespawnObelisk(event);
	}

	public static Optional<Vec3> tryLooseRespawn(ServerLevel level, ServerPlayer player) {
		if (!level.getGameRules().getBoolean(RULE_RANGEDRESPAWN))
			return Optional.empty();

		Optional<Vec3> newRespawn = looseWorldSpawn(level, player);
		if (newRespawn.isEmpty())
			newRespawn = looseBedSpawn(level, player);
		return newRespawn;
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
		ScheduledTasks.schedule(new ScheduledTickTask(2) {
			@Override
			public void run() {
				player.addEffect(new MobEffectInstance(GHOSTLY.get(), ghostlyEffect * 20, 0, false, false, true));
				player.sendSystemMessage(Component.translatable(LOOSE_WORLD_RESPAWN_POINT));
			}
		});
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

		ScheduledTasks.schedule(new ScheduledTickTask(2) {
			@Override
			public void run() {
				player.addEffect(new MobEffectInstance(GHOSTLY.get(), ghostlyEffect * 20, 0, false, false, true));
				player.sendSystemMessage(Component.translatable(LOOSE_BED_RESPAWN_POINT));
			}
		});
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
		player.displayClientMessage(Component.translatable(LOOSE_BED_RESPAWN_POINT), false);
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

	@SubscribeEvent
	public void onFollowRange(LivingEvent.LivingVisibilityEvent event) {
		if (!event.getEntity().hasEffect(GHOSTLY.get()))
			return;

		event.modifyVisibility(0d);
	}
}