package insane96mcp.iguanatweaksreborn.module.combat.fletching.inventory;

import insane96mcp.iguanatweaksreborn.module.combat.fletching.crafting.FletchingRecipe;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Iterator;

public class FletchingPlaceRecipe extends ServerPlaceRecipe<CraftingContainer> {
    public FletchingPlaceRecipe(RecipeBookMenu<CraftingContainer> pMenu) {
        super(pMenu);
    }

    @Override
    public void placeRecipe(int pWidth, int pHeight, int pOutputSlot, Recipe<?> pRecipe, Iterator<Integer> pIngredients, int maxAmount) {
        FletchingRecipe fletchingRecipe = (FletchingRecipe) pRecipe;
        int amount1 = fletchingRecipe.getIngredientAmount(0);
        while (amount1 < maxAmount)
            amount1 += fletchingRecipe.getIngredientAmount(0);
        this.addItemToSlot(pIngredients, 0, amount1, 0, 0);
        this.addItemToSlot(pIngredients, 1, amount1 / fletchingRecipe.getIngredientAmount(0), 0, 0);
        if (fletchingRecipe.getCatalyst2() != null) {
            this.addItemToSlot(pIngredients, 2, amount1 / fletchingRecipe.getIngredientAmount(0), 0, 0);
        }
    }
}
