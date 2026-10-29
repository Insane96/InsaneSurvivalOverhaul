package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@LoadFeature(module = ISOModules.ITEMS, description = "Make items not able to mine / attack")
public class DisabledItems extends Feature {

	public static final TagKey<Item> NO_DAMAGE = ISOItemTagsProvider.create("no_damage");
	public static final String NO_DAMAGE_ITEM_LANG = InsaneSO.lang("no_damage_item");
	public static final TagKey<Item> NO_MINING = ISOItemTagsProvider.create("no_mining");
	public static final String NO_MINING_ITEM_LANG = InsaneSO.lang("no_mining_item");
	public static final TagKey<Item> NO_EQUIP = ISOItemTagsProvider.create("no_equip");
	public static final String NO_EQUIP_ITEM_LANG = InsaneSO.lang("no_equip_item");

	public static final TagKey<Item> DISABLED = ISOItemTagsProvider.create("disabled");
	public static final String DISABLED_ITEM_LANG = InsaneSO.lang("disabled_item");

	@SubscribeEvent
	public void processAttacking(LivingIncomingDamageEvent event) {
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
	public void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (!this.isEnabled()
				|| player.level().isClientSide
				|| (player.tickCount + player.getId()) % 20 != 0)
			return;

		for (ItemStack stack : player.getArmorSlots()) {
			if (stack.isEmpty() || (!stack.is(NO_EQUIP) && !stack.is(DISABLED)))
				continue;
			player.level().playSound(null, player, SoundEvents.ALLAY_HURT, SoundSource.PLAYERS, 0.7f, 2f);
			EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(stack);
			if (stack.getItem() instanceof Equipable) {
				player.setItemSlot(equipmentSlot, ItemStack.EMPTY);
				if (!player.addItem(stack))
					player.drop(stack, true);
			}
		}
	}
}