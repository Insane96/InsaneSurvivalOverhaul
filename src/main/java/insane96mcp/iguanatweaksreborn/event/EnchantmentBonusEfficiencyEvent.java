package insane96mcp.iguanatweaksreborn.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;

/**
 * Called to determine the efficiency bonus of a stack when mining
 */
public class EnchantmentBonusEfficiencyEvent extends LivingEvent {
    ItemStack stack;
    @Nullable
    BlockState state;
    float originalEfficiency;
    float newEfficiency;
    public EnchantmentBonusEfficiencyEvent(LivingEntity entity, @Nullable BlockState state, ItemStack stack, float originalEfficiency)
    {
        super(entity);
        this.stack = stack;
        this.state = state;
        this.originalEfficiency = originalEfficiency;
        this.newEfficiency = originalEfficiency;
    }

    @Nullable
    public BlockState getState() {
        return this.state;
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
