package insane96mcp.iguanatweaksreborn.data.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class EnchantRandomlyWeightlessFunction extends LootItemConditionalFunction {
    final NumberProvider count;
    final boolean maxLvl;
    final boolean treasure;

    protected EnchantRandomlyWeightlessFunction(LootItemCondition[] pPredicates, NumberProvider count, boolean maxLvl, boolean treasure) {
        super(pPredicates);
        this.count = count;
        this.maxLvl = maxLvl;
        this.treasure = treasure;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        int amount = this.count.getInt(context);
        boolean isBook = stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
        List<Enchantment> eligibleEnchantments = new ArrayList<>(ForgeRegistries.ENCHANTMENTS.getValues()
                .stream()
                .filter(ench -> (isBook || ench.canEnchant(stack))
                        && ench.isDiscoverable()
                        && !EnchantmentsFeature.isEnchantmentDisabled(ench)
                        && (!ench.isTreasureOnly() || this.treasure))
                .toList());
        if (eligibleEnchantments.isEmpty())
            return stack;
        List<Enchantment> applied = new ArrayList<>();
        ItemStack newStack = stack.copy();
        for (int i = 0; i < amount; i++) {
            // Only consider enchantments compatible with all already-applied ones
            List<Enchantment> compatible = eligibleEnchantments.stream()
                    .filter(e -> applied.stream().allMatch(e::isCompatibleWith))
                    .toList();
            if (compatible.isEmpty())
                break;

            Enchantment chosen = compatible.get(random.nextInt(compatible.size()));
            newStack = applyEnchantment(newStack, chosen, random);
            applied.add(chosen);
            eligibleEnchantments.remove(chosen);
            if (eligibleEnchantments.isEmpty())
                break;
        }
        return newStack;
    }

    private ItemStack applyEnchantment(ItemStack stack, Enchantment enchantment, RandomSource random) {
        int lvl = Mth.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
        if (this.maxLvl)
            lvl = enchantment.getMaxLevel();
        if (stack.is(Items.BOOK))
            stack = new ItemStack(Items.ENCHANTED_BOOK);
        if (stack.is(Items.ENCHANTED_BOOK))
            EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(enchantment, lvl));
        else
            stack.enchant(enchantment, lvl);
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return ISORegistries.ENCHANT_RANDOMLY_WEIGHTLESS.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantRandomlyWeightlessFunction> {
        public void serialize(JsonObject jObject, EnchantRandomlyWeightlessFunction enchantRandomlyWeightlessFunction, JsonSerializationContext context) {
            jObject.add("count", context.serialize(enchantRandomlyWeightlessFunction.count));
            jObject.addProperty("max_lvl", enchantRandomlyWeightlessFunction.maxLvl);
            jObject.addProperty("treasure", enchantRandomlyWeightlessFunction.treasure);
            super.serialize(jObject, enchantRandomlyWeightlessFunction, context);
        }

        public EnchantRandomlyWeightlessFunction deserialize(JsonObject jObject, JsonDeserializationContext context, LootItemCondition[] conditions) {
            NumberProvider enchantmentsCount = GsonHelper.getAsObject(jObject, "count", context, NumberProvider.class);
            boolean maxLvl = GsonHelper.getAsBoolean(jObject, "max_lvl");
            boolean treasure = GsonHelper.getAsBoolean(jObject, "treasure", false);
            return new EnchantRandomlyWeightlessFunction(conditions, enchantmentsCount, maxLvl, treasure);
        }
    }
}
