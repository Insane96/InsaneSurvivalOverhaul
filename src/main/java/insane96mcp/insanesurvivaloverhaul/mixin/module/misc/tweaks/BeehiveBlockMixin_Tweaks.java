package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeehiveBlock.class)
public abstract class BeehiveBlockMixin_Tweaks {
	/**
	 * When a player shears a beehive, drops the honeycombs out of the clicked face instead of inside the block
	 */
	@WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BeehiveBlock;dropHoneycomb(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
	private void insanesurvivaloverhaul$dropHoneycombFromFace(Level level, BlockPos pos, Operation<Void> original, @Local(argsOnly = true) BlockHitResult hitResult) {
		if (Tweaks.doHoneycombsDropFromFace())
			Tweaks.dropHoneycombFromFace(level, pos, hitResult.getDirection());
		else
			original.call(level, pos);
	}
}
