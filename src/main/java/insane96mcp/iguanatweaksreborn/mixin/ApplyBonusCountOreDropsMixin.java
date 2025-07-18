package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ApplyBonusCount.OreDrops.class)
public class ApplyBonusCountOreDropsMixin {
    @Inject(method = "calculateNewCount", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    public void onCalculateNewCount(RandomSource random, int originalCount, int enchantmentLvl, CallbackInfoReturnable<Integer> cir) {
        if (!EnchantmentsFeature.isBonusLootEnchantmentReworkEnabled()
                || enchantmentLvl <= 0
                || !EnchantmentsFeature.isLuckOneLevelOnly())
            return;

        // Fixed to level II
        int i = random.nextInt(4) - 1;
        if (i < 0)
            i = 0;
        cir.setReturnValue(originalCount * (i + 1));
    }
}
