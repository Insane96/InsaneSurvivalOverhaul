package insane96mcp.insanesurvivaloverhaul.module.items;

import com.mojang.blaze3d.platform.InputConstants;
import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.ITEMS, canBeDisabled = false)
public class ShulkerBoxesClient extends Feature {
	public static final Lazy<KeyMapping> TOGGLE_AUTO_PICKUP = Lazy.of(() -> new KeyMapping(
			"key.insanesurvivaloverhaul.toggle_shulker_auto_pickup",
			KeyConflictContext.GUI,
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_S,
			"key.categories.insanesurvivaloverhaul"
	));

	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.register(TOGGLE_AUTO_PICKUP.get());
	}

	@SubscribeEvent
	public void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
		if (!Feature.isEnabled(ShulkerBoxes.class)
				|| !ShulkerBoxes.autoPickupIntoShulkerBoxes
				|| !(event.getScreen() instanceof AbstractContainerScreen<?> screen)
				|| !TOGGLE_AUTO_PICKUP.get().isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode())))
			return;
		//Don't steal the key while typing in a text field (e.g. creative search, anvil rename)
		if (screen.children().stream().anyMatch(c -> c instanceof EditBox editBox && editBox.canConsumeInput()))
			return;
		Slot slot = screen.getSlotUnderMouse();
		if (slot == null
				|| slot.container != Minecraft.getInstance().player.getInventory()
				|| !slot.getItem().is(Tags.Items.SHULKER_BOXES))
			return;

		ServerboundToggleShulkerAutoPickupPacket.send(screen.getMenu().containerId, slot.index);
		event.setCanceled(true);
	}

	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent event) {
		if (!isEnabled(ShulkerBoxes.class)
				|| !ShulkerBoxes.autoPickupIntoShulkerBoxes
				|| !event.getItemStack().is(Tags.Items.SHULKER_BOXES))
			return;

		if (ShulkerBoxes.hasAutoPickup(event.getItemStack()))
			event.getToolTip().add(Component.translatable("insanesurvivaloverhaul.shulker_auto_pickup.enabled").withStyle(ChatFormatting.GREEN));
		else
			event.getToolTip().add(Component.translatable("insanesurvivaloverhaul.shulker_auto_pickup.disabled").withStyle(ChatFormatting.GRAY));
		event.getToolTip().add(Component.translatable("insanesurvivaloverhaul.shulker_auto_pickup.toggle", TOGGLE_AUTO_PICKUP.get().getTranslatedKeyMessage()).withStyle(ChatFormatting.DARK_GRAY));
	}
}
