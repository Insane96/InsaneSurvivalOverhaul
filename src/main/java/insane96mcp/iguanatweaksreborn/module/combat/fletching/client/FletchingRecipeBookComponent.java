package insane96mcp.iguanatweaksreborn.module.combat.fletching.client;

import insane96mcp.iguanatweaksreborn.module.combat.fletching.crafting.FletchingRecipe;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.inventory.FletchingMenu;
import insane96mcp.iguanatweaksreborn.setup.client.GhostRecipeAmount;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

public class FletchingRecipeBookComponent extends RecipeBookComponent {
    protected final GhostRecipeAmount ghostRecipeAmount = new GhostRecipeAmount();

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean r = super.mouseClicked(pMouseX, pMouseY, pButton);
        if (this.ghostRecipe.getRecipe() == null)
            this.ghostRecipeAmount.clear();
        return r;
    }

    public void setupGhostRecipe(Recipe<?> pRecipe, List<Slot> pSlots) {
        ItemStack resultStack = pRecipe.getResultItem(this.minecraft.level.registryAccess());
        this.ghostRecipeAmount.setRecipe(pRecipe);
        this.ghostRecipe.setRecipe(pRecipe);
        this.ghostRecipeAmount.addIngredient(Ingredient.of(resultStack), resultStack.getCount(), (pSlots.get(FletchingMenu.RESULT_SLOT)).x, (pSlots.get(FletchingMenu.RESULT_SLOT)).y);
        NonNullList<Ingredient> nonnulllist = pRecipe.getIngredients();
        Iterator<Ingredient> iterator = nonnulllist.iterator();
        for(int i = 0; i < 3; ++i) {
            if (!iterator.hasNext()) {
                return;
            }
            Ingredient ingredient = iterator.next();
            if (!ingredient.isEmpty()) {
                Slot slot1 = pSlots.get(i);
                this.ghostRecipeAmount.addIngredient(ingredient, ((FletchingRecipe)pRecipe).getIngredientAmount(i), slot1.x, slot1.y);
            }
        }
    }

    @Override
    public void slotClicked(@Nullable Slot pSlot) {
        if (pSlot != null && pSlot.index < this.menu.getSize()) {
            this.ghostRecipeAmount.clear();
        }
        super.slotClicked(pSlot);
    }

    public void renderTooltip(GuiGraphics guiGraphics, int pRenderX, int pRenderY, int pMouseX, int pMouseY) {
        if (this.isVisible()) {
            this.renderGhostRecipeTooltip(guiGraphics, pRenderX, pRenderY, pMouseX, pMouseY);
        }
        super.renderTooltip(guiGraphics, pRenderX, pRenderY, pMouseX, pMouseY);
    }

    private void renderGhostRecipeTooltip(GuiGraphics guiGraphics, int p_100376_, int p_100377_, int pMouseX, int pMouseY) {
        ItemStack itemstack = null;

        for(int i = 0; i < this.ghostRecipeAmount.size(); ++i) {
            GhostRecipeAmount.GhostIngredient ghostrecipe$ghostingredient = this.ghostRecipeAmount.get(i);
            int j = ghostrecipe$ghostingredient.getX() + p_100376_;
            int k = ghostrecipe$ghostingredient.getY() + p_100377_;
            if (pMouseX >= j && pMouseY >= k && pMouseX < j + 16 && pMouseY < k + 16) {
                itemstack = ghostrecipe$ghostingredient.getItem();
            }
        }

        if (itemstack != null && this.minecraft.screen != null) {
            guiGraphics.renderComponentTooltip(this.minecraft.font, Screen.getTooltipFromItem(this.minecraft, itemstack), pMouseX, pMouseY, itemstack);
        }

    }

    public void renderGhostRecipeAmount(GuiGraphics guiGraphics, int pLeftPos, int pTopPos, boolean p_100326_, float pPartialTick) {
        this.ghostRecipeAmount.render(guiGraphics, this.minecraft, pLeftPos, pTopPos, p_100326_, pPartialTick);
    }

    public void addItemToSlot(Iterator<Ingredient> pIngredients, int pSlot, int pMaxAmount, int pY, int pX) {
        Ingredient ingredient = pIngredients.next();
        if (!ingredient.isEmpty()) {
            Slot slot = this.menu.slots.get(pSlot);
            this.ghostRecipeAmount.addIngredient(ingredient, pMaxAmount, slot.x, slot.y);
        }

    }
}
