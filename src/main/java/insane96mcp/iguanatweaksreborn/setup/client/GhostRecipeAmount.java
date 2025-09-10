package insane96mcp.iguanatweaksreborn.setup.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/// Ghost recipe but with amount displayed
public class GhostRecipeAmount {
    @Nullable
    private Recipe<?> recipe;
    private final List<GhostIngredient> ingredients = Lists.newArrayList();
    float time;

    public void clear() {
        this.recipe = null;
        this.ingredients.clear();
        this.time = 0.0F;
    }

    public void addIngredient(Ingredient pIngredient, int amount, int pX, int pY) {
        this.ingredients.add(new GhostIngredient(pIngredient, amount, pX, pY));
    }

    public GhostIngredient get(int pIndex) {
        return this.ingredients.get(pIndex);
    }

    public int size() {
        return this.ingredients.size();
    }

    @Nullable
    public Recipe<?> getRecipe() {
        return this.recipe;
    }

    public void setRecipe(Recipe<?> pRecipe) {
        this.recipe = pRecipe;
    }

    public void render(GuiGraphics pGuiGraphics, Minecraft pMinecraft, int pLeftPos, int pTopPos, boolean p_282174_, float pPartialTick) {
        if (!Screen.hasControlDown()) {
            this.time += pPartialTick;
        }

        for(int i = 0; i < this.ingredients.size(); ++i) {
            GhostIngredient ghostrecipe$ghostingredient = this.ingredients.get(i);
            int j = ghostrecipe$ghostingredient.getX() + pLeftPos;
            int k = ghostrecipe$ghostingredient.getY() + pTopPos;
            if (i == 0 && p_282174_) {
                pGuiGraphics.fill(j - 4, k - 4, j + 20, k + 20, 822018048);
            } else {
                pGuiGraphics.fill(j, k, j + 16, k + 16, 822018048);
            }

            ItemStack itemstack = ghostrecipe$ghostingredient.getItem();
            pGuiGraphics.renderFakeItem(itemstack, j, k);
            RenderSystem.depthFunc(516);
            pGuiGraphics.fill(j, k, j + 16, k + 16, 822083583);
            RenderSystem.depthFunc(515);
            if (i == 0) {
                pGuiGraphics.renderItemDecorations(pMinecraft.font, itemstack, j, k);
            }
            if (itemstack.getCount() != 1)
                pGuiGraphics.renderItemDecorations(pMinecraft.font, itemstack, j, k, String.valueOf(itemstack.getCount()));
        }

    }

    @OnlyIn(Dist.CLIENT)
    public class GhostIngredient {
        private final Ingredient ingredient;
        private final int amount;
        private final int x;
        private final int y;

        public GhostIngredient(Ingredient pIngredient, int amount, int pX, int pY) {
            this.ingredient = pIngredient;
            this.amount = amount;
            this.x = pX;
            this.y = pY;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public ItemStack getItem() {
            ItemStack[] aitemstack = this.ingredient.getItems();
            ItemStack stack = aitemstack.length == 0 ? ItemStack.EMPTY : aitemstack[Mth.floor(GhostRecipeAmount.this.time / 30.0F) % aitemstack.length];
            stack.setCount(this.amount);
            return stack;
        }
    }
}
