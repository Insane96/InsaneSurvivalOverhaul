package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul.ExplosionOverhaul;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Definition(id = "level", field = "Lnet/minecraft/world/level/Explosion;level:Lnet/minecraft/world/level/Level;")
    @Definition(id = "addParticle", method = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
    @Definition(id = "EXPLOSION", field = "Lnet/minecraft/core/particles/ParticleTypes;EXPLOSION:Lnet/minecraft/core/particles/SimpleParticleType;")
    @Expression("this.level.addParticle(EXPLOSION, ?, ?, ?, ?, ?, ?)")
    @WrapOperation(method = "finalizeExplosion", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void onExplosionEmitterParticle(Level level, ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, Operation<Void> original) {
		if (!Feature.isEnabled(ExplosionOverhaul.class) || !ExplosionOverhaul.disableEmitterParticles)
            original.call(level, particleData, x, y, z, xSpeed, ySpeed, zSpeed);
	}
}
