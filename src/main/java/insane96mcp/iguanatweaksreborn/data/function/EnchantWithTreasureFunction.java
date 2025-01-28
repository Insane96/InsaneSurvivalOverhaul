package insane96mcp.iguanatweaksreborn.data.function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.utils.ISOLogHelper;
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
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class EnchantWithTreasureFunction extends LootItemConditionalFunction {
    final boolean allowCurses;
    final boolean allowTreasure;

    protected EnchantWithTreasureFunction(LootItemCondition[] pPredicates, boolean allowCurses, boolean allowTreasure) {
        super(pPredicates);
        this.allowCurses = allowCurses;
        this.allowTreasure = allowTreasure;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        Enchantment enchantment;
        boolean isBook = stack.is(Items.BOOK) || stack.is(Items.ENCHANTED_BOOK);
        List<Enchantment> list = ForgeRegistries.ENCHANTMENTS.getValues()
                .stream()
                .filter(ench -> (isBook || ench.canEnchant(stack))
                        && ench.isDiscoverable()
                        && ench.isTreasureOnly()
                        && (!ench.isCurse() || this.allowCurses)
                        && (ench.isCurse() || this.allowTreasure))
                .toList();

        if (list.isEmpty()) {
            ISOLogHelper.warn("Couldn't find a compatible treasure enchantment for {}", stack);
            return stack;
        }

        enchantment = list.get(random.nextInt(list.size()));

        return enchantItem(stack, enchantment, random);
    }

    @Override
    public LootItemFunctionType getType() {
        return ISORegistries.ENCHANT_WITH_TREASURE.get();
    }

    private static ItemStack enchantItem(ItemStack pStack, Enchantment pEnchantment, RandomSource pRandom) {
        int i = Mth.nextInt(pRandom, pEnchantment.getMinLevel(), pEnchantment.getMaxLevel());
        if (pStack.is(Items.BOOK)) {
            pStack = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantedBookItem.addEnchantment(pStack, new EnchantmentInstance(pEnchantment, i));
        }
        else if (pStack.is(Items.ENCHANTED_BOOK))
            EnchantedBookItem.addEnchantment(pStack, new EnchantmentInstance(pEnchantment, i));
        else
            pStack.enchant(pEnchantment, i);

        return pStack;
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantWithTreasureFunction> {
        public void serialize(JsonObject pJson, EnchantWithTreasureFunction pValue, JsonSerializationContext pSerializationContext) {
            pJson.addProperty("allow_curses", pValue.allowCurses);
            pJson.addProperty("allow_treasure", pValue.allowTreasure);
            super.serialize(pJson, pValue, pSerializationContext);
        }

        public EnchantWithTreasureFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
            boolean allowCurses = GsonHelper.getAsBoolean(pObject, "allow_curses", true);
            boolean allowTreasure = GsonHelper.getAsBoolean(pObject, "allow_treasure", true);
            return new EnchantWithTreasureFunction(pConditions, allowCurses, allowTreasure);
        }
    }
}
