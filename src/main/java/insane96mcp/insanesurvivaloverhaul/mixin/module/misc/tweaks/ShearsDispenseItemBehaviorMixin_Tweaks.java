package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShearsDispenseItemBehavior.class)
public abstract class ShearsDispenseItemBehaviorMixin_Tweaks {
	/**
	 * When a dispenser shears a beehive, drops the honeycombs out of a free face instead of inside the block. The dispenser's side is never free, so they don't end up inside the dispenser
	 */
	@WrapOperation(method = "tryShearBeehive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BeehiveBlock;dropHoneycomb(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
	private static void insanesurvivaloverhaul$dropHoneycombFromFace(Level level, BlockPos pos, Operation<Void> original) {
		if (Tweaks.doHoneycombsDropFromFace())
			Tweaks.dropHoneycombFromFace(level, pos, null);
		else
			original.call(level, pos);
	}
}
