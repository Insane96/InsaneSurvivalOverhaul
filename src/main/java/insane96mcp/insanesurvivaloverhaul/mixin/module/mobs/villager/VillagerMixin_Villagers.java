package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.villager;

import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.Villagers;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public class VillagerMixin_Villagers {

    /**
     * When {@link Villagers} is active, villagers can never breed: this stops both the partner search and the breeding behaviour.
     */
    @Inject(method = "canBreed", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$disableBreeding(CallbackInfoReturnable<Boolean> cir) {
        if (!Villagers.canVillagersBreed())
            cir.setReturnValue(false);
    }
}
