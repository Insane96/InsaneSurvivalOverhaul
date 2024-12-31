package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import insane96mcp.insanelib.base.Feature;
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
        if (!Feature.isEnabled(EnchantmentsFeature.class)
                || !EnchantmentsFeature.nerfForune)
            return;
        //I  : 80%   1x, 20%   2x. Avg: +20%
        //II : 66.7% 1x, 16.7% 2x, 16.7% 3x. Avg: +50%
        //III: 57.1% 1x, 14.3% 2x, 14.3% 3x, 14.3% 4x. Avg: 85.7%
        //IV : 50%   1x, 12.5% 2x, 12.5% 3x, 12.5% 4x, 12.5% 5x. Avg: 125%
        int i = random.nextInt(enchantmentLvl + 4) - 3;
        if (i < 0)
            i = 0;
        cir.setReturnValue(originalCount * (i + 1));
    }
}
