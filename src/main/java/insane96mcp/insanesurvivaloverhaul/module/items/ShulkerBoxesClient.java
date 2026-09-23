package insane96mcp.insanesurvivaloverhaul.module.items;

import com.mojang.blaze3d.platform.InputConstants;
import insane96mcp.insanelib.core.feature.Feature;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class ShulkerBoxesClient {
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

	public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
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
}
