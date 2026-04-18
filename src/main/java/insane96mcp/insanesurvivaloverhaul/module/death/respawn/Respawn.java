package insane96mcp.insanesurvivaloverhaul.module.death.respawn;

import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import insane96mcp.insanelib.data.SerializableMobEffectInstance;
import insane96mcp.insanelib.world.effect.ILMobEffect;
import insane96mcp.insanelib.world.scheduled.ScheduledTasks;
import insane96mcp.insanelib.world.scheduled.ScheduledTickTask;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.criterion.ActivateEchoPillarTrigger;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOBlockTagsProvider;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@LoadFeature(module = ISOModules.DEATH, description = "Changes to respawning. Adds the insanesurvivaloverhaul:do_loose_respawn game rule that can disable the loose spawn range, which is disabled by default")
public class Respawn extends JsonFeature {
	public static final TagKey<Block> ECHO_PILLAR_BLOCKS_TO_ROT = ISOBlockTagsProvider.create("structures/echo_pillar/blocks_to_rot");
	public static final TagKey<Item> ECHO_PILLAR_CATALYST = ISOItemTagsProvider.create("echo_pillar_catalyst");
	public static final MutableComponent ECHO_PILLAR_CATALYST_MESSAGE = Component.translatable(InsaneSO.lang("echo_pillar_catalyst")).withStyle(ChatFormatting.DARK_PURPLE);
	public static final MutableComponent FAIL_ECHO_PILLAR_MESSAGE = Component.translatable(InsaneSO.lang("fail_echo_pillar"));

	public static final DeferredHolder<MobEffect, ILMobEffect> GHOSTLY = ISORegistries.MOB_EFFECTS.register("ghostly", () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x857965, true));

	public static final String LOOSE_WORLD_RESPAWN_POINT = InsaneSO.lang("loose_world_respawn_point");
	public static final String LOOSE_BED_RESPAWN_POINT = InsaneSO.lang("loose_bed_respawn_point");
	public static final GameRules.Key<GameRules.BooleanValue> RULE_RANGEDRESPAWN = GameRules.register(InsaneSO.MOD_ID + ":do_loose_respawn", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

	public static final Supplier<ActivateEchoPillarTrigger> ACTIVATE_ECHO_PILLAR =
			ISORegistries.registerTrigger("activate_echo_pillar", ActivateEchoPillarTrigger::new);

	public static final SimpleBlockWithItem ECHO_PILLAR = SimpleBlockWithItem.register("echo_pillar", () -> new EchoPillarBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel(EchoPillarBlock::lightLevel)));

	@Config(min = 0, description = "The range from world spawn where players will respawn.")
	public static MinMaxConfig looseWorldSpawnRange = new MinMaxConfig(128d, 256d);
	@Config(min = 0, description = "The range from beds where players will respawn.")
	public static MinMaxConfig looseBedSpawnRange = new MinMaxConfig(80d, 160d);
    @Config(min = 0, description = "How many seconds of the Ghostly effect is given to the player on respawn.")
    public static Integer ghostlyEffect = 120;
	@Config(description = "If enabled, respawning will try to place you on land and not in fluids")
	public static Boolean dontRespawnOnFluid = true;

	@Config(description = "Data pack that makes Echo Pillars generate in the world")
	public static Boolean echoPillars = true;

	public static final List<SerializableMobEffectInstance> ECHO_PILLAR_EFFECTS_DEFAULT = List.of(
			new SerializableMobEffectInstance.Builder(MobEffects.REGENERATION, 45 * 20)
					.noParticles()
					.build(),
			new SerializableMobEffectInstance.Builder(MobEffects.ABSORPTION, 60 * 20)
					.setAmplifier(1)
					.noParticles()
					.build(),
			new SerializableMobEffectInstance.Builder(MobEffects.MOVEMENT_SPEED, 60 * 20)
					.noParticles()
					.build(),
            new SerializableMobEffectInstance.Builder(MobEffects.SATURATION, 20 * 20)
                    .noParticles()
                    .build(),
            new SerializableMobEffectInstance.Builder(GHOSTLY, 120 * 20)
                    .noParticles()
                    .build()
	);

	public static final ArrayList<SerializableMobEffectInstance> echoPillarEffects = new ArrayList<>();

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		getJsonConfigs().add(new JsonFeature.JsonConfig<>("echo_pillar_effects.json", echoPillarEffects, ECHO_PILLAR_EFFECTS_DEFAULT, SerializableMobEffectInstance.LIST_TYPE));
		InsaneSO.addServerPack("echo_pillar", "Insane's Survival Overhaul Echo Pillar", () -> this.isEnabled() && !Packs.disableAllDataPacks && echoPillars);
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onPlayerRespawnPosition(PlayerRespawnPositionEvent event) {
		if (!this.isEnabled() || event.isFromEndFight())
			return;

		ServerPlayer player = (ServerPlayer) event.getEntity();
		DimensionTransition dt = event.getDimensionTransition();
		Optional<Vec3> newPos = tryLooseRespawn(dt.newLevel(), player);
		newPos.ifPresent(pos -> event.setDimensionTransition(new DimensionTransition(dt.newLevel(), pos, dt.speed(), dt.yRot(), dt.xRot(), dt.postDimensionTransition())));
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!this.isEnabled()
				|| event.isEndConquered())
			return;

		tryEchoPillar(event);
	}

	public static Optional<Vec3> tryLooseRespawn(ServerLevel level, ServerPlayer player) {
		if (!Feature.isEnabled(Respawn.class)
				|| !level.getGameRules().getBoolean(RULE_RANGEDRESPAWN))
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
				player.addEffect(new MobEffectInstance(GHOSTLY, ghostlyEffect * 20, 0, false, false, true));
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
				player.addEffect(new MobEffectInstance(GHOSTLY, ghostlyEffect * 20, 0, false, false, true));
			}
		});
		return Optional.of(new Vec3(respawnPos.getX() + 0.5d, respawnPos.getY() + 0.5d, respawnPos.getZ() + 0.5d));
	}

	@Nullable
	private static BlockPos getSpawnPositionInRange(BlockPos center, MinMaxConfig minMax, Level level, RandomSource random) {
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
			InsaneSO.LOGGER.warn("Failed to find a respawn point within {}", center);
			return null;
		}

		return respawn.immutable();
	}

	private void tryEchoPillar(PlayerEvent.PlayerRespawnEvent event) {
		ServerPlayer player = (ServerPlayer) event.getEntity();
		BlockPos pos = player.getRespawnPosition();
		if (pos == null
				|| !player.level().getBlockState(pos).is(ECHO_PILLAR.block().get()))
			return;

		if (!player.level().getBlockState(pos).getValue(EchoPillarBlock.ENABLED)) {
			player.sendSystemMessage(FAIL_ECHO_PILLAR_MESSAGE);
			player.setRespawnPosition(Level.OVERWORLD, null, 0f, false, false);
			return;
		}
		EchoPillarBlock.onPillarRespawn(player, player.level(), pos);
	}

	@SubscribeEvent
	public void onSetRespawn(PlayerSetSpawnEvent event) {
		if (!this.isEnabled()
				|| event.isForced())
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

	@SubscribeEvent
	public void onFollowRange(LivingEvent.LivingVisibilityEvent event) {
		if (!event.getEntity().hasEffect(GHOSTLY))
			return;

		event.modifyVisibility(-256d);
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !event.getItemStack().is(ECHO_PILLAR_CATALYST))
			return;

		event.getToolTip().add(ECHO_PILLAR_CATALYST_MESSAGE);
	}
}