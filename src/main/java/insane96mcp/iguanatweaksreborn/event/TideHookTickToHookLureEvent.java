package insane96mcp.iguanatweaksreborn.event;

import com.li64.tide.registries.entities.misc.fishing.TideFishingHook;
import net.minecraftforge.eventbus.api.Event;

public class TideHookTickToHookLureEvent extends Event {
    private final TideFishingHook hook;
    private int tick;
    private final Type type;

    public TideHookTickToHookLureEvent(TideFishingHook hook, int tick, Type type)
    {
        this.hook = hook;
        this.tick = tick;
        this.type = type;
    }

    public TideFishingHook getHookEntity() {
        return this.hook;
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
