package insane96mcp.insanesurvivaloverhaul.mixin.module.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanesurvivaloverhaul.module.items.ShulkerBoxes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ShulkerBoxBlock.class)
public class ShulkerBoxBlockMixin_ShulkerBoxes {
	/**
	 * The vanilla shulker box loot table only copies a fixed set of components from the block entity, so auto pickup is lost when the block is broken. This copies it back onto the dropped Shulker Boxes.
	 */
	@ModifyReturnValue(method = "getDrops", at = @At("RETURN"))
	public List<ItemStack> insanesurvivaloverhaul$keepAutoPickupOnDrop(List<ItemStack> original, @Local(argsOnly = true) LootParams.Builder params) {
		BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockEntity != null)
			ShulkerBoxes.copyAutoPickupToDrops(blockEntity, original);
		return original;
	}
}
