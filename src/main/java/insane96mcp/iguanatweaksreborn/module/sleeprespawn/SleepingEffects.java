package insane96mcp.iguanatweaksreborn.module.sleeprespawn;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalTweaks;
import insane96mcp.iguanatweaksreborn.data.ITRMobEffectInstance;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.Tiredness;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.TirednessHandler;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.*;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Sleeping Effects", description = "Prevents the player from sleeping if has not enough Hunger and gives him effects on wake up. Effects on wake up are controlled via json in this feature's folder")
@LoadFeature(module = Modules.Ids.SLEEP_RESPAWN, enabledByDefault = false)
public class SleepingEffects extends JsonFeature {

	public static final List<ITRMobEffectInstance> EFFECTS_ON_WAKE_UP_DEFAULT = List.of(
			new ITRMobEffectInstance.Builder(MobEffects.MOVEMENT_SLOWDOWN, 400).build(),
			new ITRMobEffectInstance.Builder(MobEffects.WEAKNESS, 300).setAmplifier(1).build(),
			new ITRMobEffectInstance.Builder(MobEffects.DIG_SLOWDOWN, 300).setAmplifier(1).build(),
			new ITRMobEffectInstance.Builder(MobEffects.REGENERATION, 900).build()
	);
	public static final ArrayList<ITRMobEffectInstance> effectsOnWakeUp = new ArrayList<>();
	public static final String NO_FOOD_FOR_SLEEP = "iguanatweaksreborn.no_food_for_sleep";

	@Config(min = 0, max = 20)
	@Label(name = "Hunger Depleted on Wake Up", description = "How much the hunger bar is depleted when you wake up in the morning. Saturation is depleted before depleting hunger bar. Setting to 0 will disable this feature.")
	public static Integer hungerDepletedOnWakeUp = 15;
	@Config
	@Label(name = "No Sleep If Hungry", description = "If the player's hunger bar is below 'Hunger Depleted on Wake Up' he can't sleep.")
	public static Boolean noSleepIfHungry = false;
	@Config
	@Label(name = "No beneficial effect when hungry", description = "If the player has no hunger on wake up, beneficial effects are not applied.")
	public static Boolean noBeneficialEffectWhenHungry = true;
	@Config
	@Label(name = "Dizzy when tired", description = "Apply the bad effects only when too tired")
	public static Boolean dizzyWhenToTired = true;

	public SleepingEffects(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("effects_on_wake_up.json", effectsOnWakeUp, EFFECTS_ON_WAKE_UP_DEFAULT, ITRMobEffectInstance.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSurvivalTweaks.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void wakeUpHungerAndEffects(SleepFinishedTimeEvent event) {
		if (!this.isEnabled()
				|| (hungerDepletedOnWakeUp == 0 && effectsOnWakeUp.isEmpty()))
			return;

		event.getLevel().players().stream().filter(LivingEntity::isSleeping).toList().forEach(player -> {
			float tirednessOnWakeUp = TirednessHandler.getOnWakeUp(player);

			FoodData foodData = player.getFoodData();
			int hungerToDeplete = hungerDepletedOnWakeUp;
			if (foodData.getSaturationLevel() > 0) {
				float saturation = foodData.saturationLevel;
				float saturationToDeplete = Math.min(hungerToDeplete, saturation);
				foodData.setSaturation(saturation - saturationToDeplete);
				hungerToDeplete -= saturationToDeplete;
			}
			if (hungerToDeplete > 0)
				foodData.setFoodLevel(foodData.foodLevel - Math.min(hungerToDeplete, foodData.foodLevel));

			for (ITRMobEffectInstance mobEffectInstance : effectsOnWakeUp) {
				if (noBeneficialEffectWhenHungry && mobEffectInstance.effect.get().isBeneficial() && player.getFoodData().getFoodLevel() <= 0)
					continue;
				if (dizzyWhenToTired && Feature.isEnabled(Tiredness.class) && tirednessOnWakeUp == 0f && !mobEffectInstance.effect.get().isBeneficial())
					continue;
				player.addEffect(mobEffectInstance.getMobEffectInstance());
			}
		});
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void tooHungryToSleep(PlayerSleepInBedEvent event) {
		if (!this.isEnabled()
				|| event.getResultStatus() != null
				|| !noSleepIfHungry
				|| hungerDepletedOnWakeUp == 0
				|| event.getEntity().getFoodData().getFoodLevel() >= hungerDepletedOnWakeUp)
			return;
		event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
		event.getEntity().displayClientMessage(Component.translatable(NO_FOOD_FOR_SLEEP), true);
	}
}