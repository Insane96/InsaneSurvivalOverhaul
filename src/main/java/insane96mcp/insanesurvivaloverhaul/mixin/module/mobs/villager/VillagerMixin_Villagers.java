package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.villager;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.Villagers;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public abstract class VillagerMixin_Villagers {

    @Shadow
    private void setUnhappy() {
        throw new AssertionError();
    }

    /**
     * When {@link Villagers} is active, villagers can never breed: this stops both the partner search and the breeding behaviour.
     */
    @Inject(method = "canBreed", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$disableBreeding(CallbackInfoReturnable<Boolean> cir) {
        if (!Villagers.canVillagersBreed())
            cir.setReturnValue(false);
    }

    /**
     * Convalescent villagers refuse to trade: they shake their head (like vanilla villagers with no trades) instead of opening the trading screen.
     */
    @WrapOperation(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/npc/Villager;startTrading(Lnet/minecraft/world/entity/player/Player;)V"))
    private void insanesurvivaloverhaul$refuseTradingWhenConvalescent(Villager villager, Player player, Operation<Void> original) {
        if (Villagers.isConvalescent(villager))
            this.setUnhappy();
        else
            original.call(villager, player);
    }

    /**
     * Convalescent villagers don't restock. Safety net for the work behaviour, which is blocked too ({@link WorkAtPoiMixin_Villagers}).
     */
    @Inject(method = "shouldRestock", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$noRestockWhenConvalescent(CallbackInfoReturnable<Boolean> cir) {
        if (Villagers.isConvalescent((Villager) (Object) this))
            cir.setReturnValue(false);
    }
}
