package insane96mcp.iguanatweaksreborn.module.sleeprespawn;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.data.ISOMobEffectInstance;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.Tiredness;
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
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Sleeping Effects", description = "Prevents the player from sleeping if has not enough Hunger and gives him effects on wake up. Effects on wake up are controlled via json in this feature's folder")
@LoadFeature(module = Modules.Ids.SLEEP_RESPAWN)
public class SleepingEffects extends JsonFeature {

	public static final List<ISOMobEffectInstance> EFFECTS_ON_WAKE_UP_DEFAULT = List.of(
			/*new ISOMobEffectInstance.Builder(MobEffects.MOVEMENT_SLOWDOWN, 400).build(),
			new ISOMobEffectInstance.Builder(MobEffects.WEAKNESS, 300).setAmplifier(1).build(),
			new ISOMobEffectInstance.Builder(MobEffects.DIG_SLOWDOWN, 300).setAmplifier(1).build(),
			new ISOMobEffectInstance.Builder(MobEffects.REGENERATION, 600).build()*/
	);
	public static final ArrayList<ISOMobEffectInstance> effectsOnWakeUp = new ArrayList<>();
	public static final String NO_FOOD_FOR_SLEEP = "iguanatweaksreborn.no_food_for_sleep";

	@Config(min = 0, max = 40)
	@Label(name = "Hunger & Saturation Depleted on Wake Up", description = "How much saturation and hunger are depleted when you wake up in the morning. Setting to 0 will disable this feature.")
	public static Integer hungerDepletedOnWakeUp = 12;
	@Config(min = 0, max = 20)
	@Label(name = "Tired bonus Hunger & Saturation Depleted", description = "How much more saturation and hunger are depleted per level of Tired.")
	public static Integer tiredBonusHungerSaturationDepleted = 1;
	@Config
	@Label(name = "No Sleep If Hungry", description = "If the player's hunger bar is below 'Hunger Depleted on Wake Up' he can't sleep.")
	public static Boolean noSleepIfHungry = true;
	@Config
	@Label(name = "No Sleep with Hunger effect", description = "If the player's has the hunger effect he can't sleep.")
	public static Boolean noSleepWithHungerEffect = true;
	@Config
	@Label(name = "No beneficial effect when hungry", description = "If the player has no hunger on wake up, beneficial effects are not applied.")
	public static Boolean noBeneficialEffectWhenHungry = true;
	@Config(min = -1, max = 255)
	@Label(name = "Dizzy when tired", description = "Apply the bad effects only when the Tired effect is equal or above this amplifier. -1 to disable")
	public static Integer dizzyWhenToTired = 3;

	public SleepingEffects(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("effects_on_wake_up.json", effectsOnWakeUp, EFFECTS_ON_WAKE_UP_DEFAULT, ISOMobEffectInstance.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSurvivalOverhaul.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void wakeUpHungerAndEffects(SleepFinishedTimeEvent event) {
		if (!this.isEnabled()
				|| (hungerDepletedOnWakeUp == 0 && effectsOnWakeUp.isEmpty()))
			return;

		event.getLevel().players().stream().filter(LivingEntity::isSleeping).toList().forEach(player -> {
			//noinspection DataFlowIssue
			int tiredAmplifier = player.getEffect(Tiredness.TIRED.get()) != null ? player.getEffect(Tiredness.TIRED.get()).getAmplifier() : -1;
			if (!ModList.get().isLoaded("nohunger")) {
				FoodData foodData = player.getFoodData();
				int hungerToDeplete = hungerDepletedOnWakeUp + (tiredAmplifier + 1) * tiredBonusHungerSaturationDepleted;
				if (foodData.getSaturationLevel() > 0) {
					float saturation = foodData.saturationLevel;
					int saturationToDeplete = (int) Math.min(hungerToDeplete, saturation);
					foodData.setSaturation(saturation - saturationToDeplete);
					hungerToDeplete -= saturationToDeplete;
				}
				if (hungerToDeplete > 0) {
					int foodToDeplete = Math.min(hungerToDeplete, foodData.foodLevel);
					foodData.setFoodLevel(foodData.foodLevel - foodToDeplete);
					hungerToDeplete -= foodToDeplete;
				}
			}

			for (ISOMobEffectInstance mobEffectInstance : effectsOnWakeUp) {
				if (noBeneficialEffectWhenHungry
						&& mobEffectInstance.effect.get().isBeneficial()
						&& player.getFoodData().getFoodLevel() <= 0)
					continue;
				if (dizzyWhenToTired > -1
						&& !mobEffectInstance.effect.get().isBeneficial()
						&& Feature.isEnabled(Tiredness.class)
						&& tiredAmplifier < dizzyWhenToTired)
					continue;
				player.addEffect(mobEffectInstance.getMobEffectInstance());
			}
		});
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void tooHungryToSleep(PlayerSleepInBedEvent event) {
		if (!this.isEnabled()
				|| event.getResultStatus() != null
				|| !noSleepIfHungry)
			return;
		if ((hungerDepletedOnWakeUp > 0 && event.getEntity().getFoodData().getFoodLevel() < hungerDepletedOnWakeUp)
				|| (noSleepWithHungerEffect && event.getEntity().hasEffect(MobEffects.HUNGER))) {
			event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
			event.getEntity().displayClientMessage(Component.translatable(NO_FOOD_FOR_SLEEP), true);
		}
	}
}