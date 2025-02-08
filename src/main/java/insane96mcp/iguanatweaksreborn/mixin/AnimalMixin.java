package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Animal.class)
public abstract class AnimalMixin extends AgeableMob {
    protected AnimalMixin(EntityType<? extends AgeableMob> pEntityType, Level pLevel) {
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
        ((ServerLevel) instance).sendParticles(pParticleData, this.iguanaTweaksReborn$getRandomXInWidth(0.2f), this.getRandomY() + 0.5, this.iguanaTweaksReborn$getRandomZInWidth(0.2f), 1, 0, 0, 0, 0.1f);
    }

    @Unique
    public double iguanaTweaksReborn$getRandomXInWidth(double delta) {
        return this.iguanaTweaksReborn$getRandomValueInWidth(delta) + this.getX();
    }

    @Unique
    public double iguanaTweaksReborn$getRandomZInWidth(double delta) {
        return this.iguanaTweaksReborn$getRandomValueInWidth(delta) + this.getZ();
    }

    @Unique
    public double iguanaTweaksReborn$getRandomValueInWidth(double delta) {
        return Mth.nextDouble(this.random, -this.getBbWidth() - delta, this.getBbWidth() + delta);
    }
}
