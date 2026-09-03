package insane96mcp.insanesurvivaloverhaul.module.combat.fletching.entity;

import insane96mcp.insanesurvivaloverhaul.mixin.accessor.StandingAndWallBlockItemAccessor;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TorchArrow extends Arrow {
    public TorchArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(FletchingFeature.TORCH_ARROW_ITEM.get());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.level().isClientSide || !FletchingFeature.torchArrowsPlaceTorches) {
            super.onHitBlock(result);
            return;
        }

        Direction direction = result.getDirection();
        BlockPos pos = result.getBlockPos().relative(direction);
        Player player = this.getOwner() instanceof Player owner ? owner : null;
        BlockPlaceContext context = new BlockPlaceContext(this.level(), player, InteractionHand.MAIN_HAND, this.getPickupItem(), result);
        BlockState stateToPlace = ((StandingAndWallBlockItemAccessor) Items.TORCH).invokeGetPlacementState(context);

        if (stateToPlace != null && this.level().getBlockState(pos).canBeReplaced()) {
            this.level().setBlock(pos, stateToPlace, 3);
            this.playSound(stateToPlace.getSoundType().getPlaceSound());
            this.discard();
        }
        super.onHitBlock(result);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity living) {
        super.doPostHurtEffects(living);
        living.setRemainingFireTicks(living.getRemainingFireTicks() + 30);
    }
}
