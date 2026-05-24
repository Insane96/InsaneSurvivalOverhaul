package insane96mcp.insanesurvivaloverhaul.module.hungerhealth;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.ILRangedAttribute;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.FoodDataAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.InCombat;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.text.DecimalFormat;

@LoadFeature(module = ISOModules.HUNGER_HEALTH, description = "Makes natural regeneration and hunger work differently, similar to Combat Test snapshots. Can be customized. Also allows to add passive regen")
public class HungerAndHealthRegen extends Feature {
	public static final DeferredHolder<Attribute, Attribute> PASSIVE_REGEN_PER_SECOND = ISORegistries.ATTRIBUTES.register("passive_regen_per_second", () -> new ILRangedAttribute(0d, 0d, Double.MAX_VALUE));
	public static final DeferredHolder<Attribute, Attribute> REGEN_PER_SECOND = ISORegistries.ATTRIBUTES.register("natural_regen_per_second", () -> new ILRangedAttribute(0.5d, 0d, Double.MAX_VALUE));
	public static final DeferredHolder<Attribute, Attribute> MAX_EXHAUSTION = ISORegistries.ATTRIBUTES.register("max_exhaustion", () -> new ILRangedAttribute(0d, 0d, 40d));
	public static final DeferredHolder<Attribute, Attribute> HUNGER_CONSUMED = ISORegistries.ATTRIBUTES.register("hunger_consumed", () -> new ILRangedAttribute(0d, 0d, 40d));
	public static final DeferredHolder<Attribute, Attribute> HUNGER_REQUIRED_TO_REGEN = ISORegistries.ATTRIBUTES.register("hunger_required_to_regen", () -> new ILRangedAttribute(0d, 0d, 20d));

	@Config(description = "How much health do players heal overtime each second without consuming hunger? This value is applied to the insanesurvivaloverhaul:passive_regen_per_second attribute on player join.")
	public static Double passiveRegenPerSecond = 0d;
	@Config(description = "How much health do players heal overtime each second by consuming hunger? This value is applied to the insanesurvivaloverhaul:regen_per_second attribute on player join.")
	public static Double regenPerSecond = 0.5d;
	@Config(min = 0, max = 1, description = "How much health regen is reduced by when in combat.")
	public static Double regenReductionInCombat = 0.8d;
	@Config(min = 0, description = "Time in seconds in which the health regen is reduced after entering combat.")
	public static Double regenReductionInCombatTime = 10d;
	@Config(description = "Set to true to disable the health regen boost given when max hunger and saturation (false in Vanilla).")
	public static Boolean disableSaturationRegenBoost = true;
	@Config(min = 0d, max = 40d, description = "Vanilla consumes 1 saturation or hunger whenever Exhaustion reaches 4.0. You can change that value with this config option. This value is applied to the insanesurvivaloverhaul:max_exhaustion attribute on player join. NOTE that Minecraft (for ... reasons) caps this value to 40.")
	public static Double maxExhaustion = 6.0d;
	@Config(min = 0d, description = "Exhaustion applied per half heart healed, only applies if 'Consume hunger only' is false. (vanilla is 6; Combat Test doesn't use this).")
	public static Double exhaustionOnHeal = 6d;
	@Config(min = 0d, max = 40d, description = "Hunger consumed per half heart healed, only applies if 'Consume hunger only' is true. Decimal values are treated as chances to consume one more (e.g. this set to 0.5 means that there's 50% chance to consume one hunger). This value is applied to the insanesurvivaloverhaul:hunger_consumed attribute on player join. (vanilla ignores this; Combat Test has this set to 0.5).")
	public static Double hungerConsumptionOnHeal = 0.5d;
	@Config(description = "Set to true to consume Hunger only (and not saturation) when regenerating health (false for Vanilla).")
	public static Boolean consumeHungerOnly = true;
	@Config(min = 0, description = "Sets how much hunger the player must have to regeneration health (vanilla is 18). This value is applied to the insanesurvivaloverhaul:hunger_required_to_regen attribute on player join.")
	public static Integer hungerRequiredToRegen = 7;
	@Config(min = 0, description = "Sets how many ticks between starve damage happens (vanilla is 80).")
	public static Integer starve$speed = 160;
	@Config(min = 0, description = "Set how much damage is dealt when starving (vanilla is 1).")
	public static Integer starve$damage = 1;
	@Config(min = 0, max = 20, description = "The player will start starving at this hunger (Vanilla is 0)")
	public static Integer starve$atHunger = 1;
	@Config(description = "If below 'Starve at Hunger' player will starve 2x faster for each hunger point below 'Starve at Hunger'.")
	public static Boolean starve$fasterWhenReallyHungry = true;
	@Config(description = "If enabled, peaceful difficulty no longer heals and fulfills the player")
	public static Boolean peacefulHunger = true;

