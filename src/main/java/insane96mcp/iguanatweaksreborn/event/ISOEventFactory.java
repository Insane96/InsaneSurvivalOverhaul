package insane96mcp.iguanatweaksreborn.event;

import insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul.ISOExplosion;
import insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul.ISOExplosionCreatedEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class ISOEventFactory {
    /**
     * Returns true if the event is canceled
     */
    public static boolean onITRExplosionCreated(ISOExplosion explosion)
    {
        ISOExplosionCreatedEvent event = new ISOExplosionCreatedEvent(explosion);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }

    /**
     * Apply changes to damage amount after damage absorb but before absorption reduction
     */
    public static float onLivingHurtPreAbsorption(LivingEntity livingEntity, DamageSource source, float amount)
    {
        LivingHurtPreAbsorptionEvent event = new LivingHurtPreAbsorptionEvent(livingEntity, source, amount);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAmount();
    }

    /**
     * Apply changes to the ticks that will be removed from the hook to lure and hook
     */
    public static int onHookTickToHookLure(FishingHook hook, int tick)
    {
        HookTickToHookLureEvent event = new HookTickToHookLureEvent(hook, tick);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getTick();
    }

    public static float onLivingAttack(LivingEntity entity, DamageSource src, float amount)
    {
        if (entity instanceof Player)
            return amount;
        ISOLivingAttackEvent event = new ISOLivingAttackEvent(entity, src, amount);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAmount();
    }

    public static float onPlayerAttack(LivingEntity entity, DamageSource src, float amount)
    {
        ISOLivingAttackEvent event = new ISOLivingAttackEvent(entity, src, amount);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getAmount();
    }

    public static int getStackMaxDamage(ItemStack stack, int originalMaxDurability)
    {
        StackMaxDamageEvent event = new StackMaxDamageEvent(stack, originalMaxDurability);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getNewMaxDamage();
    }
}
