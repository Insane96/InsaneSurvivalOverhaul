package insane96mcp.insanesurvivaloverhaul.mixin.module.world.coalfire;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FireBlock.class)
public class FireBlockMixin_CoalFire {
	@ModifyReturnValue(at = @At("RETURN"), method = "getFireTickDelay")
	private static int setFireTickDelay(int original, RandomSource random) {
		if (ServerLifecycleHooks.getCurrentServer() == null)
			return original;
		ServerLevel level = ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD);
		if (level == null)
			return original;
		return original / level.getGameRules().getInt(CoalFire.RULE_FIRESPEEDMULTIPLIER);
	}
}
