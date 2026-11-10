package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.villager;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.MerchantTrades;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin_MerchantTrades extends AbstractVillager {

    public WanderingTraderMixin_MerchantTrades(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * When {@link MerchantTrades} is enabled, replaces the hardcoded 5 generic + 1 rare offers with the `buying`, `uncommon` and `common` pools,
     * each rolling the `amount` of its trade set. The experimental trade rebalance is left vanilla.
     */
    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$updateTradesFromTradeSets(CallbackInfo ci) {
        if (!Feature.isEnabled(MerchantTrades.class)
                || this.level().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE))
            return;
        MerchantTrades.updateWanderingTraderOffers((WanderingTrader) (Object) this, this.getOffers());
        ci.cancel();
    }
}
