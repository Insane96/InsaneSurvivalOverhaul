package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.event.HurtItemStackEvent;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@LoadFeature(module = ISOModules.ITEMS, description = "Items no longer break, will sit at one durability and can't be used. Items in the item tag `insanesurvivaloverhaul:not_unbreakable` are not affected and items in the item tag `insanesurvivaloverhaul:unbreakable` are always unbreakable.")
public class UnvanishableItems extends Feature {

	public static final String TOOL_DURABILITY_LANG = InsaneSO.lang("tool_durability");
	public static final String BROKEN_DURABILITY_LANG = InsaneSO.lang("broken_durability");
	public static final String BROKEN_ITEM_LANG = InsaneSO.lang("broken_item");
	public static final TagKey<Item> NOT_UNBREAKABLE = ISOItemTagsProvider.create("not_unbreakable");
	public static final TagKey<Item> UNBREAKABLE = ISOItemTagsProvider.create("unbreakable");
	public static final TagKey<Item> REMOVE_ORIGINAL_MODIFIERS_TAG = ISOItemTagsProvider.create("remove_original_modifiers");
	public static final TagKey<Item> NO_DURABILITY_TOOLTIP = ISOItemTagsProvider.create("no_durability_tooltip");

	@Config(description = "Items with durability get a durability tooltip. Items in the `insanesurvivaloverhaul:no_durability_tooltip` item tag will not get the tooltip.")
	public static Boolean durabilityTooltip = true;
	@Config(description = "If set to true only enchanted items will no longer break")
	public static Boolean enchantedItemsOnly = false;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
	}

	public static boolean isUnbreakable(ItemStack stack) {
		if (stack.is(NOT_UNBREAKABLE))
			return false;
		else if (stack.is(UNBREAKABLE))
			return true;
        return !enchantedItemsOnly || stack.isEnchanted();
    }

	public static boolean isBroken(ItemStack stack) {
		return stack.isDamageableItem() && (isUnbreakable(stack) && stack.getDamageValue() >= stack.getMaxDamage() - 1);
	}

	@SubscribeEvent
	public void processAttackDamage(LivingIncomingDamageEvent event) {
		if (!this.isEnabled()
				|| !(event.getSource().getDirectEntity() instanceof Player player))
			return;

		ItemStack stack = player.getMainHandItem();

		if (stack.getMaxDamage() == 0)
			return;
		if (isBroken(stack)) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable(BROKEN_ITEM_LANG), true);
        }
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event) {
		if (!this.isEnabled()
				|| event.getEntity().level().isClientSide
				|| event.getEntity().tickCount % 20 != event.getEntity().getId() % 20)
			return;

		for (ItemStack stack : event.getEntity().getArmorSlots()) {
			if (stack.isEmpty() || !isBroken(stack))
				continue;
			event.getEntity().level().playSound(null, event.getEntity(), SoundEvents.ALLAY_HURT, SoundSource.PLAYERS, 0.7f, 2f);
			EquipmentSlot equipmentSlot = event.getEntity().getEquipmentSlotForItem(stack);
			if (stack.getItem() instanceof Equipable) {
				event.getEntity().setItemSlot(equipmentSlot, ItemStack.EMPTY);
				if (!event.getEntity().addItem(stack))
					event.getEntity().drop(stack, true);
			}
		}
	}

	@SubscribeEvent
	public void processEfficiencyMultipliers(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled())
			return;

		Player player = event.getEntity();
		ItemStack stack = player.getMainHandItem();
		if (stack.getMaxDamage() == 0)
			return;
		if (isBroken(stack)){
			event.setCanceled(true);
			event.getEntity().displayClientMessage(Component.translatable(BROKEN_ITEM_LANG), true);
		}
	}

	/*@SubscribeEvent
	public void onEmptyRightClick(PlayerInteractEvent.RightClickEmpty event) {
		if (!this.isEnabled())
			return;

		ItemStack stack = event.getItemStack();
		if (stack.getMaxDamage() == 0)
			return;
		if (isBroken(stack)) {
			event.setCanceled(true);
			event.getEntity().displayClientMessage(Component.translatable(BROKEN_ITEM_LANG), true);
		}
	}*/

	@SubscribeEvent
	public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
		if (!this.isEnabled())
			return;

		ItemStack stack = event.getItemStack();
		if (stack.getMaxDamage() == 0)
			return;
		if (isBroken(stack)) {
			event.setCanceled(true);
			event.getEntity().displayClientMessage(Component.translatable(BROKEN_ITEM_LANG), true);
		}
	}

	@SubscribeEvent
	public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
		if (!this.isEnabled())
			return;

		ItemStack stack = event.getItemStack();
		if (stack.getMaxDamage() == 0)
			return;
		if (isBroken(stack)) {
			event.setCanceled(true);
			event.getEntity().displayClientMessage(Component.translatable(BROKEN_ITEM_LANG), true);
		}
	}

	@SubscribeEvent
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		if (!this.isEnabled())
			return;

		ItemStack stack = event.getItemStack();
		if (stack.getMaxDamage() == 0)
			return;
		if (isBroken(stack)) {
			event.setCanceled(true);
			event.getEntity().displayClientMessage(Component.translatable(BROKEN_ITEM_LANG), true);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void processItemDamaging(HurtItemStackEvent event) {
		if (!this.isEnabled()
				|| !(event.getLivingEntity() instanceof Player player))
			return;

		ItemStack stack = event.getStack();
		if (!isUnbreakable(stack))
			return;
		if (isBroken(stack)) {
			event.setAmount(0);
			return;
		}
		if (event.getAmount() >= stack.getMaxDamage() - stack.getDamageValue() - 1) {
			event.getStack().setDamageValue(event.getStack().getMaxDamage() - 1);
			event.setAmount(0);
			EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(stack);
			if (stack.getItem() instanceof Equipable) {
				player.setItemSlot(equipmentSlot, ItemStack.EMPTY);
				if (!player.addItem(stack))
					player.drop(stack, true);
			}
			player.onEquippedItemBroken(stack.getItem(), equipmentSlot);
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
                || event.getItemStack().is(NO_DURABILITY_TOOLTIP))
			return;

		ItemStack stack = event.getItemStack();

		if (stack.isDamageableItem()) {
			int durability = stack.getMaxDamage();
			if (isUnbreakable(stack))
				durability--;
			int durabilityLeft = durability - stack.getDamageValue();
			MutableComponent component = null;
            if (isBroken(stack)) {
				if (durabilityLeft < -stack.getDamageValue() * 0.05f)
					component = Component.translatable("insanesurvivaloverhaul.heavily_broken_durability").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);
				else
					component = Component.translatable(BROKEN_DURABILITY_LANG).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD);
			}
            else if (durabilityTooltip)
				component = Component.translatable(TOOL_DURABILITY_LANG, durabilityLeft, durability).withStyle(ChatFormatting.GRAY);
			if (component != null)
				event.getToolTip().add(event.getToolTip().size(), component);
		}
	}
}