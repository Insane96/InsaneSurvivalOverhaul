package insane96mcp.insanesurvivaloverhaul.module.items.shulkerboxes;

import com.mojang.serialization.MapCodec;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.Pouch;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.PouchItem;
import insane96mcp.insanesurvivaloverhaul.module.items.pouch.PouchMenu;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

/**
 * A shaped recipe that moves the contents of the Pouches used in the grid into the resulting Shulker Box.
 * Pouches are read left to right, top to bottom: the first one fills slots 0-8, the second one slots 9-17, and so on,
 * keeping the slot positions.
 */
public class ShulkerBoxFromPouchesRecipe extends ShapedRecipe {
    public ShulkerBoxFromPouchesRecipe(ShapedRecipe recipe) {
        super(recipe.getGroup(), recipe.category(), recipe.pattern, recipe.getResultItem(RegistryAccess.EMPTY), recipe.showNotification());
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);
        NonNullList<ItemStack> contents = NonNullList.withSize(ShulkerBoxBlockEntity.CONTAINER_SIZE, ItemStack.EMPTY);
        boolean hasContents = false;
        int offset = 0;
        for (ItemStack stack : input.items()) {
            if (!stack.is(Pouch.ITEM.get()))
                continue;
            NonNullList<ItemStack> pouchContents = PouchItem.getContentsList(stack);
            for (int i = 0; i < pouchContents.size() && offset + i < contents.size(); i++) {
                ItemStack pouchStack = pouchContents.get(i);
                if (pouchStack.isEmpty())
                    continue;
                contents.set(offset + i, pouchStack.copy());
                hasContents = true;
            }
            offset += PouchMenu.CONTAINER_SIZE;
        }
        if (hasContents)
            result.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ShulkerBoxes.FROM_POUCHES_SERIALIZER.get();
    }

    /**
     * Same format as the vanilla shaped recipe
     */
    public static class Serializer implements RecipeSerializer<ShulkerBoxFromPouchesRecipe> {
        private static final MapCodec<ShulkerBoxFromPouchesRecipe> CODEC = RecipeSerializer.SHAPED_RECIPE.codec().xmap(ShulkerBoxFromPouchesRecipe::new, recipe -> recipe);
        private static final StreamCodec<RegistryFriendlyByteBuf, ShulkerBoxFromPouchesRecipe> STREAM_CODEC = RecipeSerializer.SHAPED_RECIPE.streamCodec().map(ShulkerBoxFromPouchesRecipe::new, recipe -> recipe);

        @Override
        public MapCodec<ShulkerBoxFromPouchesRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShulkerBoxFromPouchesRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
