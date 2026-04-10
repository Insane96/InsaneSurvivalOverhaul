package insane96mcp.insanesurvivaloverhaul.mixin.module.hungerhealth.fooddrinks;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.fooddrinks.FoodDrinks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(PumpkinBlock.class)
public abstract class PumpkinBlockMixin_FoodDrinks {
    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public boolean insanesurvivaloverhaulreplacePumpkinShearWithLootTable(Level instance, Entity entity, Operation<Boolean> original, ItemStack usedStack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, @Local(ordinal = 1) Direction direction) {
        if (!Feature.isEnabled(FoodDrinks.class)
                || !FoodDrinks.addPumpkinShearLootTable)
            return original.call(instance, entity);
        LootTable lootTable = ((ServerLevel) instance).getServer().reloadableRegistries().getLootTable(FoodDrinks.PUMPKIN_SHEAR_LOOT_TABLE);
        LootParams lootParams = (new LootParams.Builder((ServerLevel) instance)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, player.getItemInHand(interactionHand)).withOptionalParameter(LootContextParams.THIS_ENTITY, player).withParameter(LootContextParams.BLOCK_STATE, blockState).create(LootContextParamSets.BLOCK);
        List<ItemStack> list = lootTable.getRandomItems(lootParams);
        list.forEach(itemStack -> {
            ItemEntity itementity = new ItemEntity(instance, pos.getX() + 0.5D + direction.getStepX() * 0.65D, pos.getY() + 0.1D, pos.getZ() + 0.5D + direction.getStepZ() * 0.65D, itemStack);
            itementity.setDeltaMovement(0.05D * (double)direction.getStepX() + instance.random.nextDouble() * 0.02D, 0.05D, 0.05D * (double)direction.getStepZ() + level.random.nextDouble() * 0.02D);
            instance.addFreshEntity(itementity);
        });
        return false;
    }
}