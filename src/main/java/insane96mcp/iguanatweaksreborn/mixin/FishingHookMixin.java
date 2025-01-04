package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.event.ISOEventFactory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile {

    protected FishingHookMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 0), method = "catchingFish", ordinal = 0)
    private int onTickToHookLure(int i) {
        return ISOEventFactory.onHookTickToHookLure((FishingHook) (Object) this, i);
    }
}