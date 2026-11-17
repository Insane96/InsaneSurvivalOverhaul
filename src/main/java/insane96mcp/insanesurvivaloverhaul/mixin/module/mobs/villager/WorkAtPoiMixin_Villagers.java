package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.villager;

import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.Villagers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.WorkAtPoi;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorkAtPoi.class)
public class WorkAtPoiMixin_Villagers {

    /**
     * Convalescent villagers don't work at their job site (farmers' composter included), so they don't use the workstation nor restock.
     */
    @Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;)Z", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$noWorkWhenConvalescent(ServerLevel level, Villager owner, CallbackInfoReturnable<Boolean> cir) {
        if (Villagers.isConvalescent(owner))
            cir.setReturnValue(false);
    }
}
