package insane96mcp.insanesurvivaloverhaul.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

import javax.annotation.Nullable;

public class ISOEventHook {
    /// Fired before {@link LivingEntity#actuallyHurt(DamageSource, float)} is called
    public static float onPreAbsorpDamage(LivingEntity entity, DamageContainer container) {
        return NeoForge.EVENT_BUS.post(new LivingDamageEventPreAbsorp(entity, container)).getNewDamage();
    }

    /**
     * Fired before death penalties (item/durability/xp loss) are applied to the player.
     * Returns the posted event so callers can read mutable fields after listeners run.
     * Returns {@code null} if the event was cancelled — callers must skip all penalty processing.
     */
    @Nullable
    public static DeathPenaltyEvent onDeathPenalty(ServerPlayer player, int lostItemsPercentage, int lostDurabilityPercentage, boolean destroyItems, int lostXpPercentage, boolean destroyXp) {
        DeathPenaltyEvent event = new DeathPenaltyEvent(player, lostItemsPercentage, lostDurabilityPercentage, destroyItems, lostXpPercentage, destroyXp);
        NeoForge.EVENT_BUS.post(event);
        return event.isCanceled() ? null : event;
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
