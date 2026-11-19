package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.villager;

import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.MerchantTrades;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin_MerchantTrades {

    /**
     * After the level 1 offers are generated, puts the Local Flavours offer first (see {@link MerchantTrades#addLocalFlavoursOffer}).
     */
    @Inject(method = "updateTrades", at = @At("TAIL"))
    private void insanesurvivaloverhaul$addLocalFlavoursOffer(CallbackInfo ci) {
        MerchantTrades.addLocalFlavoursOffer((Villager) (Object) this);
    }
}
