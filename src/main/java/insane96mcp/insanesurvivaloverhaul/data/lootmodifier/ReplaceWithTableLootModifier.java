package insane96mcp.insanesurvivaloverhaul.data.lootmodifier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Like {@code neoforge:add_table}, but the generated loot is discarded first: the target table's loot is replaced by the subtable's.
 * Loot modifiers are not run for the subtable. If the subtable is missing, the loot is left unchanged.
 * <pre>
 * {
 *   "type": "insanesurvivaloverhaul:replace_with_table",
 *   "conditions": [],
 *   "table": "namespace:loot_table_id"
 * }
 * </pre>
 */
public class ReplaceWithTableLootModifier extends LootModifier {
    public static final MapCodec<ReplaceWithTableLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(glm -> glm.conditions),
            ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").forGetter(glm -> glm.table)
    ).apply(instance, ReplaceWithTableLootModifier::new));

    private final ResourceKey<LootTable> table;

    public ReplaceWithTableLootModifier(LootItemCondition[] conditions, ResourceKey<LootTable> table) {
        super(conditions);
        this.table = table;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        context.getResolver().get(Registries.LOOT_TABLE, this.table).ifPresent(replacementTable -> {
            generatedLoot.clear();
            replacementTable.value().getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
        });
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ISORegistries.REPLACE_WITH_TABLE_LOOT_MODIFIER.get();
    }
}
