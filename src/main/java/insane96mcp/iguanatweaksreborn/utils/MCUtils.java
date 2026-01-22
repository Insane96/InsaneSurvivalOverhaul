package insane96mcp.iguanatweaksreborn.utils;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MCUtils {

    @Nullable
    public static <T extends LivingEntity> T getNearestEntity(List<? extends T> entitiesNearby, List<? extends T> entitiesToExclude, Vec3 pos) {
        double nearestDistance = -1.0D;
        T r = null;

        for(T entity : entitiesNearby) {
            if (entitiesToExclude.contains(entity))
                continue;
            double distance = entity.distanceToSqr(pos);
            if (nearestDistance == -1.0D || distance < nearestDistance) {
                nearestDistance = distance;
                r = entity;
            }
        }

        return r;
    }

    private static final AttributeModifier ANTI_KNOCKBACK_MODIFIER = new AttributeModifier("Insane's Survival Reimagined Anti-Knockback", 1f, AttributeModifier.Operation.ADDITION);

    //Stolen from Tinkers Construct ToolAttackUtil
    public static boolean attackEntityIgnoreInvFrames(DamageSource source, float damage, Entity target, @Nullable LivingEntity living, boolean noKnockback) {
        Optional<AttributeInstance> knockbackResistance = getKnockbackAttribute(living);
        // store last damage before secondary attack
        float oldLastDamage = living == null ? 0 : living.lastHurt;

        // prevent knockback in secondary attacks, if requested
        if (noKnockback)
            knockbackResistance.ifPresent(MCUtils::disableKnockback);

        // set hurt resistance time to 0 because we always want to deal damage in traits
        int lastInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        boolean ret = target.hurt(source, damage);
        target.invulnerableTime = lastInvulnerableTime; // reset to the old time so bows work right
        // set total received damage, important for AI and stuff
        if (living != null)
            living.lastHurt += oldLastDamage;

        // remove no knockback marker
        if (noKnockback)
            knockbackResistance.ifPresent(MCUtils::enableKnockback);
        return ret;
    }

    /** Gets the knockback attribute instance if the modifier is not already present */
    private static Optional<AttributeInstance> getKnockbackAttribute(@Nullable LivingEntity living) {
        return Optional.ofNullable(living)
                .map(e -> e.getAttribute(Attributes.KNOCKBACK_RESISTANCE))
                .filter(attribute -> !attribute.hasModifier(ANTI_KNOCKBACK_MODIFIER));
    }

    /** Enable the anti-knockback modifier */
    private static void disableKnockback(AttributeInstance instance) {
        instance.addTransientModifier(ANTI_KNOCKBACK_MODIFIER);
    }

    /** Disables the anti knockback modifier */
    private static void enableKnockback(AttributeInstance instance) {
        instance.removeModifier(ANTI_KNOCKBACK_MODIFIER);
    }

    public static int getDurabilityLeft(ItemStack stack) {
        return stack.getMaxDamage() - stack.getDamageValue();
    }

    public static float getPercentageDurabilityLeft(ItemStack itemStack) {
        Objects.requireNonNull(itemStack, "itemStack can't be null");
        if (!itemStack.isDamageableItem())
            return 0f;

        return ((float) getDurabilityLeft(itemStack)) / itemStack.getMaxDamage();
    }

    /**
     * Returns the result of the food formula with EvalEx given hunger, saturation_modifier, effectiveness and fast_food variables
     */
    public static float computeFoodFormula(FoodProperties food, String formula) {
        Expression expression = new Expression(formula);
        try {
            //noinspection ConstantConditions
            EvaluationValue result = expression
                    .with("hunger", food.getNutrition())
                    .and("saturation_modifier", food.getSaturationModifier())
                    .and("effectiveness", insane96mcp.insanelib.util.MCUtils.getFoodEffectiveness(food))
                    .and("fast_food", food.isFastFood())
                    .evaluate();
            return result.getNumberValue().floatValue();
        }
        catch (Exception ex) {
            ISOLogHelper.error("Failed to evaluate food formula: %s\n%s", formula, ex.getMessage());
            return -1f;
        }
    }
}
