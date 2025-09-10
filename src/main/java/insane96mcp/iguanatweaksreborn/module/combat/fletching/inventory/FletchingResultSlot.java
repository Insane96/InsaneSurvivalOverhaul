package insane96mcp.iguanatweaksreborn.module.combat.fletching.inventory;

import insane96mcp.iguanatweaksreborn.module.combat.fletching.Fletching;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.crafting.FletchingRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class FletchingResultSlot extends Slot {
    private final CraftingContainer craftSlots;
    private final Player player;
    private int removeCount;

    public FletchingResultSlot(Player pPlayer, CraftingContainer craftSlots, Container pContainer, int pSlot, int pXPosition, int pYPosition) {
        super(pContainer, pSlot, pXPosition, pYPosition);
        this.player = pPlayer;
        this.craftSlots = craftSlots;
    }

    /**
     * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
     */
    public boolean mayPlace(ItemStack pStack) {
        return false;
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new stack.
     */
    public ItemStack remove(int pAmount) {
        if (this.hasItem()) {
            this.removeCount += Math.min(pAmount, this.getItem().getCount());
        }

        return super.remove(pAmount);
    }

    public void onTake(Player pPlayer, ItemStack pStack) {
        this.checkTakeAchievements(pStack);
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(pPlayer);
        Optional<FletchingRecipe> oRecipe = pPlayer.level().getRecipeManager().getRecipeFor(Fletching.FLETCHING_RECIPE_TYPE.get(), this.craftSlots, pPlayer.level());
        net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);
        if (oRecipe.isEmpty())
            return;
        FletchingRecipe recipe = oRecipe.get();
        this.craftSlots.removeItem(0, recipe.getBaseIngredient().getCount());
        this.craftSlots.removeItem(1, recipe.getCatalyst1().getCount());
        if (recipe.getCatalyst2() != null)
            this.craftSlots.removeItem(2, recipe.getCatalyst2().getCount());
    }

    /**
     * Typically increases an internal count, then calls {@code onCrafting(item)}.
     * @param pStack the output - ie, iron ingots, and pickaxes, not ore and wood.
     */
    protected void onQuickCraft(ItemStack pStack, int pAmount) {
        this.removeCount += pAmount;
        this.checkTakeAchievements(pStack);
    }
}
