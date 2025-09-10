package insane96mcp.iguanatweaksreborn.module.combat.fletching.entity.projectile;

import insane96mcp.iguanatweaksreborn.mixin.StandingAndWallBlockItemAccessor;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.Fletching;
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
    public TorchArrow(EntityType<? extends Arrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public TorchArrow(Level pLevel, double pX, double pY, double pZ) {
        super(pLevel, pX, pY, pZ);
    }

    public TorchArrow(Level pLevel, LivingEntity pShooter) {
        super(pLevel, pShooter);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if (this.level().isClientSide || !Fletching.torchArrowsPlaceTorches) {
            super.onHitBlock(pResult);
            return;
        }
        Direction direction = pResult.getDirection();
        BlockPos pos = pResult.getBlockPos().relative(direction);
        Player player = null;
        if (this.getOwner() instanceof Player)
            player = (Player) this.getOwner();
        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(this.level(), player, InteractionHand.MAIN_HAND, getPickupItem(), pResult);
        BlockState stateToPlace = ((StandingAndWallBlockItemAccessor)Items.TORCH).invokeGetPlacementState(blockPlaceContext);

        if (stateToPlace != null && this.level().getBlockState(pos).canBeReplaced()) {
            this.level().setBlock(pos, stateToPlace, 3);
            this.playSound(stateToPlace.getSoundType().getPlaceSound());
            this.discard();
        }
        super.onHitBlock(pResult);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity pLiving) {
        super.doPostHurtEffects(pLiving);
        pLiving.setRemainingFireTicks(pLiving.getRemainingFireTicks() + 30);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Fletching.TORCH_ARROW_ITEM.get());
    }
}
