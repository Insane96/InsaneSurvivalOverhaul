package insane96mcp.insanesurvivaloverhaul.module.combat.fletching;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record FletchingRecipeInput(ItemStack ingredient, ItemStack catalyst1, ItemStack catalyst2) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> this.ingredient;
            case 1 -> this.catalyst1;
            case 2 -> this.catalyst2;
            default -> throw new IllegalArgumentException("No item for index " + index);
        };
    }

    @Override
    public int size() {
        return 3;
    }
}
