package insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

/**
 * Loot function {@code insanesurvivaloverhaul:set_random_item}: replaces the item with a random one from {@code items} (an id, a list of ids or a #tag),
 * keeping count and components, like {@code minecraft:set_item}.
 */
public class SetRandomItemFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetRandomItemFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
            .and(RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(f -> f.items))
            .apply(instance, SetRandomItemFunction::new));

    private final HolderSet<Item> items;

    public SetRandomItemFunction(List<LootItemCondition> conditions, HolderSet<Item> items) {
        super(conditions);
        this.items = items;
    }

    @Override
    public LootItemFunctionType<SetRandomItemFunction> getType() {
        return ISORegistries.SET_RANDOM_ITEM_FUNCTION.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        return this.items.getRandomElement(context.getRandom())
                .map(item -> stack.transmuteCopy(item.value()))
                .orElse(stack);
    }
}
