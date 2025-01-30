package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.mobs.villager.wanderingtrader.WanderingTrades;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderMixin extends AbstractVillager {

	public WanderingTraderMixin(EntityType<? extends AbstractVillager> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(at = @At("HEAD"), method = "updateTrades", cancellable = true)
	private void overrideUpdateTrades(CallbackInfo ci) {
		if (this.level().isClientSide
				|| !Feature.isEnabled(WanderingTrades.class))
			return;
		VillagerTrades.ItemListing[] ordinaryTrades = VillagerTrades.WANDERING_TRADER_TRADES.get(1);
		VillagerTrades.ItemListing[] rareTrades = VillagerTrades.WANDERING_TRADER_TRADES.get(2);
		VillagerTrades.ItemListing[] buyingTrades = VillagerTrades.WANDERING_TRADER_TRADES.get(3);
		if (ordinaryTrades != null && rareTrades != null) {
			MerchantOffers merchantoffers = this.getOffers();
			this.addOffersFromItemListings(merchantoffers, buyingTrades, WanderingTrades.buyingTrades);
			this.addOffersFromItemListings(merchantoffers, ordinaryTrades, WanderingTrades.ordinaryTrades);
			this.addOffersFromItemListings(merchantoffers, rareTrades, WanderingTrades.rareTrades);
		}
		ci.cancel();
	}
}
