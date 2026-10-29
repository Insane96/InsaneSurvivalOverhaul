package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

import java.util.List;

@LoadFeature(module = ISOModules.ITEMS, description = "Changes to Shulker Boxes.")
public class ShulkerBoxes extends Feature {

	@Config(description = "When picking up an item, if a Shulker Box in the inventory already contains that item, the item is put in the Shulker Box instead of the inventory. Auto pickup must be enabled on each Shulker Box by pressing the toggle key (S by default) while hovering it in the inventory.")
	public static Boolean autoPickupIntoShulkerBoxes = true;

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemPickup(ItemEntityPickupEvent.Pre event) {
		if (!this.isEnabled()
				|| !autoPickupIntoShulkerBoxes
				|| event.canPickup().isFalse())
			return;

		ItemEntity itemEntity = event.getItemEntity();
		Player player = event.getPlayer();
		//Respect vanilla pickup rules unless another handler forced the pickup
		if (!event.canPickup().isTrue()
				&& (itemEntity.hasPickUpDelay() || (itemEntity.getTarget() != null && !itemEntity.getTarget().equals(player.getUUID()))))
			return;

		ItemStack pickedStack = itemEntity.getItem();
		if (pickedStack.isEmpty() || !pickedStack.canFitInsideContainerItems())
			return;

		Item pickedItem = pickedStack.getItem();
		int originalCount = pickedStack.getCount();
		Inventory inventory = player.getInventory();
		for (List<ItemStack> compartment : List.of(inventory.items, inventory.offhand)) {
			for (ItemStack shulkerBox : compartment) {
				if (pickedStack.isEmpty())
					break;
				if (shulkerBox.is(Tags.Items.SHULKER_BOXES))
					tryInsertIntoShulkerBox(shulkerBox, pickedStack);
			}
		}

		int moved = originalCount - pickedStack.getCount();
		if (moved <= 0)
			return;

		//Vanilla won't play the pickup animation nor award the stat for the part that went into the Shulker Boxes
		player.take(itemEntity, moved);
		player.awardStat(Stats.ITEM_PICKED_UP.get(pickedItem), moved);
		if (pickedStack.isEmpty())
			itemEntity.discard();
	}

	/**
	 * Moves as much as possible of stack into the shulker box, only if the shulker box already contains the same item.
	 * Existing stacks are filled first, then empty slots. stack is shrunk by the amount moved.
	 */
	private static void tryInsertIntoShulkerBox(ItemStack shulkerBox, ItemStack stack) {
		ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
		if (!hasAutoPickup(shulkerBox)
				|| contents == null
				|| contents.nonEmptyStream().noneMatch(s -> ItemStack.isSameItemSameComponents(s, stack)))
			return;

		NonNullList<ItemStack> items = NonNullList.withSize(ShulkerBoxBlockEntity.CONTAINER_SIZE, ItemStack.EMPTY);
		contents.copyInto(items);
		int maxStackSize = stack.getMaxStackSize();
		for (int i = 0; i < items.size() && !stack.isEmpty(); i++) {
			ItemStack slotStack = items.get(i);
			if (slotStack.isEmpty() || !ItemStack.isSameItemSameComponents(slotStack, stack))
				continue;
			int toMove = Math.min(stack.getCount(), maxStackSize - slotStack.getCount());
			if (toMove <= 0)
				continue;
			slotStack.grow(toMove);
			stack.shrink(toMove);
		}
		for (int i = 0; i < items.size() && !stack.isEmpty(); i++) {
			if (!items.get(i).isEmpty())
				continue;
			int toMove = Math.min(stack.getCount(), maxStackSize);
			items.set(i, stack.copyWithCount(toMove));
			stack.shrink(toMove);
		}
		shulkerBox.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
	}

	public static boolean hasAutoPickup(ItemStack stack) {
		return stack.getOrDefault(ISORegistries.SHULKER_AUTO_PICKUP, false);
	}

	/**
	 * Copies auto pickup (and its glint) from the placed Shulker Box block entity to the dropped Shulker Boxes.
	 * The block entity keeps the components it doesn't handle itself, but the vanilla loot table doesn't copy them back.
	 */
	public static void copyAutoPickupToDrops(BlockEntity blockEntity, List<ItemStack> drops) {
		if (!blockEntity.components().getOrDefault(ISORegistries.SHULKER_AUTO_PICKUP.get(), false))
			return;
		for (ItemStack drop : drops) {
			if (!drop.is(Tags.Items.SHULKER_BOXES))
				continue;
			drop.set(ISORegistries.SHULKER_AUTO_PICKUP, true);
			drop.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		}
	}

	/**
	 * Toggles auto pickup on the Shulker Box in the given slot of the player's open menu.
	 * Only Shulker Boxes in the player's own inventory can be toggled. Enabled Shulker Boxes also get the enchantment glint.
	 */
	public static void toggleAutoPickup(Player player, int containerId, int slotIndex) {
		if (!Feature.isEnabled(ShulkerBoxes.class)
				|| !autoPickupIntoShulkerBoxes)
			return;
		AbstractContainerMenu menu = player.containerMenu;
		if (menu.containerId != containerId
				|| slotIndex < 0
				|| slotIndex >= menu.slots.size())
			return;
		Slot slot = menu.getSlot(slotIndex);
		ItemStack stack = slot.getItem();
		if (slot.container != player.getInventory()
				|| !stack.is(Tags.Items.SHULKER_BOXES))
			return;

		if (hasAutoPickup(stack)) {
			stack.remove(ISORegistries.SHULKER_AUTO_PICKUP);
			stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
		}
		else {
			stack.set(ISORegistries.SHULKER_AUTO_PICKUP, true);
			stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
		}
		slot.setChanged();
		menu.broadcastChanges();
	}
}
