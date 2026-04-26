package insane96mcp.insanesurvivaloverhaul.module.hungerhealth;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.FoodDataAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.text.DecimalFormat;

@LoadFeature(module = ISOModules.HUNGER_HEALTH, name = "Health Regen & Hunger", description = "Makes Health regen work differently, similar to Combat Test snapshots. Can be customized. Hunger related stuff doesn't work (for obvious reasons) if No Hunger feature is enabled")
public class HealthRegenHunger extends Feature {
	public static final ResourceLocation SPRINT_PENALTY_ID = InsaneSO.id("hungry_sprint_penalty");

	public static ResourceLocation PASSIVE_REGEN_TICK;
	private static final int PASSIVE_REGEN_TICK_RATE = 10;
	private static final int FOOD_REGEN_TICK_RATE = 10;

	@Config(description = "If true, Passive Regeneration is enabled")
	public static Boolean passiveHealthRegen$enable = false;
	@Config(description = "Min represents how many ticks the regeneration of 1 HP takes when health is 100%, Max how many ticks when health is 0%")
	public static MinMaxConfig passiveHealthRegen$speed = new MinMaxConfig(120, 3600);

	@Config(min = 0, description = "Sets how many ticks between the health regeneration happens (vanilla is 80).")
	public static Integer healthRegenSpeed = 40;
	@Config(min = 0, description = "Sets how much hunger the player must have to regen health (vanilla is 17).")
	public static Integer regenWhenFoodAbove = 6;
	@Config(description = "Set to true to disable the health regen boost given when max hunger and saturation (false in Vanilla).")
	public static Boolean disableSaturationRegenBoost = true;
	@Config(description = "Set to true to consume Hunger only (and not saturation) when regenerating health (false for Vanilla).")
	public static Boolean consumeHungerOnly = true;
	@Config(min = 0d, max = 40d, description = "Vanilla consumes 1 saturation or hunger whenever Exhaustion reaches 4.0. You can change that value with this config option. NOTE that Minecraft caps this value to 40.")
	public static Double maxExhaustion = 6.0d;
	@Config(min = 0d, max = 1d, description = "If 'Consume Hunger Only' is true then this is the chance to consume an hunger whenever the player is healed (vanilla ignores this; Combat Test has this set to 0.5).")
	public static Double hungerConsumptionChance = 0.5d;
	@Config(min = 0, description = "Sets how many ticks between starve damage happens (vanilla is 80).")
	public static Integer starve$speed = 160;
	@Config(min = 0, description = "Set how much damage is dealt when starving (vanilla is 1).")
	public static Integer starve$damage = 1;
	@Config(min = 0, max = 20, description = "The player will start starving at this hunger (Vanilla is 0)")
	public static Integer starve$atHunger = 1;
	@Config(description = "If below 'Starve at Hunger' player will starve 2x faster for each hunger point below 'Starve at Hunger'.")
	public static Boolean starve$fasterWhenReallyHungry = true;
	@Config(min = 0, max = 20, description = "Player can only sprint when have at least this much hunger. Vanilla is 7")
	public static Integer sprint$minHunger = 1;
	@Config(min = 0, max = 20, description = "Movement speed penalty when below this hunger")
	public static Integer sprint$speedPenaltyBelowHunger = 7;
	@Config(min = 0, description = "How much less movement speed per hunger below 'Speed Penalty below hunger' sprinting players have")
	public static Double sprint$speedReductionEachHunger = 0.025;
	@Config(description = "If enabled, peaceful difficulty no longer heals and fulfills the player")
	public static Boolean peacefulHunger = true;

	//@Config(min = 0d, max = 1f, description = "When eating you'll get healed by this percentage of 'hunger + saturation' restored.")
	//public static Double foodHealMultiplier = 0d;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		PASSIVE_REGEN_TICK = this.createDataKey("passive_health_regen");
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (!this.isEnabled()
				|| player.level().isClientSide)
			return;

