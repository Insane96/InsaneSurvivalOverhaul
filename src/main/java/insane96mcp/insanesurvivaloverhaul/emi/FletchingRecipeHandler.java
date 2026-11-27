package insane96mcp.insanesurvivaloverhaul.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets EMI fill the Fletching Table slots from the player's inventory ("+" button) and list the fletching
 * recipes in the Craftables sidebar while the Fletching Table screen is open.
 */
public class FletchingRecipeHandler implements StandardRecipeHandler<FletchingMenu> {
    @Override
    public List<Slot> getInputSources(FletchingMenu menu) {
        // Crafting slots + player inventory, without the result slot
        List<Slot> slots = new ArrayList<>(menu.slots.subList(FletchingMenu.INGREDIENT_SLOT, FletchingMenu.RESULT_SLOT));
        slots.addAll(menu.slots.subList(FletchingMenu.RESULT_SLOT + 1, menu.slots.size()));
        return slots;
    }

    @Override
    public List<Slot> getCraftingSlots(FletchingMenu menu) {
        // Must follow the order of EmiFletchingRecipe#getInputs(): ingredient, catalyst1, catalyst2
        return List.of(
                menu.getSlot(FletchingMenu.INGREDIENT_SLOT),
                menu.getSlot(FletchingMenu.CATALYST_1_SLOT),
                menu.getSlot(FletchingMenu.CATALYST_2_SLOT));
    }

    @Override
    public @Nullable Slot getOutputSlot(FletchingMenu menu) {
        // Enables crafting straight to cursor/inventory with ctrl/shift + click on "+"
        return menu.getSlot(FletchingMenu.RESULT_SLOT);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == EmiFletchingRecipe.CATEGORY;
    }
}
