package insane96mcp.insanesurvivaloverhaul.module.mobs.villager.trade;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

/**
 * Loot function {@code insanesurvivaloverhaul:set_random_potion}: sets the potion contents to a random potion from {@code potions} (an id, a list of ids or a #tag),
 * like {@code minecraft:set_potion} with a random potion. Works on potions, splash and lingering potions and tipped arrows.
 */
public class SetRandomPotionFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetRandomPotionFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
            .and(RegistryCodecs.homogeneousList(Registries.POTION).fieldOf("potions").forGetter(f -> f.potions))
            .apply(instance, SetRandomPotionFunction::new));

    private final HolderSet<Potion> potions;

    public SetRandomPotionFunction(List<LootItemCondition> conditions, HolderSet<Potion> potions) {
        super(conditions);
        this.potions = potions;
    }

    @Override
    public LootItemFunctionType<SetRandomPotionFunction> getType() {
        return ISORegistries.SET_RANDOM_POTION_FUNCTION.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        this.potions.getRandomElement(context.getRandom())
                .ifPresent(potion -> stack.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, contents -> contents.withPotion(potion)));
        return stack;
    }
}
