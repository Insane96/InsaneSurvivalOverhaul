package insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when a SRExplosion is created, allowing for modifications.
 * Not cancellable and doesn't have a result
 */
@Cancelable
public class ISOExplosionCreatedEvent extends Event {
    private final ISOExplosion explosion;

    public ISOExplosionCreatedEvent(ISOExplosion explosion) {
        super();
        this.explosion = explosion;
    }

    public ISOExplosion getExplosion() {
        return this.explosion;
    }
}
