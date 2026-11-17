package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.villager;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.Villagers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Zombie.class)
public class ZombieMixin_Villagers {

    /**
     * When {@link Villagers#alwaysConvertZombie} is enabled, the difficulty checks of the villager conversion see Hard,
     * so a villager killed by a zombie always becomes a zombie villager (vanilla: never on Easy, 50% on Normal).
     * The conversion event can still cancel it.
     */
    @ModifyExpressionValue(method = "killedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getDifficulty()Lnet/minecraft/world/Difficulty;"))
    private Difficulty insanesurvivaloverhaul$alwaysConvertVillager(Difficulty original) {
        if (Villagers.shouldAlwaysConvertZombie())
            return Difficulty.HARD;
        return original;
    }
}
