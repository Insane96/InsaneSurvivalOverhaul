package insane96mcp.insanesurvivaloverhaul.mixin.module.misc.tweaks;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.Tweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonEggBlock.class)
public abstract class DragonEggBlockMixin_Tweaks {
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	public void insanesurvivaloverhaul$preventTeleport(BlockState state, Level level, BlockPos pos, Player player, CallbackInfo ci) {
		if (Feature.isEnabled(Tweaks.class) && Tweaks.teleportDragonEggOnlyOnRightClick)
			ci.cancel();
	}
}
