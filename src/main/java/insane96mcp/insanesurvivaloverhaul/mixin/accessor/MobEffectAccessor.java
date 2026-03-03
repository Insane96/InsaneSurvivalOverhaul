package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MobEffect.class)
public interface MobEffectAccessor {
    @Accessor
    Map<Holder<Attribute>, MobEffect.AttributeTemplate> getAttributeModifiers();
}