		if (player.tickCount % PASSIVE_REGEN_TICK_RATE == 1 && HealthRegenHunger.passiveHealthRegen$enable && player.isHurt()) {
			incrementPassiveRegenTick(player);
			int passiveRegen = getPassiveRegenSpeed(player);

			if (getPassiveRegenTick(player) > passiveRegen) {
				float heal = 1.0f;
				player.heal(heal);
				resetPassiveRegenTick(player);
			}
		}
	}

	private static int getPassiveRegenSpeed(Player player) {
		float healthPerc = 1f - (player.getHealth() / player.getMaxHealth());
		float ticks = (float) ((HealthRegenHunger.passiveHealthRegen$speed.max - HealthRegenHunger.passiveHealthRegen$speed.min) * healthPerc + HealthRegenHunger.passiveHealthRegen$speed.min);
		if (player.level().getDifficulty().equals(Difficulty.HARD))
			ticks *= 1.5f;
		return (int) ticks;
	}

	private static int getPassiveRegenTick(Player player) {
		return ModNBTData.get(player, PASSIVE_REGEN_TICK, Integer.class);
	}

	private static void incrementPassiveRegenTick(Player player) {
		ModNBTData.put(player, PASSIVE_REGEN_TICK, getPassiveRegenTick(player) + FOOD_REGEN_TICK_RATE);
	}

	private static void resetPassiveRegenTick(Player player) {
		ModNBTData.put(player, PASSIVE_REGEN_TICK, 0);
	}

	/*@SubscribeEvent
	public void onPlayerEat(LivingEntityUseItemEvent.Finish event) {
		if (!this.isEnabled()
				|| event.getItem().getItem().getFoodProperties(event.getItem(), event.getEntity()) == null
				|| !(event.getEntity() instanceof Player)
				|| event.getEntity().level().isClientSide)
			return;

		healOnEat(event);
	}

	public void healOnEat(LivingEntityUseItemEvent.Finish event) {
		if (foodHealMultiplier == 0d)
			return;
		FoodProperties food = event.getItem().getItem().getFoodProperties(event.getItem(), event.getEntity());
		//noinspection ConstantConditions
		double heal = MCUtils.getFoodEffectiveness(food) * foodHealMultiplier;
		event.getEntity().heal((float) heal);
	}*/

	/**
	 * Returns true if overrides the vanilla tick, otherwise false
	 */
	public static boolean tickFoodStats(FoodData foodStats, Player player) {
		if (!Feature.isEnabled(HealthRegenHunger.class))
			return false;
		Difficulty difficulty = player.level().getDifficulty();
		((FoodDataAccessor) foodStats).setLastFoodLevel(foodStats.getFoodLevel());
		if (foodStats.getExhaustionLevel() > maxExhaustion) {
			foodStats.setExhaustion(foodStats.getExhaustionLevel() - maxExhaustion.floatValue());
			if (foodStats.getSaturationLevel() > 0.0F) {
				foodStats.setSaturation(Math.max(foodStats.getSaturationLevel() - 1.0F, 0.0F));
			}
			else if (peacefulHunger || difficulty != Difficulty.PEACEFUL) {
				foodStats.setFoodLevel(Math.max(foodStats.getFoodLevel() - 1, 0));
			}
		}
		tick(foodStats, player, difficulty);

		return true;
	}

	/**
	 * Different from Players#isHurt as doesn't return true if missing less than half a heart
	 */
	public static boolean isPlayerHurt(Player player) {
		return player.getHealth() > 0 && player.getHealth() <= player.getMaxHealth() - 1;
	}

	private static void tick(FoodData foodStats, Player player, Difficulty difficulty) {
		FoodDataAccessor accessor = (FoodDataAccessor) foodStats;
		boolean naturalRegen = player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION) && !ModList.get().isLoaded("nohunger");
		if (naturalRegen && foodStats.getSaturationLevel() > 0.0F && isPlayerHurt(player) && foodStats.getFoodLevel() >= 20 && !disableSaturationRegenBoost) {
			accessor.setTickTimer(accessor.getTickTimer() + 1);
			if (accessor.getTickTimer() >= 10) {
				float f = Math.min(foodStats.getSaturationLevel(), 6.0F);
				player.heal(f / 6.0F);
				foodStats.addExhaustion(f);
				accessor.setTickTimer(0);
			}
		}
		else if (naturalRegen && foodStats.getFoodLevel() > regenWhenFoodAbove && isPlayerHurt(player)) {
			accessor.setTickTimer(accessor.getTickTimer() + 1);
			if (accessor.getTickTimer() >= getRegenSpeed(player)) {
				player.heal(1.0F);
				if (consumeHungerOnly) {
					if (player.level().getRandom().nextDouble() < hungerConsumptionChance)
						addHunger(foodStats, -1);
				}
				else
					foodStats.addExhaustion(6.0F);
				accessor.setTickTimer(0);
			}
		}
		else if (foodStats.getFoodLevel() <= starve$atHunger) {
			accessor.setTickTimer(accessor.getTickTimer() + 1);
			int actualStarveSpeed = starve$speed;
			if (starve$fasterWhenReallyHungry && foodStats.getFoodLevel() < starve$atHunger) {
				int pow = Mth.abs(foodStats.getFoodLevel() - starve$atHunger);
				actualStarveSpeed = actualStarveSpeed >> pow;
			}
			if (accessor.getTickTimer() >= actualStarveSpeed) {
				if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
					player.hurt(player.damageSources().starve(), starve$damage);
				}
				accessor.setTickTimer(0);
			}
		}
		else if (!ModList.get().isLoaded("nohunger")){
			accessor.setTickTimer(0);
		}
	}

	public static void addHunger(FoodData foodStats, int hunger) {
		foodStats.setFoodLevel(Mth.clamp(foodStats.getFoodLevel() + hunger, 0, 20));
	}

	private static int getRegenSpeed(Player player) {
		return healthRegenSpeed;
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void debugScreen(CustomizeGuiOverlayEvent.DebugText event) {
		if (!this.isEnabled()
			|| ModList.get().isLoaded("nohunger"))
			return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer playerEntity = mc.player;
		if (playerEntity == null)
			return;
		if (mc.getDebugOverlay().showDebugScreen() && !mc.showOnlyReducedInfo()) {
			FoodData foodStats = playerEntity.getFoodData();
			event.getLeft().add(String.format("Hunger: %d, Saturation: %s, Exhaustion: %s", foodStats.getFoodLevel(), new DecimalFormat("#.#").format(foodStats.getSaturationLevel()), new DecimalFormat("0.00").format(foodStats.getExhaustionLevel())));
		}
	}
}