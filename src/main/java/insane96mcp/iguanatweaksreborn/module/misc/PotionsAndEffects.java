package insane96mcp.iguanatweaksreborn.module.misc;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.effect.AttackDamageMobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@LoadFeature(module = Modules.Ids.MISC, description = "Various changes to potions and effects")
public class PotionsAndEffects extends Feature {
    @Config(min = 1, description = "Poison will damage the player every this ticks at level I. Vanilla is 25.")
    public static Integer poisonDamageSpeed = 60;
    @Config(description = "Changes Strength and Weakness +/-3 damage per level to +/-20% damage per level. (Requires a Minecraft restart)")
    public static Boolean betterStrengthAndWeakness = true;
    @Config(description = "Changes Mining fatigue and haste to no longer affects attack speed. (Requires a Minecraft restart)")
    public static Boolean betterHasteAndMiningFatigue = true;
    @Config(description = "Changes Healing potions to work like pre 1.6.1 by healing 3 health per level")
    public static Boolean betterHealingPotion = true;
    @Config(description = "Regeneration will regenerate health every this ticks at level I. Vanilla is 50.")
    public static Integer regenerationBaseSpeed = 50;
    @Config(description = "The strength used to throw splash potions. Vanilla is 0.5")
    public static Double splashPotionThrowStrength = 0.8d;
    @Config(description = "If true, entities will no longer be set on fire if have Fire Resistance (like bedrock edition)")
    public static Boolean preventFireWithResistance = false;
    @Config(description = "Splash potions will always be 2/3 effective compared to drinkable ones but will also apply the full effect duration when thrown")
    public static Boolean streamlineSplashPotions = true;

    public static float STREAMLINE_SPLASH_POTION_MULTIPLIER = 0.667f;

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        if (betterStrengthAndWeakness) {
            MobEffects.DAMAGE_BOOST.attributeModifiers.remove(Attributes.ATTACK_DAMAGE);
            MobEffects.DAMAGE_BOOST.addAttributeModifier(Attributes.ATTACK_DAMAGE, "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", 0.0D, AttributeModifier.Operation.MULTIPLY_BASE);
            ((AttackDamageMobEffect) MobEffects.DAMAGE_BOOST).multiplier = 0.2d;
        }
        if (betterStrengthAndWeakness) {
            MobEffects.WEAKNESS.attributeModifiers.remove(Attributes.ATTACK_DAMAGE);
            MobEffects.WEAKNESS.addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", 0.0D, AttributeModifier.Operation.MULTIPLY_BASE);
            ((AttackDamageMobEffect) MobEffects.WEAKNESS).multiplier = -0.2d;
        }
        if (betterHasteAndMiningFatigue) {
            MobEffects.DIG_SPEED.attributeModifiers.remove(Attributes.ATTACK_SPEED);
            MobEffects.DIG_SLOWDOWN.attributeModifiers.remove(Attributes.ATTACK_SPEED);
        }
    }

    public static boolean isFireImmune(Entity entity) {
        if (!isEnabled(Tweaks.class)
                || !preventFireWithResistance
                || !(entity instanceof LivingEntity livingEntity))
            return false;

        return livingEntity.hasEffect(MobEffects.FIRE_RESISTANCE);
    }

    public static int getRegenSpeed(int original) {
        return isEnabled(PotionsAndEffects.class) ? regenerationBaseSpeed : original;
    }

    public static int getPoisonDamageSpeed(int original) {
        return isEnabled(PotionsAndEffects.class) ? poisonDamageSpeed : original;
    }

    public static boolean streamlineSplashPotions() {
        return Feature.isEnabled(PotionsAndEffects.class) && streamlineSplashPotions;
    }
}