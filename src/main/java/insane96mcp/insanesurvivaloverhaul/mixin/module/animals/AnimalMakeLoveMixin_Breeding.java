package insane96mcp.insanesurvivaloverhaul.mixin.module.animals;

import insane96mcp.insanesurvivaloverhaul.module.animals.Breeding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.AnimalMakeLove;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimalMakeLove.class)
public abstract class AnimalMakeLoveMixin_Breeding {
    /**
     * Prevents Brain based animals (e.g. Hoglins, Goats, Axolotls) from breeding if they've been set in love by a player and manual breeding is disabled for them. Same as {@link BreedGoalMixin_Breeding} for Goal based animals
     */
    @Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;)Z", at = @At("HEAD"), cancellable = true)
    public void insanesurvivaloverhaul$preventBreedingIfPlayerTriggered(ServerLevel level, Animal owner, CallbackInfoReturnable<Boolean> cir) {
        if (!Breeding.isBreedingDisabled(owner)
                || !owner.isInLove()
                || owner.getLoveCause() == null)
            return;

        owner.resetLove();
        cir.setReturnValue(false);
    }
}
