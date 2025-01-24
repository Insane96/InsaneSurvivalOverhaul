package insane96mcp.iguanatweaksreborn.mixin.integration.cavernsandchasms;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.teamabnormals.caverns_and_chasms.core.registry.CCEntityTypes;
import insane96mcp.iguanatweaksreborn.module.mining.MiningMisc;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CCEntityTypes.class)
public class CCEntityTypesMixin {
    @WrapOperation(method = "registerAttributes(Lnet/minecraftforge/event/entity/EntityAttributeModificationEvent;)V", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/EntityAttributeModificationEvent;add(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ai/attributes/Attribute;D)V"), remap = false)
    private static void onSkeletonHealthNerf(EntityAttributeModificationEvent instance, EntityType<? extends LivingEntity> entityType, Attribute attribute, double value, Operation<Void> original) {
        if (MiningMisc.cavernsChasmsIntegration)
            return;
        original.call(instance, entityType, attribute, value);
    }
}
