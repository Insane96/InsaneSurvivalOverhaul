package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.villager;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.Villagers;
import net.minecraft.world.entity.npc.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VillagerData.class)
public class VillagerDataMixin_Villagers {

    /**
     * Applies {@link Villagers#experienceToLevelUpMultiplier} to the experience at the start of a level.
     */
    @ModifyReturnValue(method = "getMinXpPerLevel", at = @At("RETURN"))
    private static int insanesurvivaloverhaul$minXpPerLevel(int original) {
        return Villagers.getExperienceToLevelUp(original);
    }

    /**
     * Applies {@link Villagers#experienceToLevelUpMultiplier} to the experience needed to reach the next level.
     */
    @ModifyReturnValue(method = "getMaxXpPerLevel", at = @At("RETURN"))
    private static int insanesurvivaloverhaul$maxXpPerLevel(int original) {
        return Villagers.getExperienceToLevelUp(original);
    }
}
