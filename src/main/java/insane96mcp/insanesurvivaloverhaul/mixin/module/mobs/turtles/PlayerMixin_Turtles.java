package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.turtles;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.mobs.turtles.Turtles;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin_Turtles {
    /**
     * Prevents the Turtle Helmet from giving Water Breathing when {@link Turtles} replaces it with the Oxygen Bonus
     * attribute (applied through the helmet's components).
     */
    @ModifyExpressionValue(method = "turtleHelmetTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    public boolean insanesurvivaloverhaul$onTurtleHelmetTick(boolean original) {
        if (!original || !Turtles.replacesHelmetWaterBreathing())
            return original;
        return false;
    }
}
