package insane96mcp.insanesurvivaloverhaul.mixin.module.animals;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanesurvivaloverhaul.module.animals.Breeding;
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
public abstract class AnimalMixin_Breeding extends AgeableMob {
    @Shadow
    @Nullable
    private UUID loveCause;

    protected AnimalMixin_Breeding(EntityType<? extends AgeableMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    /**
     * Only spawns love hearts on the server, so animals that fall in love on their own (not via a client interaction) show them too
     */
    @Definition(id = "inLove", field = "Lnet/minecraft/world/entity/animal/Animal;inLove:I")
    @Expression("this.inLove > 0")
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean insanesurvivaloverhaul$checkIfServerSideBeforeSendingHearts(boolean original) {
        return original && !this.level().isClientSide;
    }

    /**
     * Sends the love hearts particles to clients instead of adding them client side only
     */
    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void insanesurvivaloverhaul$sendLoveHeartsToClients(Level instance, ParticleOptions pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, Operation<Void> original) {
        ((ServerLevel) instance).sendParticles(pParticleData, Breeding.getRandomXWithin(this, 0.2f), this.getRandomY() + 0.5, Breeding.getRandomZWithin(this,0.2f), 1, 0, 0, 0, 0.1f);
    }

    /**
     * Resets the love cause when setting in love without a player, so animals that fall in love on their own aren't considered as bred by a player
     */
    @Definition(id = "pPlayer", local = @Local(type = Player.class, argsOnly = true))
    @Expression("pPlayer != null")
    @WrapOperation(method = "setInLove", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private boolean insanesurvivaloverhaul$allowSetLoveCauseAnyway(Object left, Object right, Operation<Boolean> original) {
        boolean originalR = original.call(left, right);
        if (!originalR)
            this.loveCause = null;
        return originalR;
    }
}
