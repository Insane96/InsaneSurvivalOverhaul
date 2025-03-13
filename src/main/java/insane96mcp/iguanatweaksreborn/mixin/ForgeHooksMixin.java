package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.experience.enchantments.EnchantmentsFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ForgeHooks.class)
public abstract class ForgeHooksMixin {
	@ModifyExpressionValue(method = "dropXpForBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getEnchantmentLevel(Lnet/minecraft/world/item/enchantment/Enchantment;)I", ordinal = 0), remap = false)
    private static int iguanatweaksreborn$onItemEnchantmentLevel(int original) {
		return EnchantmentsFeature.getLuckLevel(original, original);
	}

	@WrapOperation(method = "lambda$onGrindstoneTake$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;atCenterOf(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/world/phys/Vec3;"))
	private static Vec3 iguanatweaksreborn$spawnXpInABetterPlace(Vec3i pos, Operation<Vec3> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos blockPos) {
		BlockState state = level.getBlockState(blockPos);
		Vec3 originalPos = original.call(pos);
		if (!state.hasProperty(GrindstoneBlock.FACE) || !state.hasProperty(GrindstoneBlock.FACING))
			return originalPos;
		AttachFace face = state.getValue(GrindstoneBlock.FACE);
		Direction direction = state.getValue(GrindstoneBlock.FACING);
		if (face == AttachFace.CEILING)
			return originalPos.relative(Direction.DOWN, 0.6f);
		else if (face == AttachFace.FLOOR)
			return originalPos.relative(Direction.UP, 0.6f);
		return originalPos.relative(direction, 0.6f);
	}
}
