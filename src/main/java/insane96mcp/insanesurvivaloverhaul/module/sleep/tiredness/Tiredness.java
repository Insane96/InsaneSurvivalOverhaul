package insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness;

import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import insane96mcp.insanelib.data.ObjTag;
import insane96mcp.insanelib.event.PlayerExhaustionEvent;
import insane96mcp.insanelib.world.effect.ILMobEffect;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.LivingEntityAccessor;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.MobAccessor;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.ServerLevelAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.world.thunderstorms.Thunderstorms;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@LoadFeature(module = ISOModules.SLEEP, description = "Prevents sleeping if the player is not tired. Tiredness is gained by gaining exhaustion. Allows you to sleep during daytime if too tired. Energy Boost Items are controlled via json in this feature's folder")
public class Tiredness extends JsonFeature {
	public static ResourceLocation NBT_TAG;

	public static final DeferredHolder<MobEffect, MobEffect> TIRED = ISORegistries.MOB_EFFECTS.register("tired", () -> new TirednessEffect(MobEffectCategory.HARMFUL, 0x818894)
			.addAttributeModifier(Attributes.MOVEMENT_SPEED, InsaneSO.location("tiredness/effect"), -0.04F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
			.addAttributeModifier(Attributes.ATTACK_SPEED, InsaneSO.location("tiredness/effect"), -0.04F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	public static final DeferredHolder<MobEffect, ILMobEffect> ENERGY_BOOST = ISORegistries.MOB_EFFECTS.register("energy_boost", () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x857965));

	public static final String NOT_TIRED = InsaneSO.lang("not_tired");
	public static final String TIRED_ENOUGH = InsaneSO.lang("tired_enough");
	public static final String TOO_TIRED = InsaneSO.lang("too_tired");
	public static final TagKey<Item> ENERGY_BOOST_ITEM_TAG = ISOItemTagsProvider.create("energy_boost");

	public static final List<EnergyBoostItem> ENERGY_BOOST_ITEMS_DEFAULT = new ArrayList<>(List.of(
			new EnergyBoostItem(ObjTag.of("#insanesurvivaloverhaul:energy_boost", Registries.ITEM), 0, 0),
			new EnergyBoostItem(ObjTag.of("#atmospheric:orange_pudding", Registries.ITEM), 0, 0),
			new EnergyBoostItem(ObjTag.of("farmersdelight:hot_cocoa", Registries.ITEM), 80, 0)
	));
	public static final List<EnergyBoostItem> energyBoostItems = new ArrayList<>();

	@Config(min = 0d, max = 128d, description = "Multiply the tiredness gained by this value. Normally you gain tiredness equal to the exhaustion gained. 'Effective Hunger' doesn't affect the exhaustion gained.")
	public static Double tirednessGainMultiplier = 1d;
	@Config(description = "If true the player will not set the spawn point if can't sleep.")
	public static Boolean shouldPreventSpawnPoint = false;
	@Config(min = 0d, description = "Tiredness required to get the Tired effect and be able to sleep.")
	public static Double tirednessToEffect = 400d;
	@Config(min = 0d, description = "Every this Tiredness above 'Tiredness for effect' will add a new level of Tired.")
	public static Double tirednessPerLevel = 250d;
	@Config(min = 0d, description = "If the player has energy boost, reduce tiredness by this value (multiplied by the effect level) each tick.")
	public static Double energyBoost$tirednessReduction = 0.025d;
	@Config(min = 0d, description = "By default if omitted in the json, food items will give 1 second of Energy Boost per effectiveness (hunger + saturation) of the food. This multiplies the duration of the effect")
	public static Double energyBoost$durationMultiplier = 5d;
	@Config(description = """
			What to do with tiredness when the player dies.
			RESET resets the tiredness to 0
			KEEP keeps the current tiredness
			SET_AT_EFFECT keeps the current tiredness but if higher than 'Tiredness to effect' it's set to that
			REMOVE_ONE_LEVEL keeps the current tiredness but if higher than 'Tiredness to effect' removes one level of Tired to a minimum of I""")
	public static OnDeath onDeathBehaviour = OnDeath.SET_AT_EFFECT;
	@Config(description = "List of mobs (and optional dimension where they should play) that will have their ambience sound played when the player is tired")
	public static List<String> fakeSound$mobs = List.of("minecraft:skeleton,minecraft:overworld", "minecraft:zombie,minecraft:overworld", "minecraft:spider,minecraft:overworld", "minecraft:ghast,minecraft:the_nether", "minecraft:zombified_piglin,minecraft:the_nether");
	public record FakeSoundMob(ObjTag<EntityType<?>> entity, @javax.annotation.Nullable ResourceLocation dimension) {
		boolean matchesDimension(ResourceLocation dim) {
			return this.dimension == null || this.dimension.equals(dim);
		}
	}
	public static List<FakeSoundMob> fakeSoundMobs = new ArrayList<>();
	@Config(min = 0, description = "The cooldown (in ticks) between choosing a mob to play the fake sound. This is reduced with higher Tired effect levels")
	public static MinMaxConfig fakeSound$cooldownBetweenMobs = new MinMaxConfig(12000, 24000);
	@Config(min = 0, description = "How many times will a fake sound of a mob play before going into cooldown")
	public static MinMaxConfig fakeSound$times = new MinMaxConfig(2, 6);
	@Config(description = "Phantoms will no longer spawn based on insomnia, but instead based off tiredness. Will spawn with Tired III.")
	public static Boolean tiredTiedPhantoms = true;
    @Config
    public static Boolean allowSleepingEvenWhenNotTired = false;
    @Config(description = "If true, sets the playersSleepingPercentage gamerule to 1 so tired players can sleep no matter how many players on a server.")
    public static Boolean forceOnePlayerSleep = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		getJsonConfigs().add(new JsonConfig<>("energy_boost_items.json", energyBoostItems, ENERGY_BOOST_ITEMS_DEFAULT, EnergyBoostItem.LIST_TYPE));
		NBT_TAG = this.createDataKey("tiredness");
	}

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
		fakeSoundMobs = fakeSound$mobs.stream().map(line -> {
			String[] parts = line.split(",");
			ObjTag<EntityType<?>> entity = ObjTag.of(parts[0].trim(), Registries.ENTITY_TYPE);
			ResourceLocation dimension = parts.length > 1 ? ResourceLocation.parse(parts[1].trim()) : null;
			return new FakeSoundMob(entity, dimension);
		}).collect(Collectors.toList());
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
	}

