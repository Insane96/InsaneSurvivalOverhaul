package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.EnumMap;
import java.util.function.Supplier;

@Mixin(ArmorMaterials.class)
public interface ArmorMaterialsAccessor {
    @Invoker
    static Holder<ArmorMaterial> invokeRegister(
            String name,
            EnumMap<ArmorItem.Type, Integer> defense,
            int enchantmentValue,
            Holder<SoundEvent> equipSound,
            float toughness,
            float knockbackResistance,
            Supplier<Ingredient> repairIngredient
    ) {
        throw new AssertionError();
    }
}
