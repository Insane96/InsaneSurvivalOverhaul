package insane96mcp.iguanatweaksreborn.data.lootmodifier;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanelib.util.MathHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ReplaceLootModifier extends LootModifier {
    public static final Supplier<Codec<ReplaceLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.create(inst -> codecStart(inst).and(
                    inst.group(
                            ForgeRegistries.ITEMS.getCodec().fieldOf("item_to_replace").forGetter(m -> m.itemToReplace),
                            ForgeRegistries.ITEMS.getCodec().fieldOf("new_item").forGetter(m -> m.newItem),
                            Codec.INT.optionalFieldOf("amount_to_replace", -1).forGetter(m -> m.amountToReplace),
                            Codec.list(Codec.FLOAT).optionalFieldOf("chances", List.of(1f)).forGetter(m -> m.chances),
                            Codec.list(Codec.FLOAT).optionalFieldOf("multipliers", List.of(1f)).forGetter(m -> m.multipliers),
                            Codec.BOOL.optionalFieldOf("keep_durability", false).forGetter(m -> m.keepDurability),
                            Codec.BOOL.optionalFieldOf("chests_only", false).forGetter(m -> m.chestsOnly)
                    )).apply(inst, ReplaceLootModifier::new)
            ));

    //Item to replace
    private final Item itemToReplace;
    //Item to replace with
    private final Item newItem;
    //Amount to replace. Set to -1 to replace all
    private int amountToReplace;
    //List of chances, where the first is for no-fortune applied and the subsequent ones are for increasing level of fortune
    private List<Float> chances;
    //List of multipliers, where the first is for no-fortune applied and the subsequent ones are for increasing level of fortune
    private List<Float> multipliers;
    /**
     * If true, durability % is transposed to the new item. Only works if amountToReplace is -1
     */
    private boolean keepDurability;
    private boolean chestsOnly;

    public ReplaceLootModifier(LootItemCondition[] conditionsIn, Item itemToReplace, Item newItem) {
        this(conditionsIn, itemToReplace, newItem, -1, List.of(1f), List.of(1f), false, false);
    }

    public ReplaceLootModifier(LootItemCondition[] conditionsIn, Item itemToReplace, Item newItem, int amountToReplace, List<Float> chances, List<Float> multipliers, boolean keepDurability, boolean chestsOnly) {
        super(conditionsIn);
        this.itemToReplace = itemToReplace;
        this.newItem = newItem;
        this.amountToReplace = amountToReplace;
        this.chances = chances;
        this.multipliers = multipliers;
        this.keepDurability = keepDurability;
        this.chestsOnly = chestsOnly;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (this.chestsOnly && !context.getQueriedLootTableId().getPath().contains("chests/"))
            return generatedLoot;

        List<ItemStack> toRemove = new ArrayList<>();
        List<ItemStack> toAdd = new ArrayList<>();
        generatedLoot.stream().filter(stack -> stack.getItem().equals(itemToReplace))
                .forEach(stack -> {
                    ItemStack toolStack = context.getParamOrNull(LootContextParams.TOOL);
                    int fortuneLvl = toolStack != null ? toolStack.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE) : 0;
                    float chance = this.chances.get(Math.min(fortuneLvl, this.chances.size() - 1));
                    if (context.getRandom().nextDouble() >= chance)
                        return;

                    float multiplier = this.multipliers.get(Math.min(fortuneLvl, this.multipliers.size() - 1));
                    boolean keepDurability = this.keepDurability && itemToReplace.canBeDepleted() && newItem.canBeDepleted();
                    float percentageDurability = 1f;
                    if (keepDurability) {
                        percentageDurability = (float)stack.getDamageValue() / stack.getMaxDamage();
                    }
                    toRemove.add(stack);
                    if (amountToReplace == -1) {
                        int newAmount = MathHelper.getAmountWithDecimalChance(context.getRandom(), stack.getCount() * multiplier);
                        ItemStack newStack = new ItemStack(newItem, newAmount);
                        if (keepDurability)
                            newStack.setDamageValue((int) (newStack.getMaxDamage() * percentageDurability));
                        CompoundTag tag = stack.getTag();
                        if (tag != null) {
                            tag.remove("Damage");
                            newStack.setTag(tag);
                        }
                        toAdd.add(newStack);
                    }
                    else {
                        int newAmount = MathHelper.getAmountWithDecimalChance(context.getRandom(), Math.min(stack.getCount(), amountToReplace) * multiplier);
                        ItemStack newStack = new ItemStack(this.newItem, newAmount);
                        CompoundTag tag = stack.getTag();
                        if (tag != null) {
                            tag.remove("Damage");
                            newStack.setTag(tag);
                        }

                        toAdd.add(newStack);
                        if (amountToReplace < stack.getCount()) {
                            ItemStack oldStack = new ItemStack(this.itemToReplace, stack.getCount() - amountToReplace);
                            toAdd.add(oldStack);
                        }
                    }
                });

        generatedLoot.removeAll(toRemove);
        generatedLoot.addAll(toAdd);
        return generatedLoot;
    }

    public static class Builder {
        final ReplaceLootModifier replaceLootModifier;
        public Builder(Item itemToReplace, Item newItem) {
            this(new LootItemCondition[0], itemToReplace, newItem);
        }

        public Builder(LootItemCondition[] conditionsIn, Item itemToReplace, Item newItem) {
            replaceLootModifier = new ReplaceLootModifier(conditionsIn, itemToReplace, newItem);
        }

        public Builder(EntityType<?> entityType, Item itemToReplace, Item newItem) {
            this.replaceLootModifier = new ReplaceLootModifier(new LootItemCondition[] {LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, new EntityPredicate.Builder().entityType(EntityTypePredicate.of(entityType)).build()).build()}, itemToReplace, newItem);
        }

        public Builder setAmountToReplace(int amount) {
            replaceLootModifier.amountToReplace = amount;
            return this;
        }

        public Builder setChances(List<Float> chances) {
            replaceLootModifier.chances = chances;
            return this;
        }

        public Builder setMultipliers(List<Float> multipliers) {
            replaceLootModifier.multipliers = multipliers;
            return this;
        }

        public Builder applyToChestsOnly() {
            replaceLootModifier.chestsOnly = true;
            return this;
        }

        public Builder keepDurability() {
            replaceLootModifier.keepDurability = true;
            return this;
        }

        public ReplaceLootModifier build() {
            return replaceLootModifier;
        }
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
