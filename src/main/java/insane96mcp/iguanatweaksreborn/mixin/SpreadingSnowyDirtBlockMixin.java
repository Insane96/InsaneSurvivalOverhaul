package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.world.seasons.Seasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

@Mixin(SpreadingSnowyDirtBlock.class)
public class SpreadingSnowyDirtBlockMixin {

    @Definition(id = "pLevel", local = @Local(type = ServerLevel.class, argsOnly = true))
    @Definition(id = "getMaxLocalRawBrightness", method = "Lnet/minecraft/server/level/ServerLevel;getMaxLocalRawBrightness(Lnet/minecraft/core/BlockPos;)I")
    @Expression("pLevel.getMaxLocalRawBrightness(?) >= 9")
    @ModifyExpressionValue(method = "randomTick", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    public boolean iguanatweaksreborn$allowSpreadingGrass(boolean original, BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!Seasons.shouldPreventGrassSpreadingInAutumnAndWinter())
            return original;
        return original && (SeasonHelper.getSeasonState(pLevel).getSeason() == Season.SUMMER || SeasonHelper.getSeasonState(pLevel).getSeason() == Season.SPRING);
    }
}
