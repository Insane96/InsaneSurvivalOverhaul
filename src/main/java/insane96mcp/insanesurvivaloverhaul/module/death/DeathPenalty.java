package insane96mcp.insanesurvivaloverhaul.module.death;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.items.UnvanishableItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;

@LoadFeature(module = ISOModules.DEATH,
		description = "Makes you lose a percentage of items and durability on death. Controlled via game rules. Items percentage lost can be configured with the insanesurvivaloverhaul:death_lose_items_percentage, insanesurvivaloverhaul:death_durability_penalty and insanesurvivaloverhaul:death_destroy_items game rules. Please note that this feature controls the vanilla keep_inventory game rule.")
public class DeathPenalty extends Feature {
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHLOSEITEMSPERCENTAGE = GameRules.register("insanesurvivaloverhaul:death_lose_items_percentage", GameRules.Category.PLAYER, GameRules.IntegerValue.create(30));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHDURABILITYPENALTY = GameRules.register("insanesurvivaloverhaul:death_durability_penalty", GameRules.Category.PLAYER, GameRules.IntegerValue.create(15));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DEATHDESTROYITEMS = GameRules.register("insanesurvivaloverhaul:death_destroy_items", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onPlayerDeathEarly(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof ServerPlayer player))
			return;
		int lostItemsPercentage = player.level().getGameRules().getInt(RULE_DEATHLOSEITEMSPERCENTAGE);
		int lostDurabilityPercentage = player.level().getGameRules().getInt(RULE_DEATHDURABILITYPENALTY);
		if (lostItemsPercentage == 0 && lostDurabilityPercentage == 0) {
			player.level().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(false, player.server);
			return;
		}
		player.level().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, player.server);
		boolean destroyItems = player.level().getGameRules().getBoolean(RULE_DEATHDESTROYITEMS);
		//TODO Add event so other mods can use it for their slots and to change what to do with the items (e.g. put them in a grave)
		tryDropItems(player, player.getInventory(), player.getInventory().items, player.getRandom(), lostItemsPercentage, lostDurabilityPercentage, destroyItems);
		tryDropItems(player, player.getInventory(), player.getInventory().armor, player.getRandom(), lostItemsPercentage, lostDurabilityPercentage, destroyItems);
		tryDropItems(player, player.getInventory(), player.getInventory().offhand, player.getRandom(), lostItemsPercentage, lostDurabilityPercentage, destroyItems);
	}

	private static void tryDropItems(ServerPlayer player, Inventory inventory, List<ItemStack> items, RandomSource random, int amount, int lostDurabilityPercentage, boolean destroyItems) {
		items.forEach(stack -> {
            if (stack.isEmpty())
                return;

            if (stack.getItem().isDamageable(stack)) {
                int newDamage = Math.min(stack.getDamageValue() + stack.getMaxDamage() * lostDurabilityPercentage / 100, stack.getMaxDamage());
                stack.setDamageValue(newDamage);
                if (newDamage >= stack.getMaxDamage()) {
                    player.onEquippedItemBroken(stack.getItem(), player.getEquipmentSlotForItem(stack));
                    if (!Feature.isEnabled(UnvanishableItems.class) || !UnvanishableItems.isUnvanishable(stack)) {
                        inventory.removeItem(stack);
                    }
                }
            }
			if (amount >= 100) {
				dropOrDestroy(player, stack, destroyItems);
			}
			else {
				float amountToDrop = stack.getCount() * (amount / 100f);
				int roundedAmount = MathHelper.getAmountWithDecimalChance(random, amountToDrop);
				if (roundedAmount > 0) {
					dropOrDestroy(player, stack.copyWithCount(roundedAmount), destroyItems);
					stack.shrink(roundedAmount);
				}
			}
        });
	}

	private static void dropOrDestroy(ServerPlayer player, ItemStack stack, boolean destroyItems) {
		if (!destroyItems)
			player.drop(stack, true, false);
	}
}
