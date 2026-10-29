package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import static insane96mcp.insanesurvivaloverhaul.module.items.DisabledItems.*;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.ITEMS, canBeDisabled = false)
public class DisabledItemsClient extends Feature {
	@Config(description = "If set to true items in the 'no_damage', 'no_efficiency', 'no_equip' and 'disabled' item tags will get a tooltip.")
	public static Boolean addTooltip = true;

	@SubscribeEvent
	public void processInput(InputEvent.InteractionKeyMappingTriggered event) {
		if (!isEnabled(DisabledItems.class))
			return;

		Player player = Minecraft.getInstance().player;
		if (player == null)
			return;
		ItemStack stack = player.getItemInHand(event.getHand());
		if (stack.is(DISABLED)) {
			event.setCanceled(true);
			event.setSwingHand(false);
			Minecraft.getInstance().player.displayClientMessage(Component.translatable(DISABLED_ITEM_LANG), true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onTooltip(ItemTooltipEvent event) {
		if (!isEnabled(DisabledItems.class)
				|| !addTooltip)
			return;

		ItemStack stack = event.getItemStack();
		if (stack.is(NO_DAMAGE))
			event.getToolTip().add(Component.translatable(NO_DAMAGE_ITEM_LANG).withStyle(ChatFormatting.DARK_GRAY));
		if (stack.is(NO_MINING))
			event.getToolTip().add(Component.translatable(NO_MINING_ITEM_LANG).withStyle(ChatFormatting.DARK_GRAY));
		if (stack.is(NO_EQUIP))
			event.getToolTip().add(Component.translatable(NO_EQUIP_ITEM_LANG).withStyle(ChatFormatting.DARK_GRAY));
		if (stack.is(DISABLED))
			event.getToolTip().add(Component.translatable(DISABLED_ITEM_LANG).withStyle(ChatFormatting.DARK_GRAY));
	}
}
