package insane96mcp.iguanatweaksreborn.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.iguanatweaksreborn.module.hungerhealth.fooddrinks.FoodDrinks;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
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
public abstract class PumpkinBlockMixin {
    @Definition(id = "pLevel", local = @Local(type = Level.class, argsOnly = true))
    @Definition(id = "isClientSide", field = "Lnet/minecraft/world/level/Level;isClientSide:Z")
    @Expression("pLevel.isClientSide")
    @WrapOperation(method = "use", at = @At("MIXINEXTRAS:EXPRESSION"))
    public boolean iguanatweaksreborn$replacePumpkinShearWithLootTable(Level instance, Operation<Boolean> original, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!Feature.isEnabled(FoodDrinks.class) || !FoodDrinks.addPumpkinShearLootTable)
            return original.call(instance);
        boolean isClientSide = original.call(instance);
        if (isClientSide)
            return true;
        ItemStack itemstack = player.getItemInHand(hand);
        Direction direction = hit.getDirection();
        Direction direction1 = direction.getAxis() == Direction.Axis.Y ? player.getDirection().getOpposite() : direction;
        level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.setBlock(pos, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, direction1), 11);
        LootTable lootTable = ((ServerLevel) level).getServer().getLootData().getLootTable(FoodDrinks.PUMPKIN_SHEAR_LOOT_TABLE);
        LootParams lootParams = (new LootParams.Builder((ServerLevel) level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, player.getItemInHand(hand)).withOptionalParameter(LootContextParams.THIS_ENTITY, player).withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
        List<ItemStack> list = lootTable.getRandomItems(lootParams);
        list.forEach(itemStack -> {
            ItemEntity itementity = new ItemEntity(level, pos.getX() + 0.5D + direction1.getStepX() * 0.65D, pos.getY() + 0.1D, pos.getZ() + 0.5D + direction1.getStepZ() * 0.65D, itemStack);
            itementity.setDeltaMovement(0.05D * (double)direction1.getStepX() + level.random.nextDouble() * 0.02D, 0.05D, 0.05D * (double)direction1.getStepZ() + level.random.nextDouble() * 0.02D);
            level.addFreshEntity(itementity);
        });
        itemstack.hurtAndBreak(1, player, (player1) -> player1.broadcastBreakEvent(hand));
        level.gameEvent(player, GameEvent.SHEAR, pos);
        player.awardStat(Stats.ITEM_USED.get(Items.SHEARS));
        return false;
    }
}