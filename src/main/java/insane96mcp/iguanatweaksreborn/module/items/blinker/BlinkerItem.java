package insane96mcp.iguanatweaksreborn.module.items.blinker;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class BlinkerItem extends Item {

    public BlinkerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isPassenger())
            return InteractionResultHolder.fail(stack);
        int durabilityLeft = stack.getMaxDamage() - stack.getDamageValue();
        if (durabilityLeft <= 1) {
            player.displayClientMessage(Component.translatable("item.iguanatweaksreborn.blinker.empty"), true);
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        timeCharged = getUseDuration(stack) - timeCharged;
        if (timeCharged < 3)
            return;
        int durabilityLeft = stack.getMaxDamage() - stack.getDamageValue();
        if (durabilityLeft <= 1 && livingEntity instanceof Player player && !player.getAbilities().instabuild)
            return;

        float distance = Math.min(Math.min(timeCharged - 3, durabilityLeft - 1), 32);
        double yawRadians = Math.toRadians(livingEntity.getYRot());
        double pitchRadians = Math.toRadians(livingEntity.getXRot());

        double x = livingEntity.getX() - Math.sin(yawRadians) * Math.cos(pitchRadians) * distance;
        double y = livingEntity.getEyeY() - Math.sin(pitchRadians) * distance;
        double z = livingEntity.getZ() + Math.cos(yawRadians) * Math.cos(pitchRadians) * distance;

        while (distance > 0 && !isSafePosition(level, x, y - livingEntity.getEyeHeight() / 2f, z, livingEntity)) {
            distance -= 0.5f;
            x = livingEntity.getX() - Math.sin(yawRadians) * Math.cos(pitchRadians) * distance;
            y = livingEntity.getEyeY() - Math.sin(pitchRadians) * distance;
            z = livingEntity.getZ() + Math.cos(yawRadians) * Math.cos(pitchRadians) * distance;
        }

        livingEntity.teleportTo(x, y, z);
        if (livingEntity instanceof Player player && !player.getAbilities().instabuild) {
            stack.hurt(Mth.ceil(distance), level.getRandom(), null);
            if (stack.getMaxDamage() - stack.getDamageValue() <= 1)
                livingEntity.broadcastBreakEvent(livingEntity.getUsedItemHand());
        }
        level.gameEvent(GameEvent.TELEPORT, livingEntity.position(), GameEvent.Context.of(livingEntity));
        SoundEvent soundevent = livingEntity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
        level.playSound(null, x, y, z, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
        livingEntity.playSound(soundevent, 1.0F, 1.0F);
        if (livingEntity instanceof Player)
            ((Player) livingEntity).getCooldowns().addCooldown(this, (int) Math.min(20, distance * 2f));
    }

    private boolean isSafePosition(Level level, double x, double y, double z, LivingEntity entity) {
        BlockPos pos = BlockPos.containing(x, y, z);
        return !level.getBlockState(pos).isSuffocating(level, pos);
    }
}
