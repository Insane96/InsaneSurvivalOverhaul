package insane96mcp.insanesurvivaloverhaul.module.misc.fireworks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

/**
 * Like {@link net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer} but with an additional optional "count" field
 */
public class CountCraftingRecipeSerializer<T extends CraftingRecipe> implements RecipeSerializer<T> {
    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    public CountCraftingRecipeSerializer(Factory<T> constructor, Function<T, Integer> countGetter, int defaultCount) {
        this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category),
                        ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", defaultCount).forGetter(countGetter)
                ).apply(instance, constructor::create)
        );
        this.streamCodec = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CraftingRecipe::category,
                ByteBufCodecs.VAR_INT, countGetter,
                constructor::create
        );
    }

    @Override
    public MapCodec<T> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return this.streamCodec;
    }

    @FunctionalInterface
    public interface Factory<T extends CraftingRecipe> {
        T create(CraftingBookCategory category, int count);
    }
}
