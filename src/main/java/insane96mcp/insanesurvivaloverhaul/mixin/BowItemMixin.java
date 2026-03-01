package insane96mcp.insanesurvivaloverhaul.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.ShortbowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BowItem.class)
public class BowItemMixin {

    @ModifyExpressionValue(method = "releaseUsing", at = @At(value = "CONSTANT", args = "floatValue=1.0", ordinal = 0))
    public float insanesurvivaloverhaul$shootInaccuracy(float original) {
        if (!Feature.isEnabled(Bows.class))
            return original;
        return Bows.bowInaccuracy.floatValue();
    }

    @WrapOperation(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"))
    public float insanesurvivaloverhaul$shortBowPower(int chargeTicks, Operation<Float> original, ItemStack stack) {
        if (!stack.is(Bows.SHORTBOW.get()))
            return original.call(chargeTicks);
        return ShortbowItem.getPowerForTime(chargeTicks);
    }
}
