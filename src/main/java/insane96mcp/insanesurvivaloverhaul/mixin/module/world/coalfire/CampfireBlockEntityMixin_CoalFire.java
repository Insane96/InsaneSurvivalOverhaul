package insane96mcp.insanesurvivaloverhaul.mixin.module.world.coalfire;

import insane96mcp.insanesurvivaloverhaul.module.world.coalfire.CoalFire;
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
public abstract class CampfireBlockEntityMixin_CoalFire {

	@Inject(method = "cookTick", at = @At("HEAD"))
	private static void insanesurvivaloverhaul$onCookTick(Level pLevel, BlockPos pPos, BlockState pState, CampfireBlockEntity pBlockEntity, CallbackInfo ci) {
		if (!pState.getValue(CampfireBlock.LIT)
				|| !CoalFire.canRainTurnOffCampfires()
				|| !pLevel.isRainingAt(pPos)
				|| pLevel.random.nextInt(40) > 0)
			return;
		pLevel.levelEvent(null, 1009, pPos, 0);
		CampfireBlock.dowse(null, pLevel, pPos, pState);
		pLevel.setBlock(pPos, pState.setValue(CampfireBlock.LIT, Boolean.FALSE), 3);
	}
}