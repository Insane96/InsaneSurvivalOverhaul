package insane96mcp.insanesurvivaloverhaul.module.combat.fletching;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class FletchingRecipeSerializer implements RecipeSerializer<FletchingRecipe> {
    private static final MapCodec<FletchingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SizedIngredient.FLAT_CODEC.fieldOf("ingredient").forGetter(FletchingRecipe::getIngredient),
                    SizedIngredient.FLAT_CODEC.fieldOf("catalyst1").forGetter(FletchingRecipe::getCatalyst1),
                    SizedIngredient.FLAT_CODEC.optionalFieldOf("catalyst2").forGetter(FletchingRecipe::getCatalyst2),
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(FletchingRecipe::getResult))
            .apply(instance, FletchingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC, FletchingRecipe::getIngredient,
            SizedIngredient.STREAM_CODEC, FletchingRecipe::getCatalyst1,
            ByteBufCodecs.optional(SizedIngredient.STREAM_CODEC), FletchingRecipe::getCatalyst2,
            ItemStack.STREAM_CODEC, FletchingRecipe::getResult,
            FletchingRecipe::new);

    @Override
    public MapCodec<FletchingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
