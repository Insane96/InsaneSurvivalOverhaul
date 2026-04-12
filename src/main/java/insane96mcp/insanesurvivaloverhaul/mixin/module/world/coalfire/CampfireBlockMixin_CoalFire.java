package insane96mcp.insanesurvivaloverhaul.mixin.module.world.coalfire;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin_CoalFire {

	@ModifyReturnValue(at = @At("RETURN"), method = "getStateForPlacement")
	private BlockState insanesurvivaloverhaul$onGetStateForPlacement(BlockState original) {
		if (!CoalFire.areCampfiresUnlit())
			return original;
		return original.setValue(CampfireBlock.LIT, Boolean.FALSE);
	}
}