package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth;

import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.HealthRegenHunger;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin_HealthRegenHunger {
    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void onTick(Player player, CallbackInfo callbackInfo) {
        if (HealthRegenHunger.tickFoodStats((FoodData) (Object) this, player))
            callbackInfo.cancel();
    }
}
