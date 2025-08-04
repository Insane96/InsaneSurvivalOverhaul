package insane96mcp.iguanatweaksreborn.event;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import net.minecraftforge.eventbus.api.Event;

/**
 * Triggered when the game calculates the time will take to {@link Type#LURE} and to {@link Type#HOOK} the fish
 */
public class TideHookTickToHookLureEvent extends Event {
    private final TideFishingHook hook;
    private int originalTick;
    private int tick;
    private final Type type;

    public TideHookTickToHookLureEvent(TideFishingHook hook, int tick, Type type)
    {
        this.hook = hook;
        this.originalTick = tick;
        this.tick = tick;
        this.type = type;
    }

    public TideFishingHook getHookEntity() {
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
