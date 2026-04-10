package insane96mcp.insanesurvivaloverhaul.mixin.module.farming.livestock;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanesurvivaloverhaul.module.farming.livestock.Livestock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(Animal.class)
public abstract class AnimalMixin_Livestock extends AgeableMob {
    @Shadow
    @Nullable
    private UUID loveCause;

    protected AnimalMixin_Livestock(EntityType<? extends AgeableMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Definition(id = "inLove", field = "Lnet/minecraft/world/entity/animal/Animal;inLove:I")
    @Expression("this.inLove > 0")
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean iguanatweaksreborn$checkIfServerSideBeforeSendingHearts(boolean original) {
        return original && !this.level().isClientSide;
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void iguanatweaksreborn$sendLoveHeartsToClients(Level instance, ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, Operation<Void> original) {
        ((ServerLevel) instance).sendParticles(pParticleData, Livestock.getRandomXWithin(this, 0.2f), this.getRandomY() + 0.5, Livestock.getRandomZWithin(this,0.2f), 1, 0, 0, 0, 0.1f);
    }

    @Definition(id = "pPlayer", local = @Local(type = Player.class, argsOnly = true))
    @Expression("pPlayer != null")
    @WrapOperation(method = "setInLove", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean iguanatweaksreborn$allowSetLoveCauseAnyway(Object left, Object right, Operation<Boolean> original) {
        boolean originalR = original.call(left, right);
        if (!originalR)
            this.loveCause = null;
        return originalR;
    }
}
