package insane96mcp.insanesurvivaloverhaul.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

public class ISOEventHook {
    /// Fired before {@link LivingEntity#actuallyHurt(DamageSource, float)} is called
    public static float onPreAbsorpDamage(LivingEntity entity, DamageContainer container) {
        return NeoForge.EVENT_BUS.post(new LivingDamageEventPreAbsorp(entity, container)).getNewDamage();
    }
}
