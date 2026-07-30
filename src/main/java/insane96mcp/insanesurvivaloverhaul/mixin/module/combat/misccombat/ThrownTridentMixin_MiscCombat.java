package insane96mcp.insanesurvivaloverhaul.mixin.module.combat.misccombat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.MiscCombat;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin_MiscCombat extends AbstractArrow {

    protected ThrownTridentMixin_MiscCombat(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * Replaces the vanilla fixed 8 base damage with the sum of the thrown trident item's own
     * attack damage attribute modifiers, so item/data pack changes to the trident's damage
     * (e.g. {@code tools_and_weapons_rework}) are reflected when it's thrown, not just in melee.
     * @see MiscCombat#thrownTridentItemBasedDamage
     */
    @ModifyExpressionValue(method = "onHitEntity", at = @At(value = "CONSTANT", args = "floatValue=8.0"))
    private float insanesurvivaloverhaul$itemBasedThrownDamage(float original) {
        if (!Feature.isEnabled(MiscCombat.class) || !MiscCombat.thrownTridentItemBasedDamage)
            return original;

        ItemStack weaponItem = this.getWeaponItem();
        double damage = 0d;
        for (ItemAttributeModifiers.Entry entry : weaponItem.getAttributeModifiers().modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE) && entry.slot().test(EquipmentSlot.MAINHAND) && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE)
                damage += entry.modifier().amount();
        }

        return (float) damage;
    }
}
