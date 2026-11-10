package insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import java.util.List;
import java.util.Optional;

/**
 * The pool of trades of a villager level or of a wandering trader pool, with the fields of the 26.1 {@code trade_set} format.
 * <p>
 * Ids: {@code <profession namespace>:<profession path>/level_<1-5>} (mod professions included) and {@code minecraft:wandering_trader/<buying|uncommon|common>}.
 * <p>
 * ISO extension: {@code remove} ({@code none}, {@code vanilla} or {@code all}) removes the vanilla listings, or every listing (mods included), before adding the pool.
 * Villagers always roll 2 trades per level, so {@code amount}, {@code allow_duplicates} and {@code random_sequence} are only used by the wandering trader.
 */
public record TradeSet(List<ExtraCodecs.TagOrElementLocation> trades, Optional<NumberProvider> amount, boolean allowDuplicates,
                       Optional<ResourceLocation> randomSequence, RemoveMode remove) {
    private static final Codec<List<ExtraCodecs.TagOrElementLocation>> TRADES_CODEC = Codec.withAlternative(
            ExtraCodecs.TAG_OR_ELEMENT_ID.listOf(),
            ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(List::of, List::getFirst));

    public static final Codec<TradeSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TRADES_CODEC.optionalFieldOf("trades", List.of()).forGetter(TradeSet::trades),
            NumberProviders.CODEC.optionalFieldOf("amount").forGetter(TradeSet::amount),
            Codec.BOOL.optionalFieldOf("allow_duplicates", false).forGetter(TradeSet::allowDuplicates),
            ResourceLocation.CODEC.optionalFieldOf("random_sequence").forGetter(TradeSet::randomSequence),
            RemoveMode.CODEC.optionalFieldOf("remove", RemoveMode.NONE).forGetter(TradeSet::remove)
    ).apply(instance, TradeSet::new));

    public enum RemoveMode implements StringRepresentable {
        /** Keeps every existing listing */
        NONE("none"),
        /** Removes the vanilla listings, keeps the ones added by mods */
        VANILLA("vanilla"),
        /** Removes every listing, mods included */
        ALL("all");

        public static final Codec<RemoveMode> CODEC = StringRepresentable.fromEnum(RemoveMode::values);

        private final String name;

        RemoveMode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
