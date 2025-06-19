package insane96mcp.iguanatweaksreborn.module.items;

import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.ITEMS, description = "Make items not able to mine / attack")
public class DisabledItems extends Feature {

	public static final TagKey<Item> NO_DAMAGE = ISOItemTagsProvider.create("no_damage");
	public static final String NO_DAMAGE_ITEM_LANG = "iguanatweaksreborn.no_damage_item";
	public static final TagKey<Item> NO_MINING = ISOItemTagsProvider.create("no_mining");
	public static final String NO_MINING_ITEM_LANG = "iguanatweaksreborn.no_mining_item";
	public static final TagKey<Item> NO_EQUIP = ISOItemTagsProvider.create("no_equip");
	public static final String NO_EQUIP_ITEM_LANG = "iguanatweaksreborn.no_equip_item";

	public static final TagKey<Item> DISABLED = ISOItemTagsProvider.create("disabled");
	public static final String DISABLED_ITEM_LANG = "iguanatweaksreborn.disabled_item";

	@Config(description = "If set to true items in the 'no_damage', 'no_efficiency', 'no_equip' and 'disabled' item tags will get a tooltip.")
	public static Boolean addTooltip = true;

	public DisabledItems(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void processAttacking(LivingHurtEvent event) {
		if (!this.isEnabled()
				|| !(event.getSource().getDirectEntity() instanceof Player player))
			return;

		ItemStack stack = player.getMainHandItem();
		if (stack.is(NO_DAMAGE) || stack.is(DISABLED)) {
			event.setCanceled(true);
			if (stack.is(NO_DAMAGE))
				player.displayClientMessage(Component.translatable(NO_DAMAGE_ITEM_LANG), true);
		}
	}

	@SubscribeEvent
	public void processMining(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled())
			return;

		Player player = event.getEntity();
		ItemStack stack = player.getMainHandItem();
		if (stack.is(NO_MINING) || stack.is(DISABLED)) {
			event.setCanceled(true);
			if (stack.is(NO_MINING))
				event.getEntity().displayClientMessage(Component.translatable(NO_MINING_ITEM_LANG), true);
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void processInput(InputEvent.InteractionKeyMappingTriggered event) {
		if (!this.isEnabled())
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

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (!this.isEnabled()
				|| event.player.level().isClientSide
				|| event.phase == TickEvent.Phase.START
				|| event.player.tickCount % 20 != event.player.getId() % 20)
			return;

		for (ItemStack stack : event.player.getArmorSlots()) {
			if (stack.isEmpty() || (!stack.is(NO_EQUIP) && !stack.is(DISABLED)))
				continue;
			event.player.level().playSound(null, event.player, SoundEvents.ALLAY_HURT, SoundSource.PLAYERS, 0.7f, 2f);
			EquipmentSlot equipmentSlot = Player.getEquipmentSlotForItem(stack);
			if (stack.getItem() instanceof Equipable) {
				event.player.setItemSlot(equipmentSlot, ItemStack.EMPTY);
				if (!event.player.addItem(stack))
					event.player.drop(stack, true);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
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