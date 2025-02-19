package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.world.coalfire.CoalFire;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin {

	@Inject(method = "cookTick", at = @At("HEAD"))
	private static void onGetStateForPlacement(Level pLevel, BlockPos pPos, BlockState pState, CampfireBlockEntity pBlockEntity, CallbackInfo ci) {
		if (!CoalFire.canRainTurnOffCampfires()
				|| !pLevel.isRainingAt(pPos))
			return;
		pLevel.levelEvent(null, 1009, pPos, 0);
		CampfireBlock.dowse(null, pLevel, pPos, pState);
		pLevel.setBlock(pPos, pState.setValue(CampfireBlock.LIT, Boolean.FALSE), 3);
	}
}