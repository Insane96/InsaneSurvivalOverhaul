package insane96mcp.insanesurvivaloverhaul.mixin.module.farming.livestock;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.farming.livestock.Livestock;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BreedGoal.class)
public abstract class BreedGoalMixin_Livestock {
    @Shadow @Final protected Animal animal;

    @Definition(id = "animal", field = "Lnet/minecraft/world/entity/ai/goal/BreedGoal;animal:Lnet/minecraft/world/entity/animal/Animal;")
    @Definition(id = "isInLove", method = "Lnet/minecraft/world/entity/animal/Animal;isInLove()Z")
    @Expression("this.animal.isInLove()")
    @ModifyExpressionValue(method = "canUse", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaulpreventBreedingIfPlayerTriggered(boolean original) {
        if (!Livestock.isBreedingDisabled(this.animal)
                || !original)
            return original;

        if (this.animal.getLoveCause() != null) {
            this.animal.resetLove();
            return false;
        }
        return true;
    }
}
