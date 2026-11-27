package insane96mcp.insanesurvivaloverhaul.mixin.module.combat.misccombat;

import insane96mcp.insanesurvivaloverhaul.module.combat.MiscCombat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_MiscCombat {
    @Shadow
    protected abstract void doHurtEquipment(DamageSource damageSource, float damageAmount, EquipmentSlot... slots);

    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    /**
     * Vanilla {@code hurtArmor} does nothing for mobs: only players and wolves (that override it) damage their armor.
     * Damages the armor worn on the body (e.g. Horse Armor) like the player's armor pieces, if it has durability.
     */
    @Inject(method = "hurtArmor", at = @At("HEAD"))
    private void insanesurvivaloverhaul$damageBodyArmor(DamageSource damageSource, float damageAmount, CallbackInfo ci) {
        if (!((Object) this instanceof Mob)
                || !MiscCombat.damageBodyArmor()
                || !this.getItemBySlot(EquipmentSlot.BODY).isDamageableItem())
            return;

        this.doHurtEquipment(damageSource, damageAmount, EquipmentSlot.BODY);
    }
}
