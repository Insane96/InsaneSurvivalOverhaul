package insane96mcp.iguanatweaksreborn.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * Called to determine the efficiency bonus of a stack when mining
 */
public class EnchantmentBonusEfficiencyEvent extends LivingEvent {
    ItemStack stack;
    float originalEfficiency;
    float newEfficiency;
    public EnchantmentBonusEfficiencyEvent(LivingEntity entity, ItemStack stack, float originalEfficiency)
    {
        super(entity);
        this.stack = stack;
        this.originalEfficiency = originalEfficiency;
        this.newEfficiency = originalEfficiency;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public float getOriginalEfficiency() {
        return this.originalEfficiency;
    }

    public float getNewEfficiency() {
        return this.newEfficiency;
    }

    public void setNewEfficiency(float newEfficiency) {
        this.newEfficiency = newEfficiency;
    }
}
