package insane96mcp.insanesurvivaloverhaul.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/// Fired before {@link net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre}, before damage reductions are applied
public class LivingDamageEventPreAbsorp extends LivingEvent {
    private final DamageContainer container;

    public LivingDamageEventPreAbsorp(LivingEntity entity, DamageContainer container) {
        super(entity);
        this.container = container;
    }

    /** {@return the {@link DamageContainer} instance for this damage sequence} */
    public DamageContainer getContainer() {
        return container;
    }

    /** {@return the damage source for this damage sequence} */
    public DamageSource getSource() {
        return container.getSource();
    }

    /** {@return the current value to be applied to the entity's health after this event} */
    public float getNewDamage() {
        return container.getNewDamage();
    }

    /** {@return the original damage amount from the damage source} */
    public float getOriginalDamage() {
        return container.getOriginalDamage();
    }

    /**
     * Sets the amount to reduce the entity health by
     *
     * @param newDamage the new damage value
     */
    public void setNewDamage(float newDamage) {
        container.setNewDamage(newDamage);
    }
}
