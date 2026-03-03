package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.IntegratedPack;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.MobEffectAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.event.config.ModConfigEvent;

@LoadFeature(module = ISOModules.MISC, description = "Various changes to potions and effects")
public class PotionsAndEffects extends Feature {
    @Config(min = 1, description = "Poison will damage the player every this ticks at level I. Vanilla is 25.")
    public static Integer poisonDamageSpeed = 60;
    @Config(description = "Changes Strength and Weakness +/-3 damage per level to +/-20% damage per level. (Requires a Minecraft restart)")
    public static Boolean betterStrengthAndWeakness = true;
    @Config(description = "Changes Mining fatigue and haste to no longer affects attack speed. (Requires a Minecraft restart)")
    public static Boolean betterHasteAndMiningFatigue = true;
    @Config(description = "Changes Healing and Harm potions to both heal/damage 4 per level.")
    public static Boolean streamlineHealAndHarmPotions = true;
    @Config(description = "Regeneration will regenerate health every this ticks at level I. Vanilla is 50.")
    public static Integer regenerationBaseSpeed = 50;
    @Config(description = "The strength used to throw splash potions. Vanilla is 0.5")
    public static Double splashPotionThrowStrength = 0.8d;
    @Config(description = "If true, entities will no longer be set on fire if have Fire Resistance (like bedrock edition)")
    public static Boolean preventFireWithResistance = false;
    @Config(description = "Splash potions will always be 2/3 effective compared to drinkable ones but will also apply the full effect duration when thrown")
    public static Boolean streamlineSplashPotions = true;
    @Config(description = "Enables a data pack that increases potions stack sizes")
    public static Boolean potionStackSize = true;

    public static float STREAMLINE_SPLASH_POTION_MULTIPLIER = 0.667f;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addServerPack(InsaneSO.MOD_ID, "potion_stack_size", "InsaneLib's Potion Stack Size", () -> this.isEnabled() && potionStackSize);
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        if (betterStrengthAndWeakness) {
            MobEffects.DAMAGE_BOOST.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.withDefaultNamespace("effect.strength"), 0.2D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            MobEffects.WEAKNESS.value().addAttributeModifier(Attributes.ATTACK_DAMAGE, ResourceLocation.withDefaultNamespace("effect.weakness"), -0.2D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        }
        if (betterHasteAndMiningFatigue) {
            ((MobEffectAccessor) MobEffects.DIG_SPEED.value()).getAttributeModifiers().remove(Attributes.ATTACK_SPEED);
            ((MobEffectAccessor) MobEffects.DIG_SLOWDOWN.value()).getAttributeModifiers().remove(Attributes.ATTACK_SPEED);
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