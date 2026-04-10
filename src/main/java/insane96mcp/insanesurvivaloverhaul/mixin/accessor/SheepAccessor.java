package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.animal.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sheep.class)
public interface SheepAccessor {
    @Accessor
    EatBlockGoal getEatBlockGoal();

    @Accessor
    void setEatBlockGoal(EatBlockGoal eatBlockGoal);
}
