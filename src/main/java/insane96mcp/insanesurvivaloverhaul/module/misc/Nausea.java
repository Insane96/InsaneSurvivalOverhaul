package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@LoadFeature(module = ISOModules.MISC, description = "Nausea makes eating give Hunger and attacking sometimes miss.")
public class Nausea extends Feature {

    @Config(description = "Eating while nauseous will cause hunger effect")
    public static Boolean hungerWhenEatingAndNauseous = true;
    @Config(min = 0, max = 1, description = "Hitting a mob has this % chance (per level of Nausea) to fail when nauseated")
    public static Double chanceToFailToHit = 0.15d;

    @SubscribeEvent
    public void onEat(LivingEntityUseItemEvent.Finish event) {
        if (!this.isEnabled()
                || !hungerWhenEatingAndNauseous
                || !event.getEntity().hasEffect(MobEffects.CONFUSION))
            return;
        FoodProperties foodProperties = event.getItem().get(DataComponents.FOOD);
        if (foodProperties == null)
            return;
        event.getEntity().addEffect(new MobEffectInstance(MobEffects.HUNGER, foodProperties.nutrition() * 100, event.getEntity().getEffect(MobEffects.CONFUSION).getAmplifier()));
    }

    @SubscribeEvent
    public void onAttack(LivingIncomingDamageEvent event) {
        if (!this.isEnabled()
                || chanceToFailToHit <= 0
                || event.getEntity().level().isClientSide
                || !(event.getSource().getDirectEntity() instanceof LivingEntity attacker)
                || !attacker.hasEffect(MobEffects.CONFUSION))
            return;

        int lvl = attacker.getEffect(MobEffects.CONFUSION).getAmplifier() + 1;
        if (attacker.getRandom().nextFloat() < chanceToFailToHit * lvl) {
            event.setCanceled(true);
            attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.ALLAY_AMBIENT_WITH_ITEM, attacker.getSoundSource(), 1f, 2f);
        }
    }
}
