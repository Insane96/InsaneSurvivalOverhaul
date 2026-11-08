package insane96mcp.insanesurvivaloverhaul.module.misc;

import com.mojang.serialization.MapCodec;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.items.unvanishableitems.UnvanishableItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

/**
 * A shapeless recipe where the items with durability in the grid are not consumed, they lose 1 durability instead.
 * Items broken by {@link UnvanishableItems} can't be used
 */
public class DamageToolShapelessRecipe extends ShapelessRecipe {
    public DamageToolShapelessRecipe(ShapelessRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY), recipe.getIngredients());
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (Feature.isEnabled(UnvanishableItems.class) && input.items().stream().anyMatch(UnvanishableItems::isBroken))
            return false;
        return super.matches(input, level);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remainingItems = super.getRemainingItems(input);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isDamageableItem() || stack.has(DataComponents.UNBREAKABLE))
                remainingItems.set(i, damageTool(stack));
        }
        return remainingItems;
    }

    private static ItemStack damageTool(ItemStack stack) {
        ItemStack tool = stack.copyWithCount(1);
        if (!tool.isDamageableItem())
            return tool;
        int damage = tool.getDamageValue() + 1;
        if (damage >= tool.getMaxDamage())
            return ItemStack.EMPTY;
        tool.setDamageValue(damage);
        return tool;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Packs.DAMAGE_TOOL_SHAPELESS_SERIALIZER.get();
    }

    /**
     * Same format as the vanilla shapeless recipe
     */
    public static class Serializer implements RecipeSerializer<DamageToolShapelessRecipe> {
        private static final MapCodec<DamageToolShapelessRecipe> CODEC = RecipeSerializer.SHAPELESS_RECIPE.codec().xmap(DamageToolShapelessRecipe::new, recipe -> recipe);
        private static final StreamCodec<RegistryFriendlyByteBuf, DamageToolShapelessRecipe> STREAM_CODEC = RecipeSerializer.SHAPELESS_RECIPE.streamCodec().map(DamageToolShapelessRecipe::new, recipe -> recipe);

        @Override
        public MapCodec<DamageToolShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DamageToolShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
