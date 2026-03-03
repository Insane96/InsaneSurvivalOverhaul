package insane96mcp.insanesurvivaloverhaul.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.mobs.MiscMobs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.monster.Guardian.GuardianAttackGoal")
public class GuardianAttackGoalMixin {
    @Shadow
    @Final
    private Guardian guardian;

    @Definition(id = "attackTime", field = "Lnet/minecraft/world/entity/monster/Guardian$GuardianAttackGoal;attackTime:I")
    @Definition(id = "guardian", field = "Lnet/minecraft/world/entity/monster/Guardian$GuardianAttackGoal;guardian:Lnet/minecraft/world/entity/monster/Guardian;")
    @Definition(id = "getAttackDuration", method = "Lnet/minecraft/world/entity/monster/Guardian;getAttackDuration()I")
    @Expression("this.attackTime >= this.guardian.getAttackDuration()")
    @WrapOperation(method = "tick", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$onTryAttack(int left, int right, Operation<Boolean> original, @Local LivingEntity livingEntity) {
        boolean originalResult = original.call(left, right);
        if (!Feature.isEnabled(MiscMobs.class)
                || !MiscMobs.guardianMagicDamageOnly
                || !originalResult)
            return originalResult;

        livingEntity.hurt(this.guardian.damageSources().indirectMagic(this.guardian, this.guardian), (float) this.guardian.getAttributeValue(Attributes.ATTACK_DAMAGE));
        this.guardian.setTarget(null);
        return false;
    }
}
