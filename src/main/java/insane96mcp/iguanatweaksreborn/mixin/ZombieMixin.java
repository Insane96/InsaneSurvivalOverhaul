package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.mobs.villager.villagers.Villagers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Zombie.class)
public class ZombieMixin {
	@Definition(id = "pLevel", local = @Local(type = ServerLevel.class, argsOnly = true))
	@Definition(id = "getDifficulty", method = "Lnet/minecraft/server/level/ServerLevel;getDifficulty()Lnet/minecraft/world/Difficulty;")
	@Definition(id = "NORMAL", field = "Lnet/minecraft/world/Difficulty;NORMAL:Lnet/minecraft/world/Difficulty;")
	@Expression("pLevel.getDifficulty() == NORMAL")
	@ModifyExpressionValue(method = "killedEntity", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean iguanatweaksreborn$shouldCovertAtAnyDifficulty(boolean original) {
		return Villagers.shouldConvertVillagerToZombie();
	}

	@Definition(id = "random", field = "Lnet/minecraft/world/entity/monster/Zombie;random:Lnet/minecraft/util/RandomSource;")
	@Definition(id = "nextBoolean", method = "Lnet/minecraft/util/RandomSource;nextBoolean()Z")
	@Expression("this.random.nextBoolean()")
	@ModifyExpressionValue(method = "killedEntity", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean iguanatweaksreborn$nonHardChanceToConvert(boolean original) {
		return !Villagers.shouldConvertVillagerToZombie();
	}
}
