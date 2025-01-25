package insane96mcp.iguanatweaksreborn.mixin.integration.appleskin;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import squeek.appleskin.api.food.FoodValues;
import squeek.appleskin.client.HUDOverlayHandler;

@Mixin(HUDOverlayHandler.class)
public class HUDOverlayHandlerMixin {
    @ModifyArg(method = "renderFoodOrHealthOverlay", at = @At(value = "INVOKE", target = "Lsqueek/appleskin/client/HUDOverlayHandler;drawSaturationOverlay(Lsqueek/appleskin/api/event/HUDOverlayEvent$Saturation;Lnet/minecraft/client/Minecraft;FF)V", ordinal = 1), index = 2, remap = false)
    private static float iguanatweaksreborn$onSaturationGainedOverlayDraw(float original, @Local FoodData stats, @Local FoodValues modifiedFoodValues) {
        if (!Feature.isEnabled(FoodDrinks.class)
                || !FoodDrinks.combatSnapshotEatingSaturation)
            return original;
        float saturationRestored = modifiedFoodValues.hunger * modifiedFoodValues.saturationModifier * 2.0F;
        float saturationAfter = Math.min(Math.max(stats.saturationLevel, saturationRestored), (float) stats.foodLevel);
        return saturationAfter - stats.saturationLevel;
    }
}
