package insane96mcp.insanesurvivaloverhaul.mixin.module.animals;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.animals.Breeding;
import insane96mcp.insanesurvivaloverhaul.module.animals.WellFed;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.entity.animal.Turtle$TurtleBreedGoal")
public abstract class TurtleBreedGoalMixin_Breeding extends BreedGoal {
    protected TurtleBreedGoalMixin_Breeding(Animal animal, double speedModifier) {
        super(animal, speedModifier);
    }

    /**
     * Turtles don't call spawnChildFromBreeding (they get an egg instead), so BabyEntitySpawnEvent is never fired.
     * Applies the breeding fail chance, resetting love and age as vanilla does when the event is canceled
     */
    @Inject(method = "breed", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$failBreeding(CallbackInfo ci) {
        if (!Feature.isEnabled(Breeding.class)
                || !Breeding.shouldBreedingFail(this.animal))
            return;

        this.animal.setAge(6000);
        this.partner.setAge(6000);
        this.animal.resetLove();
        this.partner.resetLove();
        ci.cancel();
    }

    /**
     * Consumes the Well Fed time of both turtles after a successful breeding, as done in {@link WellFed#onBreed} for other animals
     */
    @Inject(method = "breed", at = @At("TAIL"))
    private void insanesurvivaloverhaul$consumeWellFed(CallbackInfo ci) {
        if (!Feature.isEnabled(WellFed.class))
            return;

        WellFed.consumeFeed(this.animal);
        WellFed.consumeFeed(this.partner);
    }
}
