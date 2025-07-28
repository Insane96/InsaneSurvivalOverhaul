package insane96mcp.iguanatweaksreborn.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;

/**
 * Called to determine the mining speed bonus of a stack when mining
 */
public class EnchantmentBonusMiningSpeedEvent extends LivingEvent {
    ItemStack stack;
    @Nullable
    BlockState state;
    float originalMiningSpeed;
    float newMiningSpeed;
    public EnchantmentBonusMiningSpeedEvent(LivingEntity entity, @Nullable BlockState state, ItemStack stack, float originalMiningSpeed)
    {
        super(entity);
        this.stack = stack;
        this.state = state;
        this.originalMiningSpeed = originalMiningSpeed;
        this.newMiningSpeed = originalMiningSpeed;
    }

    @Nullable
    public BlockState getState() {
        return this.state;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public float getOriginalMiningSpeed() {
        return this.originalMiningSpeed;
    }

    public float getNewMiningSpeed() {
        return this.newMiningSpeed;
    }

    public void setNewMiningSpeed(float newMiningSpeed) {
        this.newMiningSpeed = newMiningSpeed;
    }
}
