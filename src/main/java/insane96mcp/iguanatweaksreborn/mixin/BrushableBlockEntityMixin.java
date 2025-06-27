package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.experience.DroppedExperience;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrushableBlockEntity.class)
public class BrushableBlockEntityMixin {
    @Inject(method = "brushingCompleted", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrushableBlockEntity;dropContent(Lnet/minecraft/world/entity/player/Player;)V", shift = At.Shift.AFTER))
    public void iguanatweaksreborn$onDropContent(Player pPlayer, CallbackInfo ci) {
        DroppedExperience.tryGenerateBrushXp(pPlayer);
    }
}
