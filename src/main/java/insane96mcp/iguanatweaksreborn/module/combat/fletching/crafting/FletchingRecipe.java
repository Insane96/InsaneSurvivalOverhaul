package insane96mcp.iguanatweaksreborn.module.combat.fletching.crafting;

import insane96mcp.iguanatweaksreborn.module.combat.fletching.Fletching;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.inventory.FletchingMenu;
import insane96mcp.iguanatweaksreborn.setup.client.ISOBookCategory;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class FletchingRecipe implements Recipe<Container> {
    protected final RecipeType<?> type;
    protected final ResourceLocation id;
    private final ISOBookCategory category;
	private final String group;
    final ItemStack baseIngredient;
    final ItemStack catalyst1;
    @Nullable
    final ItemStack catalyst2;
    private final ItemStack result;

    public FletchingRecipe(ResourceLocation pId, ISOBookCategory pCategory, String group, ItemStack baseIngredient, ItemStack catalyst1, @Nullable ItemStack catalyst2, ItemStack pResult) {
        this.type = Fletching.FLETCHING_RECIPE_TYPE.get();
		this.id = pId;
		this.category = pCategory;
		this.group = group;
        this.baseIngredient = baseIngredient;
        this.catalyst1 = catalyst1;
        this.catalyst2 = catalyst2;
        this.result = pResult;
    }

    @Override
    public boolean matches(Container container, Level pLevel) {
        ItemStack ingredient = container.getItem(FletchingMenu.INGREDIENT_SLOT);
        ItemStack catalyst1 = container.getItem(FletchingMenu.CATALYST_1_SLOT);
        ItemStack catalyst2 = container.getItem(FletchingMenu.CATALYST_2_SLOT);
        return ingredient.is(this.baseIngredient.getItem()) && ingredient.getCount() >= this.baseIngredient.getCount()
                && catalyst1.is(this.catalyst1.getItem()) && catalyst1.getCount() >= this.catalyst1.getCount()
                && (this.catalyst2 == null || (catalyst2.is(this.catalyst2.getItem()) && catalyst2.getCount() >= this.catalyst2.getCount()));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> nonnulllist = NonNullList.create();
        nonnulllist.add(Ingredient.of(this.baseIngredient));
        nonnulllist.add(Ingredient.of(this.catalyst1));
        if (this.catalyst2 != null)
            nonnulllist.add(Ingredient.of(this.catalyst2));
        return nonnulllist;
    }

    public ItemStack getBaseIngredient() {
        return this.baseIngredient;
    }

    public ItemStack getCatalyst1() {
        return this.catalyst1;
    }

    @Nullable
    public ItemStack getCatalyst2() {
        return this.catalyst2;
    }

    public int getIngredientAmount(int slot) {
        switch (slot) {
            case 0 -> {
                return this.baseIngredient.getCount();
            }
            case 1 -> {
                return this.catalyst1.getCount();
            }
            case 2 -> {
                if (this.catalyst2 != null)
                    return this.catalyst2.getCount();
                return 0;
            }
        }
        return 0;
    }

    public ItemStack getResult() {
        return this.result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Fletching.FLETCHING_RECIPE_SERIALIZER.get();
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public RecipeType<?> getType() {
        return this.type;
    }

    public ISOBookCategory category() {
        return this.category;
    }

	@Override
	public String getGroup() {
		return this.group;
	}

	@Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Items.FLETCHING_TABLE);
    }
}