	//@Config(min = 0d, max = 1f, description = "When eating you'll get healed by this percentage of 'hunger + saturation' restored.")
	//public static Double foodHealMultiplier = 0d;

	public static void addAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (entityType != EntityType.PLAYER)
				continue;
			if (!event.has(entityType, PASSIVE_REGEN_PER_SECOND))
				event.add(entityType, PASSIVE_REGEN_PER_SECOND);
			if (!event.has(entityType, REGEN_PER_SECOND))
				event.add(entityType, REGEN_PER_SECOND);
			if (!event.has(entityType, MAX_EXHAUSTION))
				event.add(entityType, MAX_EXHAUSTION);
			if (!event.has(entityType, HUNGER_CONSUMED))
				event.add(entityType, HUNGER_CONSUMED);
			if (!event.has(entityType, HUNGER_REQUIRED_TO_REGEN))
				event.add(entityType, HUNGER_REQUIRED_TO_REGEN);
		}
	}

	@SubscribeEvent
	public void onPlayerJoinLevel(PlayerEvent.PlayerLoggedInEvent event) {
		AttributeInstance attribute = event.getEntity().getAttribute(PASSIVE_REGEN_PER_SECOND);
		if (attribute != null)
			attribute.setBaseValue(passiveRegenPerSecond);
		attribute = event.getEntity().getAttribute(REGEN_PER_SECOND);
		if (attribute != null)
			attribute.setBaseValue(regenPerSecond);
		attribute = event.getEntity().getAttribute(MAX_EXHAUSTION);
		if (attribute != null)
			attribute.setBaseValue(maxExhaustion);
		attribute = event.getEntity().getAttribute(HUNGER_CONSUMED);
		if (attribute != null)
			attribute.setBaseValue(hungerConsumptionOnHeal);
		attribute = event.getEntity().getAttribute(HUNGER_REQUIRED_TO_REGEN);
		if (attribute != null)
			attribute.setBaseValue(hungerRequiredToRegen);
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (!this.isEnabled()
				|| player.level().isClientSide)
			return;

		passiveRegen(player);
		regenInCombat(player);
	}

	public static void passiveRegen(Player player) {
		float passiveRegenPerSecond = (float) player.getAttributeValue(PASSIVE_REGEN_PER_SECOND);
		if (passiveRegenPerSecond > 0)
			player.heal(passiveRegenPerSecond / 20f);
	}

	public static void regenInCombat(Player player) {
		if (regenReductionInCombatTime == 0 || regenReductionInCombat == 0)
			return;
		AttributeModifier IN_COMBAT_MODIFIER = new AttributeModifier(InsaneSO.id("in_combat"), -regenReductionInCombat, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
		if (InCombat.isInCombat(player, regenReductionInCombat))
			MCUtils.applyModifier(player, REGEN_PER_SECOND, IN_COMBAT_MODIFIER, false);
		else
			MCUtils.removeModifier(player, REGEN_PER_SECOND, IN_COMBAT_MODIFIER.id());
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
		if (!Feature.isEnabled(HungerAndHealthRegen.class))
			return false;
		Difficulty difficulty = player.level().getDifficulty();
		((FoodDataAccessor) foodStats).setLastFoodLevel(foodStats.getFoodLevel());
		float maxExhaustion = (float) player.getAttributeValue(MAX_EXHAUSTION);
		if (foodStats.getExhaustionLevel() > maxExhaustion) {
			foodStats.setExhaustion(foodStats.getExhaustionLevel() - maxExhaustion);
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
		else if (naturalRegen && foodStats.getFoodLevel() >= player.getAttributeValue(HUNGER_REQUIRED_TO_REGEN) && isPlayerHurt(player)) {
			accessor.setTickTimer(accessor.getTickTimer() + 1);
			if (accessor.getTickTimer() >= getRegenSpeed(player)) {
				player.heal(1.0F);
				if (consumeHungerOnly) {
					int hungerConsumed = MathHelper.getAmountWithDecimalChance(player.getRandom(), player.getAttributeValue(HUNGER_CONSUMED));
					addHunger(foodStats, -hungerConsumed);
				}
				else
					foodStats.addExhaustion(exhaustionOnHeal.floatValue());
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

	public static int getRegenSpeed(Player player) {
		float regenPerSecond = (float) player.getAttributeValue(REGEN_PER_SECOND);
		return (int) (1f / regenPerSecond * 20);
	}

	public static void addHunger(FoodData foodStats, int hunger) {
		foodStats.setFoodLevel(Mth.clamp(foodStats.getFoodLevel() + hunger, 0, 20));
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