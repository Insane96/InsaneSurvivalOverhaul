package insane96mcp.insanesurvivaloverhaul.mixin.module.animals;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.animals.Breeding;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BreedGoal.class)
public abstract class BreedGoalMixin_Breeding {
    @Shadow @Final protected Animal animal;

    /**
     * Prevents animals from breeding if they've been set in love by a player and manual breeding is disabled for them
     */
    @Definition(id = "animal", field = "Lnet/minecraft/world/entity/ai/goal/BreedGoal;animal:Lnet/minecraft/world/entity/animal/Animal;")
    @Definition(id = "isInLove", method = "Lnet/minecraft/world/entity/animal/Animal;isInLove()Z")
    @Expression("this.animal.isInLove()")
    @ModifyExpressionValue(method = "canUse", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean insanesurvivaloverhaul$preventBreedingIfPlayerTriggered(boolean original) {
        if (!Breeding.isBreedingDisabled(this.animal)
                || !original)
            return original;

        if (this.animal.getLoveCause() != null) {
            this.animal.resetLove();
            return false;
        }
        return true;
    }
}
