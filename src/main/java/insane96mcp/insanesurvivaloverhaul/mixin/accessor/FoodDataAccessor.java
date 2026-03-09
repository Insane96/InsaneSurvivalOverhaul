package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface FoodDataAccessor {
    @Accessor
    void setExhaustionLevel(float exhaustionLevel);
}
