package insane96mcp.insanesurvivaloverhaul.module.misc.fireworks;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the explosions of one or more Firework Stars to an already crafted Firework Rocket
 */
public class FireworkRocketAddStarRecipe extends CustomRecipe {
    public FireworkRocketAddStarRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack rocket = null;
        int stars = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty())
                continue;

            if (stack.is(Items.FIREWORK_ROCKET)) {
                if (rocket != null)
                    return false;
                rocket = stack;
            }
            else if (stack.is(Items.FIREWORK_STAR))
                stars++;
            else
                return false;
        }
        if (rocket == null || stars == 0)
            return false;
        Fireworks fireworks = rocket.getOrDefault(DataComponents.FIREWORKS, new Fireworks(0, List.of()));
        return fireworks.explosions().size() + stars <= Fireworks.MAX_EXPLOSIONS;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack rocket = ItemStack.EMPTY;
        List<FireworkExplosion> newExplosions = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(Items.FIREWORK_ROCKET))
                rocket = stack;
            else if (stack.is(Items.FIREWORK_STAR)) {
                FireworkExplosion explosion = stack.get(DataComponents.FIREWORK_EXPLOSION);
                if (explosion != null)
                    newExplosions.add(explosion);
            }
        }
        if (rocket.isEmpty())
            return ItemStack.EMPTY;

        ItemStack result = rocket.copyWithCount(1);
        Fireworks fireworks = result.getOrDefault(DataComponents.FIREWORKS, new Fireworks(0, List.of()));
        List<FireworkExplosion> explosions = new ArrayList<>(fireworks.explosions());
        explosions.addAll(newExplosions);
        result.set(DataComponents.FIREWORKS, new Fireworks(fireworks.flightDuration(), explosions));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FireworksFeature.FIREWORK_ROCKET_ADD_STAR_SERIALIZER.get();
    }
}
