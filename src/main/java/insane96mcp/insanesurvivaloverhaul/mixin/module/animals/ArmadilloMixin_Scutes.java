package insane96mcp.insanesurvivaloverhaul.mixin.module.animals;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.animals.scutes.ScuteBlock;
import insane96mcp.insanesurvivaloverhaul.module.animals.scutes.Scutes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Armadillo.class)
public abstract class ArmadilloMixin_Scutes extends Animal {
    protected ArmadilloMixin_Scutes(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * Intercepts the scute item spawn when an armadillo periodically sheds a scute. If configured, places or
     * stacks a {@link ScuteBlock} at the armadillo's position instead of dropping a scute item.
     * Falls back to the default item drop if the block cannot be placed or the stack is full.
     */
    @WrapOperation(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/armadillo/Armadillo;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;"))
    public ItemEntity insanesurvivaloverhaul$onShedScute(Armadillo instance, ItemLike itemLike, Operation<ItemEntity> original) {
        if (!Feature.isEnabled(Scutes.class)
                || !Scutes.scuteDropsAsBlock
                || !Scutes.tryShedScuteAsBlock(this.level(), this.blockPosition(), Scutes.ARMADILLO_SCUTE.get()))
            return original.call(instance, itemLike);
        return null;
    }

    /**
     * Prevents brushing the armadillo (by players and dispensers) from dropping a scute when
     * {@link Scutes#disableArmadilloBrushing} is enabled. Returning false makes the brush interaction fail.
     */
    @Inject(method = "brushOffScute", at = @At("HEAD"), cancellable = true)
    public void insanesurvivaloverhaul$onBrushOffScute(CallbackInfoReturnable<Boolean> cir) {
        if (Scutes.isArmadilloBrushingDisabled())
            cir.setReturnValue(false);
    }
}
