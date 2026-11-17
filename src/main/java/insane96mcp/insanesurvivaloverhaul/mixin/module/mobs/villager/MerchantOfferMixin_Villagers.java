package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.villager;

import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.Villagers;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantOffer.class)
public class MerchantOfferMixin_Villagers {

    @Shadow
    private int uses;
    @Shadow
    @Final
    private int maxUses;
    @Shadow
    private int demand;

    /**
     * When {@link Villagers} is active, positive demand decreases more slowly and negative demand is clamped (see {@link Villagers#getUpdatedDemand}).
     */
    @Inject(method = "updateDemand", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$updateDemand(CallbackInfo ci) {
        if (!Villagers.isActive())
            return;
        this.demand = Villagers.getUpdatedDemand(this.demand, this.uses, this.maxUses);
        ci.cancel();
    }
}
