package insane96mcp.insanesurvivaloverhaul.module.world.coalfire;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.List;


public class FirestarterItem extends FlintAndSteelItem {
    public FirestarterItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 60;
    }

    @Override
    public InteractionResult useOn(UseOnContext p_41297_) {
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player))
            return stack;
        BlockHitResult blockHitResult = getPlayerPOVHitResult(player.level(), player, ClipContext.Fluid.NONE);
        BlockPos blockpos = blockHitResult.getBlockPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.is(Blocks.TNT)) {
            Blocks.TNT.onCaughtFire(blockstate, level, blockpos, blockHitResult.getDirection(), player);
            level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 11);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
            return stack;
        }
        BlockState blockstate2 = blockstate.getToolModifiedState(new UseOnContext(player, player.getUsedItemHand(), blockHitResult), ItemAbilities.FIRESTARTER_LIGHT, false);
        if (blockstate2 == null) {
            BlockPos blockpos1 = blockpos.relative(blockHitResult.getDirection());
            if (BaseFireBlock.canBePlacedAt(level, blockpos1, player.getDirection())) {
                level.playSound(player, blockpos1, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                level.setBlock(blockpos1, BaseFireBlock.getState(level, blockpos1), 11);
                level.gameEvent(player, GameEvent.BLOCK_PLACE, blockpos);
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, blockpos1, stack);
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
                }
            }
        } else {
            level.playSound(player, blockpos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            level.setBlock(blockpos, blockstate2, 11);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, blockpos);
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
        }
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        BlockHitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (hitresult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = hitresult.getBlockPos();
            if (!level.mayInteract(player, blockpos)) {
                return InteractionResultHolder.pass(itemstack);
            }
            player.startUsingItem(interactionHand);
            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        }
        return InteractionResultHolder.pass(itemstack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int tickCount) {
        if (!(livingEntity instanceof Player player))
            return;
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            player.stopUsingItem();
        }
        else {
            if (tickCount % 3 == 1) {
                if (tickCount <= 45)
                    level.addParticle(ParticleTypes.SMOKE, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z, 0.0d, 0.1d, 0.0d);
                if (tickCount <= 20)
                    level.addParticle(ParticleTypes.FLAME, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z, 0.0d, 0.1d, 0.0d);
            }

            if (tickCount == 45 || tickCount == 20) {
                level.playSound(player, hitResult.getBlockPos(), SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS);
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack p_40678_) {
        return UseAnim.BOW;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("item.insanesurvivaloverhaul.firestarter.tooltip").withStyle(ChatFormatting.GRAY));
    }
}
