package insane96mcp.iguanatweaksreborn.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class ISOLivingAttackEvent extends LivingEvent
{
    private final DamageSource source;
    private float amount;
    private final float originalAmount;
    public ISOLivingAttackEvent(LivingEntity entity, DamageSource source, float amount)
    {
        super(entity);
        this.source = source;
        this.originalAmount = amount;
        this.amount = amount;
    }

    public DamageSource getSource() { return this.source; }
    public float getOriginalAmount() { return this.amount; }
    public float getAmount() { return this.amount; }
    public void setAmount(float amount) { this.amount = amount; }
}