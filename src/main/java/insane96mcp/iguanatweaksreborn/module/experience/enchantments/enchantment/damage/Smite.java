package insane96mcp.iguanatweaksreborn.module.experience.enchantments.enchantment.damage;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalTweaks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class Smite extends BonusDamageEnchantment {
    public static final TagKey<EntityType<?>> AFFECTED_BY_SMITE = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(InsaneSurvivalTweaks.MOD_ID, "enchanting/smite"));
    public Smite() {
        super(Rarity.UNCOMMON, new EquipmentSlot[]{ EquipmentSlot.MAINHAND });
    }


    @Override
    public void doPostAttack(@NotNull LivingEntity attacker, @NotNull Entity target, int lvl) {
        if (!(target instanceof LivingEntity livingentity))
            return;

        if (lvl > 0 && livingentity.getType().is(AFFECTED_BY_SMITE)) {
            int i = 20 + attacker.getRandom().nextInt(10 * lvl);
            livingentity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, i, 1));
        }
    }

    @Override
    public boolean isAffectedByEnchantment(Entity target) {
        return target.getType().is(AFFECTED_BY_SMITE);
    }
}
