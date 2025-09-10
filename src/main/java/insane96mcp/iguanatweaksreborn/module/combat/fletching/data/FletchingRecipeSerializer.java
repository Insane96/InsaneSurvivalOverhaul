package insane96mcp.iguanatweaksreborn.module.combat.fletching.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.crafting.FletchingRecipe;
import insane96mcp.iguanatweaksreborn.setup.client.ISOBookCategory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class FletchingRecipeSerializer implements RecipeSerializer<FletchingRecipe> {
    private final CookieBaker<FletchingRecipe> factory;

    public FletchingRecipeSerializer() {
        this.factory = FletchingRecipe::new;
    }

    public FletchingRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
        ISOBookCategory category = ISOBookCategory.CODEC.byName(GsonHelper.getAsString(pJson, "category", null), ISOBookCategory.FLETCHING_MISC);
		String group = GsonHelper.getAsString(pJson, "group");
        ItemStack ingredient = getItemStack(pJson, "ingredient", true);
        ItemStack catalyst1 = getItemStack(pJson, "catalyst1", true);
        ItemStack catalyst2 = getItemStack(pJson, "catalyst2", false);
        ItemStack result = getItemStack(pJson, "result", true);
        return this.factory.create(pRecipeId, category, group, ingredient, catalyst1, catalyst2, result);
    }

    @Nullable
    public static ItemStack getItemStack(JsonObject json, String key, boolean required) {
        if (!json.has(key)) {
            if (required)
                throw new JsonSyntaxException("Missing %s, expected to find a string or object".formatted(key));
            else
                return null;
        }
        if (json.get(key).isJsonObject())
            return CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, key), true, true);
        else {
            String s1 = GsonHelper.getAsString(json, key);
            ResourceLocation resourcelocation = ResourceLocation.parse(s1);
            return new ItemStack(ForgeRegistries.ITEMS.getValue(resourcelocation));
        }
    }

    public FletchingRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
        ISOBookCategory category = pBuffer.readEnum(ISOBookCategory.class);
		String group = pBuffer.readUtf();
        ItemStack ingredient = pBuffer.readItem();
        ItemStack catalyst1 = pBuffer.readItem();
        boolean hasCatalyst2 = pBuffer.readBoolean();
        ItemStack catalyst2 = null;
        if (hasCatalyst2)
            catalyst2 = pBuffer.readItem();
        ItemStack result = pBuffer.readItem();
        return this.factory.create(pRecipeId, category, group, ingredient, catalyst1, catalyst2, result);
    }

    public void toNetwork(FriendlyByteBuf pBuffer, FletchingRecipe pRecipe) {
        pBuffer.writeEnum(pRecipe.category());
		pBuffer.writeUtf(pRecipe.getGroup());
        pBuffer.writeItem(pRecipe.getBaseIngredient());
        pBuffer.writeItem(pRecipe.getCatalyst1());
        if (pRecipe.getCatalyst2() != null) {
            pBuffer.writeBoolean(true);
            pBuffer.writeItem(pRecipe.getCatalyst2());
        }
        else {
            pBuffer.writeBoolean(false);
        }
        pBuffer.writeItem(pRecipe.getResultItem(RegistryAccess.EMPTY));
    }

    public interface CookieBaker<T extends FletchingRecipe> {
        T create(ResourceLocation pId, ISOBookCategory pCategory, String group, ItemStack baseIngredient, ItemStack catalyst1, @Nullable ItemStack catalyst2, ItemStack pResult);
    }
}