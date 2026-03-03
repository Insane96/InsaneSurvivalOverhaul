package insane96mcp.insanesurvivaloverhaul.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

public class ISOEventHook {
    /// Fired before {@link LivingEntity#actuallyHurt(DamageSource, float)} is called
    public static float onPreAbsorpDamage(LivingEntity entity, DamageContainer container) {
        return NeoForge.EVENT_BUS.post(new LivingDamageEventPreAbsorp(entity, container)).getNewDamage();
    }

    /**
     * Apply changes to the ticks that will be removed from the hook to lure and hook
     */
    public static int onHookTickToHookLure(FishingHook hook, int tick, HookTickToHookLureEvent.Type type)
    {
        HookTickToHookLureEvent event = new HookTickToHookLureEvent(hook, tick, type);
        NeoForge.EVENT_BUS.post(event);
        return event.getTick();
    }
}