	public enum OnDeath {
		RESET,
		KEEP,
		SET_AT_EFFECT,
		REMOVE_ONE_LEVEL
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide)
			return;

		ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
		tickEnergyBoostEffect(serverPlayer);
	}

	private static long fakeSoundCooldown = 1200;
	private static long fakeSoundTimesToPlay = 0;
	private static long ambientSoundTime = 0;
	private static Mob mobFakeSound;
	private static BlockPos fakeMobPos;

	@SubscribeEvent
	public void onClientPlayerTick(PlayerTickEvent.Post event) {
		if (!this.isEnabled()
				|| fakeSoundMobs.isEmpty()
				//|| event.player.tickCount % 20 != 13
				|| !event.getEntity().level().isClientSide
				|| !event.getEntity().hasEffect(TIRED))
			return;

        if (--fakeSoundCooldown > 0)
            return;

		RandomSource random = event.getEntity().getRandom();
		int amplifier = event.getEntity().getEffect(TIRED).getAmplifier() + 1;
		if (mobFakeSound == null) {
			List<FakeSoundMob> filtered = fakeSoundMobs.stream()
					.filter(m -> m.matchesDimension(event.getEntity().level().dimension().location()))
					.toList();
			if (filtered.isEmpty()) {
				resetMobFakeSound(random, amplifier);
				return;
			}
			FakeSoundMob fakeSoundMob = filtered.get(random.nextInt(filtered.size()));
			Holder<EntityType<?>> entityHolder = fakeSoundMob.entity().asHolder();
			if (entityHolder == null) {
				resetMobFakeSound(random, amplifier);
				return;
			}
			Entity entity = entityHolder.value().create(event.getEntity().level());
			if (!(entity instanceof Mob)) {
				InsaneSO.LOGGER.warn("Can't play fake sound, {} is not an instance of Mob", entity);
				resetMobFakeSound(random, amplifier);
				return;
			}
			mobFakeSound = (Mob) entity;
			fakeSoundTimesToPlay = (int) (random.triangle(fakeSound$times.min, fakeSound$times.max + 1));
			fakeMobPos = event.getEntity().blockPosition();
		}
		if (mobFakeSound != null && random.nextInt(1000) < ambientSoundTime++) {
			SoundEvent soundEvent = ((MobAccessor)mobFakeSound).ambientSound();
			event.getEntity().level().playSound(event.getEntity(),
					fakeMobPos.getX() + getRandomRange(random),
					fakeMobPos.getY() + getRandomRange(random),
					fakeMobPos.getZ() + getRandomRange(random),
					soundEvent,
					mobFakeSound.getSoundSource(),
					((LivingEntityAccessor)mobFakeSound).soundVolume(),
					((LivingEntityAccessor)mobFakeSound).voicePitch());
			ambientSoundTime = -mobFakeSound.getAmbientSoundInterval();
			fakeSoundTimesToPlay--;
			if (fakeSoundTimesToPlay <= 0)
				resetMobFakeSound(random, amplifier);
		}
	}

	private static double getRandomRange(RandomSource random) {
		double randomRange = random.nextFloat() * 24d - 12d;
		if (randomRange > -6 && randomRange < 0)
			randomRange = -6;
		else if (randomRange >= 0 && randomRange < 6)
			randomRange = 0;
		return randomRange;
	}

	private void resetMobFakeSound(RandomSource random, int reduction) {
		fakeSoundTimesToPlay = 0;
		fakeSoundCooldown = (long) (fakeSound$cooldownBetweenMobs.getIntRandBetween(random) * (1f - 0.15f * reduction));
		mobFakeSound = null;
	}

	private void tickEnergyBoostEffect(ServerPlayer player) {
		if (!player.hasEffect(ENERGY_BOOST))
			return;

		//noinspection ConstantConditions
		int effectLevel = player.getEffect(ENERGY_BOOST).getAmplifier() + 1;
		TirednessHandler.subtract(player, energyBoost$tirednessReduction.floatValue() * effectLevel);

		if (player.tickCount % 20 == 0)
			TirednessHandler.syncToClient(player);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onFoodExhaustion(PlayerExhaustionEvent event) {
		if (!isEnabled(Tiredness.class)
				|| event.getEntity().level().isClientSide)
			return;

		ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
		if (!serverPlayer.gameMode.isSurvival())
			return;

		float tiredness = TirednessHandler.get(serverPlayer);
		TirednessHandler.add(serverPlayer, event.getAmount() * tirednessGainMultiplier.floatValue());
		tryApplyTired(tiredness, serverPlayer);
		TirednessHandler.syncToClient(serverPlayer);
	}

	private static void tryApplyTired(float tiredness, ServerPlayer player) {
		int wantedAmplifier = -1;
		if (tiredness >= tirednessToEffect)
			wantedAmplifier = (int) Math.min(Math.round((tiredness - tirednessToEffect) / tirednessPerLevel), 2);
		if (wantedAmplifier >= 0) {
			int currAmplifier = -1;
			if (player.hasEffect(TIRED)) {
				//noinspection DataFlowIssue
				currAmplifier = player.getEffect(TIRED).getAmplifier();
			}
			if (wantedAmplifier != currAmplifier) {
				player.addEffect(new MobEffectInstance(TIRED, -1, wantedAmplifier, true, false, true));
				if (wantedAmplifier == 0)
					player.displayClientMessage(Component.translatable(TIRED_ENOUGH), false);
				else if (wantedAmplifier == 4)
					player.displayClientMessage(Component.translatable(TOO_TIRED), false);
			}
		}
		else if (!player.hasEffect(ENERGY_BOOST)) {
			if (player.hasEffect(TIRED))
				player.removeEffect(TIRED);
		}
	}

	@SubscribeEvent
	public void onItemFinishUse(LivingEntityUseItemEvent.Finish event) {
		if (!this.isEnabled()
				|| event.getItem().getItem().getFoodProperties(event.getItem(), null) == null
				|| !(event.getEntity() instanceof Player player))
			return;
		for (EnergyBoostItem energyBoostItem : energyBoostItems) {
			energyBoostItem.tryApply(player, event.getItem());
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onTiredBreakSpeed(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled()
				|| !event.getEntity().hasEffect(TIRED))
			return;

		//noinspection ConstantConditions
		int level = event.getEntity().getEffect(TIRED).getAmplifier() + 1;
		event.setNewSpeed(event.getNewSpeed() * (1 - (level * 0.04f)));
	}

	@SubscribeEvent
	public void notTiredToSleep(CanPlayerSleepEvent event) {
		if (!this.isEnabled()
				|| event.getProblem() != null)
			return;

		ServerPlayer player = event.getEntity();

		if (!player.hasEffect(TIRED) && !allowSleepingEvenWhenNotTired && !player.getAbilities().instabuild) {
			event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
			player.displayClientMessage(Component.translatable(NOT_TIRED), true);
			if (!shouldPreventSpawnPoint)
				player.setRespawnPosition(player.level().dimension(), event.getPos(), player.getYRot(), false, true);
		}
		else {
			event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
			player.startSleeping(event.getPos());
			((ServerLevel)player.level()).updateSleepingPlayerList();
			if (!shouldPreventSpawnPoint)
				player.setRespawnPosition(player.level().dimension(), event.getPos(), player.getYRot(), false, true);
		}
	}

	static int timeSkipped;

	//Run after Sleeping Effects
	@SubscribeEvent(priority = EventPriority.LOW)
	public void resetTirednessOnWakeUp(SleepFinishedTimeEvent event) {
		if (!this.isEnabled())
			return;
		AtomicInteger highestTired = new AtomicInteger();
		event.getLevel().players().stream()
				.filter(LivingEntity::isSleeping)
				.filter(player -> player.hasEffect(TIRED))
				.toList().forEach(player -> {
			float tirednessOnWakeUp = TirednessHandler.getOnWakeUp(player);
			if (player.getEffect(TIRED).getAmplifier() > highestTired.get())
				highestTired.set(player.getEffect(TIRED).getAmplifier());
			TirednessHandler.set(player, tirednessOnWakeUp);
			player.removeEffect(TIRED);
		});

		skipTime(event, highestTired.get());
		Thunderstorms.onSkipNight(timeSkipped, (ServerLevel) event.getLevel());
	}

	private static void skipTime(SleepFinishedTimeEvent event, int highestTiredAmplifier) {
		timeSkipped = 12000;
		//If above Tired I increase the time skipped by 2.5 minutes per level
		timeSkipped += 3000 * highestTiredAmplifier;
		event.setTimeAddition(event.getLevel().dayTime() + timeSkipped);
	}

	public static boolean onSleepFinished(ServerLevel level, boolean original) {
		if (!Feature.isEnabled(Tiredness.class))
			return original;

		int rainTime = ((ServerLevelAccessor)level).getServerLevelData().getRainTime();
		int thunderTime = ((ServerLevelAccessor)level).getServerLevelData().getThunderTime();
		int clearWeatherTime = ((ServerLevelAccessor)level).getServerLevelData().getClearWeatherTime();
		if (rainTime > 0)
			((ServerLevelAccessor) level).getServerLevelData().setRainTime(Math.max(rainTime - timeSkipped, 1));
		if (thunderTime > 0)
			((ServerLevelAccessor) level).getServerLevelData().setThunderTime(Math.max(thunderTime - timeSkipped, 1));
		if (clearWeatherTime > 0)
			((ServerLevelAccessor) level).getServerLevelData().setClearWeatherTime(Math.max(clearWeatherTime - timeSkipped, 1));
		//Return false to cancel the vanilla method of resetting the weather
		return false;
	}

	@SubscribeEvent
	public void allowSleepAtDay(CanContinueSleepingEvent event) {
		if (!(event.getEntity() instanceof Player player) || !canSleepDuringDay(player))
			return;
		event.setContinueSleeping(true);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean canSleepDuringDay(Player player) {
		return isEnabled(Tiredness.class)
				&& (player.hasEffect(TIRED) || allowSleepingEvenWhenNotTired || player.getAbilities().instabuild);
	}

	@SubscribeEvent
	public void onServerStarted(ServerStartedEvent event) {
		if (!this.isEnabled()
                || !forceOnePlayerSleep)
			return;

		event.getServer().getGameRules().getRule(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE).set(1, event.getServer());
	}

	@SubscribeEvent
	public void onPhantomTryToSpawn(PlayerSpawnPhantomsEvent event) {
		if (!this.isEnabled()
				|| !tiredTiedPhantoms)
			return;
		if (event.getEntity().getEffect(TIRED) == null) {
			event.setResult(PlayerSpawnPhantomsEvent.Result.DENY);
			return;
		}
		Level level = event.getEntity().level();
		//noinspection DataFlowIssue
		int amplifier = event.getEntity().getEffect(TIRED).getAmplifier();
		//Only summon them at Tired III
		if (amplifier < 2
				|| !level.dimensionType().hasSkyLight()
				|| !level.canSeeSky(event.getEntity().blockPosition())) {
			event.setResult(PlayerSpawnPhantomsEvent.Result.DENY);
			return;
		}
		event.setResult(PlayerSpawnPhantomsEvent.Result.ALLOW);
		event.setPhantomsToSpawn(event.getPhantomsToSpawn());
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.Clone event) {
		if (!this.isEnabled()
			|| !event.isWasDeath())
			return;

		float tiredness = TirednessHandler.get(event.getOriginal());
		switch (onDeathBehaviour) {
			case RESET -> tiredness = 0;
			case KEEP -> { }
			case SET_AT_EFFECT -> {
				if (tiredness > tirednessToEffect.floatValue() + tirednessPerLevel.floatValue())
					tiredness = tirednessToEffect.floatValue() + tirednessPerLevel.floatValue();
			}
			case REMOVE_ONE_LEVEL -> {
				if (tiredness > tirednessToEffect) {
					tiredness -= tirednessPerLevel;
					if (tiredness < tirednessToEffect)
						tiredness = tirednessToEffect.floatValue();
				}
			}
		}

		TirednessHandler.set(event.getEntity(), tiredness);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void debugScreen(CustomizeGuiOverlayEvent.DebugText event) {
		if (!this.isEnabled())
			return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer playerEntity = mc.player;
		if (playerEntity == null)
			return;
		if (mc.getDebugOverlay().showDebugScreen() && !mc.showOnlyReducedInfo()) {
			event.getLeft().add(String.format("Tiredness: %s", new DecimalFormat("#.#").format(TirednessHandler.get(playerEntity))));
		}
	}
}