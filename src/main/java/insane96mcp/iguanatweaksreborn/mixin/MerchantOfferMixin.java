package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.mobs.villager.villagers.Villagers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantOffer.class)
public class MerchantOfferMixin {

	@Shadow
	@Final
	private ItemStack baseCostA;

	@Shadow
	private int specialPriceDiff;

	@Shadow
	private int demand;

	@Shadow @Final private int maxUses;

	@Inject(at = @At("TAIL"), method = "addToSpecialPriceDiff")
	private void addToSpecialPriceDiff(int add, CallbackInfo callbackInfo) {
		this.specialPriceDiff = Villagers.clampSpecialPrice(this.specialPriceDiff, this.baseCostA);
	}

	@Inject(at = @At("TAIL"), method = "updateDemand")
	private void updateDemand(CallbackInfo callbackInfo) {
		this.demand = Villagers.clampDemand(this.demand, this.maxUses);
	}
}
