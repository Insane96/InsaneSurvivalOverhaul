package insane96mcp.insanesurvivaloverhaul.mixin.module.animals;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.animals.scutes.ScuteBlock;
import insane96mcp.insanesurvivaloverhaul.module.animals.scutes.Scutes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Turtle.class)
public abstract class TurtleMixin_Scutes extends Animal {
    protected TurtleMixin_Scutes(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * Intercepts the scute item spawn when a turtle grows up. If configured, places or
     * stacks a {@link ScuteBlock} at the turtle's position instead of dropping a scute item.
     * Falls back to the default item drop if the block cannot be placed or the stack is full.
     */
    @WrapOperation(method = "ageBoundaryReached", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Turtle;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;I)Lnet/minecraft/world/entity/item/ItemEntity;"))
    public ItemEntity insanesurvivaloverhaul$onSpawnScute(Turtle instance, ItemLike itemLike, int i, Operation<ItemEntity> original) {
        if (!Feature.isEnabled(Scutes.class)
                || !Scutes.scuteDropsAsBlock
                || !Scutes.tryShedScuteAsBlock(this.level(), this.blockPosition(), Scutes.TURTLE_SCUTE.get()))
            return original.call(instance, itemLike, i);

        this.level().playSound(null, this.blockPosition(), Scutes.TURTLE_SHED_SCUTE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return null;
    }
}
