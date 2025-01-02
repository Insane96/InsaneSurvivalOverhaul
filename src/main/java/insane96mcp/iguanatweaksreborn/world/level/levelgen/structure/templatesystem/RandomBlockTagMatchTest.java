package insane96mcp.iguanatweaksreborn.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.iguanatweaksreborn.setup.ISTRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class RandomBlockTagMatchTest extends RuleTest {
    public static final Codec<RandomBlockTagMatchTest> CODEC = RecordCodecBuilder.create((p_259017_) -> p_259017_.group(
            TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter((p_163766_) -> p_163766_.tag),
            Codec.FLOAT.fieldOf("probability").forGetter((p_163764_) -> p_163764_.probability))
    .apply(p_259017_, RandomBlockTagMatchTest::new));
    private final TagKey<Block> tag;
    private final float probability;

    public RandomBlockTagMatchTest(TagKey<Block> tag, float probability) {
        this.tag = tag;
        this.probability = probability;
    }

    public boolean test(BlockState state, RandomSource random) {
        return state.is(this.tag) && random.nextFloat() < this.probability;
    }

    protected RuleTestType<?> getType() {
        return ISTRegistries.RANDOM_BLOCK_TAG_MATCH.get();
    }

    public static class Type implements RuleTestType<RandomBlockTagMatchTest> {
        @Override
        public Codec<RandomBlockTagMatchTest> codec() {
            return CODEC;
        }
    }
}
