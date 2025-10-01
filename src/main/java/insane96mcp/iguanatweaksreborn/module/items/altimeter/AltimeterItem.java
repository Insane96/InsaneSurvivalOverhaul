package insane96mcp.iguanatweaksreborn.module.items.altimeter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AltimeterItem extends Item {
    public AltimeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.displayClientMessage(Component.translatable("hud_info.depth", player.getBlockY()).withStyle(ChatFormatting.GRAY), true);
        return InteractionResultHolder.success(stack);
    }
}
