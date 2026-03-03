package insane96mcp.insanesurvivaloverhaul.event;

import net.minecraft.world.entity.projectile.FishingHook;
import net.neoforged.bus.api.Event;

/**
 * Triggered when the game calculates the time will take to {@link Type#LURE} and to {@link Type#HOOK} the fish
 */
public class HookTickToHookLureEvent extends Event {
    private final FishingHook hook;
    private final int originalTick;
    private int tick;
    private final Type type;

    public HookTickToHookLureEvent(FishingHook hook, int tick, Type type)
    {
        this.hook = hook;
        this.originalTick = tick;
        this.tick = tick;
        this.type = type;
    }

    public FishingHook getHookEntity() {
        return this.hook;
    }

    public int getOriginalTick() {
        return this.originalTick;
    }

    public int getTick() {
        return this.tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {
        HOOK, LURE;
    }
}
