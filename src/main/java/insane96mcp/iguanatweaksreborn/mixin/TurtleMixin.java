package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.ScuteBlock;
import insane96mcp.iguanatweaksreborn.module.misc.tweaks.Tweaks;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Turtle.class)
public abstract class TurtleMixin extends Animal {
    protected TurtleMixin(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @WrapOperation(method = "ageBoundaryReached", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Turtle;spawnAtLocation(Lnet/minecraft/world/level/ItemLike;I)Lnet/minecraft/world/entity/item/ItemEntity;"))
    public ItemEntity onSpawnScute(Turtle instance, ItemLike itemLike, int i, Operation<ItemEntity> original) {
        if (!Feature.isEnabled(Tweaks.class)
                || !Tweaks.turtle$scuteDropsAsBlock)
            return original.call(instance, itemLike, i);

        BlockState blockState = this.level().getBlockState(this.blockPosition());
        if (blockState.canBeReplaced())
            this.level().setBlock(this.blockPosition(), Tweaks.SCUTE.get().defaultBlockState(), 3);
        else if (blockState.getBlock() == Tweaks.SCUTE.get()) {
            int height = blockState.getValue(ScuteBlock.HEIGHT) + 1;
            if (height > 15)
                return original.call(instance, itemLike, i);
            this.level().setBlock(this.blockPosition(), blockState.setValue(ScuteBlock.HEIGHT, height), 3);
        }
        this.level().levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, this.blockPosition(), 0);
        return null;
    }
}
