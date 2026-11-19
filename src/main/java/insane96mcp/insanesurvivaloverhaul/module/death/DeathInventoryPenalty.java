package insane96mcp.insanesurvivaloverhaul.module.death;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.insanesurvivaloverhaul.event.DeathPenaltyEvent;
import insane96mcp.insanesurvivaloverhaul.event.ISOEventHook;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.GameRulesIntegerValueAccessor;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.InventoryAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.items.unvanishableitems.UnvanishableItems;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@LoadFeature(module = ISOModules.DEATH,
		description = "Makes you lose a percentage of items and durability and xp on death. By default death_keep_equipment is enabled with a 15% death_durability_penalty, the other game rules are set to 0. Controlled via game rules.\nItems percentage lost can be configured with the insanesurvivaloverhaul:death_lose_items_percentage, insanesurvivaloverhaul:death_durability_penalty and insanesurvivaloverhaul:death_destroy_items game rules.\n insanesurvivaloverhaul:death_experience_lost for the percentage of experience dropped and insanesurvivaloverhaul:death_destroy_experience if the experience should be dropped or destroyed.\ninsanesurvivaloverhaul:death_keep_equipment makes worn armor, the offhand and the hotbar items with durability stay in the inventory (with the death_durability_penalty applied), while everything else drops normally (e.g. into a grave mod). When enabled, the other item and experience game rules are ignored.\nThe feature does nothing if the vanilla keep_inventory game rule is enabled.")
public class DeathInventoryPenalty extends Feature {
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHLOSEITEMSPERCENTAGE = GameRules.register("insanesurvivaloverhaul:death_lose_items_percentage", GameRules.Category.PLAYER, GameRulesIntegerValueAccessor.invokeCreate(0, 0, 100, (server, value) -> {}));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHDURABILITYPENALTY = GameRules.register("insanesurvivaloverhaul:death_durability_penalty", GameRules.Category.PLAYER, GameRulesIntegerValueAccessor.invokeCreate(15, 0, 100, (server, value) -> {}));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DEATHDESTROYITEMS = GameRules.register("insanesurvivaloverhaul:death_destroy_items", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHEXPERIENCEDROPPED = GameRules.register("insanesurvivaloverhaul:death_experience_lost", GameRules.Category.PLAYER, GameRulesIntegerValueAccessor.invokeCreate(0, 0, 100, (server, value) -> {}));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DEATHDESTROYEXPERIENCE = GameRules.register("insanesurvivaloverhaul:death_destroy_experience", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DEATHKEEPEQUIPMENT = GameRules.register("insanesurvivaloverhaul:death_keep_equipment", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

	/**
	 * Items (and experience) kept on death, stored on the dead player and given back on respawn.
	 * Used instead of the keep_inventory game rule, so a keep_inventory manually set always takes precedence.
	 * Serialized so it survives the player logging out on the death screen.
	 */
	public static final DeferredHolder<AttachmentType<?>, AttachmentType<KeptOnDeath>> KEPT_ON_DEATH =
			ISORegistries.ATTACHMENT_TYPES.register("kept_on_death", () -> AttachmentType.builder(() -> KeptOnDeath.EMPTY)
					.serialize(KeptOnDeath.CODEC, kept -> !kept.items().isEmpty() || kept.experience() >= 0)
					.build());

	/** End (exclusive) of the hotbar slots in {@link Inventory} indexes */
	private static final int HOTBAR_END = Inventory.getSelectionSize();
	/** Start (inclusive) of the armor slots in {@link Inventory} indexes, followed by the offhand slot */
	private static final int ARMOR_START = Inventory.INVENTORY_SIZE;
	/** End (exclusive) of the offhand slot in {@link Inventory} indexes */
	private static final int OFFHAND_END = Inventory.INVENTORY_SIZE + 4 + 1;

	public record KeptStack(int slot, ItemStack stack) {
		public static final Codec<KeptStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("slot").forGetter(KeptStack::slot),
				ItemStack.CODEC.fieldOf("stack").forGetter(KeptStack::stack)
		).apply(instance, KeptStack::new));
	}

	/**
	 * @param experience experience points to give back on respawn, or -1 if experience is not kept (vanilla drop)
	 */
	public record KeptOnDeath(List<KeptStack> items, int experience) {
		public static final KeptOnDeath EMPTY = new KeptOnDeath(List.of(), -1);
		public static final Codec<KeptOnDeath> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				KeptStack.CODEC.listOf().fieldOf("items").forGetter(KeptOnDeath::items),
				Codec.INT.fieldOf("experience").forGetter(KeptOnDeath::experience)
		).apply(instance, KeptOnDeath::new));
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof ServerPlayer player))
			return;
		GameRules gameRules = player.level().getGameRules();
		//A manually set keep_inventory takes precedence
		if (gameRules.getBoolean(GameRules.RULE_KEEPINVENTORY))
			return;
		if (gameRules.getBoolean(RULE_DEATHKEEPEQUIPMENT)) {
			keepEquipment(player, gameRules);
			return;
		}
		int lostItemsPercentage = gameRules.getInt(RULE_DEATHLOSEITEMSPERCENTAGE);
		int lostDurabilityPercentage = gameRules.getInt(RULE_DEATHDURABILITYPENALTY);
		int lostXpPercentage = gameRules.getInt(RULE_DEATHEXPERIENCEDROPPED);
		if (lostItemsPercentage == 0 && lostDurabilityPercentage == 0 && lostXpPercentage == 0)
			return;
		boolean destroyItems = gameRules.getBoolean(RULE_DEATHDESTROYITEMS);
		boolean destroyXp = gameRules.getBoolean(RULE_DEATHDESTROYEXPERIENCE);
		DeathPenaltyEvent deathEvent = ISOEventHook.onDeathPenalty(player, lostItemsPercentage, lostDurabilityPercentage, destroyItems, lostXpPercentage, destroyXp);
		if (deathEvent == null) {
			keepEverything(player);
			return;
		}
		lostItemsPercentage = deathEvent.getLostItemsPercentage();
		lostDurabilityPercentage = deathEvent.getLostDurabilityPercentage();
		destroyItems = deathEvent.isDestroyItems();
		Consumer<ItemStack> handler = deathEvent.getItemDropHandler();
		Predicate<ItemStack> filter = deathEvent.getItemFilter();
		for (List<ItemStack> compartment : ((InventoryAccessor) player.getInventory()).getCompartments())
			tryDropItems(player, compartment, player.getRandom(), lostItemsPercentage, lostDurabilityPercentage, destroyItems, handler, filter);
		for (List<ItemStack> extra : deathEvent.getAdditionalItemLists())
			tryDropItems(player, extra, player.getRandom(), lostItemsPercentage, lostDurabilityPercentage, destroyItems, handler, filter);
		tryDropExperience(player, deathEvent.getLostXpPercentage(), deathEvent.isDestroyXp());
		keepEverything(player);
	}

	/**
	 * Moves the whole inventory and the experience in {@link #KEPT_ON_DEATH}, as keep_inventory would do.
	 */
	private static void keepEverything(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		List<KeptStack> kept = new ArrayList<>();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (!inventory.getItem(slot).isEmpty())
				kept.add(new KeptStack(slot, inventory.removeItemNoUpdate(slot)));
		}
		player.setData(KEPT_ON_DEATH, new KeptOnDeath(kept, getTotalXp(player)));
	}

	/**
	 * Removes the worn armor, the offhand and the hotbar items with durability from the inventory, applying the durability penalty,
	 * and stores them in {@link #KEPT_ON_DEATH}. Everything else drops normally (and can be collected by grave mods).
	 * Items preventing equipment drop (Curse of Vanishing, including Rune Enchanting's rune) are left to vanilla to be destroyed.
	 */
	private static void keepEquipment(ServerPlayer player, GameRules gameRules) {
		int lostDurabilityPercentage = gameRules.getInt(RULE_DEATHDURABILITYPENALTY);
		DeathPenaltyEvent deathEvent = ISOEventHook.onDeathPenalty(player, 0, lostDurabilityPercentage, false, 0, false);
		if (deathEvent == null) {
			keepEverything(player);
			return;
		}
		lostDurabilityPercentage = deathEvent.getLostDurabilityPercentage();
		Predicate<ItemStack> filter = deathEvent.getItemFilter();
		Inventory inventory = player.getInventory();
		List<KeptStack> kept = new ArrayList<>();
		for (int slot = 0; slot < OFFHAND_END; slot++) {
			if (slot == HOTBAR_END)
				slot = ARMOR_START;
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty()
					|| !stack.isDamageableItem()
					|| !filter.test(stack)
					|| EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP))
				continue;
			inventory.removeItemNoUpdate(slot);
			if (applyDurabilityPenalty(player, stack, lostDurabilityPercentage))
				kept.add(new KeptStack(slot, stack));
		}
		if (!kept.isEmpty())
			player.setData(KEPT_ON_DEATH, new KeptOnDeath(kept, -1));
	}

	/**
	 * Prevents vanilla from dropping experience when it's kept.
	 */
	@SubscribeEvent
	public void onExperienceDrop(LivingExperienceDropEvent event) {
		if (event.getEntity() instanceof ServerPlayer player
				&& player.hasData(KEPT_ON_DEATH)
				&& player.getData(KEPT_ON_DEATH).experience() >= 0)
			event.setDroppedExperience(0);
	}

	/**
	 * Gives back the items and experience kept on death, items in the same slots if possible.
	 * Not gated by the feature being enabled, so nothing is lost if the feature gets disabled between death and respawn.
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onPlayerClone(PlayerEvent.Clone event) {
		if (!event.isWasDeath()
				|| !event.getOriginal().hasData(KEPT_ON_DEATH))
			return;
		KeptOnDeath kept = event.getOriginal().getData(KEPT_ON_DEATH);
		event.getOriginal().removeData(KEPT_ON_DEATH);
		Player player = event.getEntity();
		Inventory inventory = player.getInventory();
		for (KeptStack keptStack : kept.items()) {
			if (keptStack.slot() < inventory.getContainerSize() && inventory.getItem(keptStack.slot()).isEmpty())
				inventory.setItem(keptStack.slot(), keptStack.stack());
			else if (!inventory.add(keptStack.stack()))
				player.drop(keptStack.stack(), false);
		}
		if (kept.experience() > 0)
			player.giveExperiencePoints(kept.experience());
	}

	/**
	 * Damages the stack by a percentage of its max durability.
	 * Returns false if the item broke and must be destroyed.
	 */
	private static boolean applyDurabilityPenalty(ServerPlayer player, ItemStack stack, int lostDurabilityPercentage) {
		if (!stack.getItem().isDamageable(stack))
			return true;
		int newDamage = Math.min(stack.getDamageValue() + stack.getMaxDamage() * lostDurabilityPercentage / 100, stack.getMaxDamage());
		stack.setDamageValue(newDamage);
		if (newDamage >= stack.getMaxDamage()) {
			player.onEquippedItemBroken(stack.getItem(), player.getEquipmentSlotForItem(stack));
			return Feature.isEnabled(UnvanishableItems.class) && UnvanishableItems.isUnvanishable(stack);
		}
		return true;
	}

	private static void tryDropItems(ServerPlayer player, List<ItemStack> items, RandomSource random, int amount, int lostDurabilityPercentage, boolean destroyItems, @Nullable Consumer<ItemStack> itemDropHandler, Predicate<ItemStack> itemFilter) {
		for (int i = 0; i < items.size(); i++) {
			ItemStack stack = items.get(i);
            if (stack.isEmpty() || !itemFilter.test(stack))
                continue;

            if (!applyDurabilityPenalty(player, stack, lostDurabilityPercentage)) {
                items.set(i, ItemStack.EMPTY);
                continue;
            }
			if (amount >= 100) {
				items.set(i, ItemStack.EMPTY);
				dropOrDestroy(player, stack, destroyItems, itemDropHandler);
			}
			else {
				float amountToDrop = stack.getCount() * (amount / 100f);
				int roundedAmount = MathHelper.getAmountWithDecimalChance(random, amountToDrop);
				if (roundedAmount > 0) {
					dropOrDestroy(player, stack.copyWithCount(roundedAmount), destroyItems, itemDropHandler);
					stack.shrink(roundedAmount);
				}
			}
		}
	}

	private static void dropOrDestroy(ServerPlayer player, ItemStack stack, boolean destroyItems, @Nullable Consumer<ItemStack> itemDropHandler) {
		if (destroyItems)
			return;
		if (itemDropHandler != null)
			itemDropHandler.accept(stack);
		else
			player.drop(stack, true, false);
	}

	private static void tryDropExperience(ServerPlayer player, int lostXpPercentage, boolean destroyXp) {
		if (lostXpPercentage == 0)
			return;

		int totalXp = getTotalXp(player);
		if (totalXp <= 0)
			return;

		int xpToDrop = lostXpPercentage >= 100 ? totalXp : MathHelper.getAmountWithDecimalChance(player.getRandom(), totalXp * (lostXpPercentage / 100f));
		if (xpToDrop <= 0)
			return;

		if (!destroyXp)
			ExperienceOrb.award((ServerLevel) player.level(), player.position(), xpToDrop);

		int newTotal = totalXp - xpToDrop;
		player.experienceLevel = 0;
		player.experienceProgress = 0f;
		player.totalExperience = 0;
		player.giveExperiencePoints(newTotal);
	}

	/**
	 * Returns the total XP points the player has.
	 */
	private static int getTotalXp(Player player) {
		return getTotalXpForLevel(player.experienceLevel) + (int) (player.experienceProgress * player.getXpNeededForNextLevel());
	}

	/**
	 * Returns the total XP points required to reach the given level from 0.
	 * Mirrors vanilla's getXpNeededForNextLevel() so the sum stays correct if the per-level costs ever change.
	 */
	private static int getTotalXpForLevel(int level) {
		int total = 0;
		for (int i = 0; i < level; i++)
			total += i >= 30 ? 112 + (i - 30) * 9 : i >= 15 ? 37 + (i - 15) * 5 : 7 + i * 2;
		return total;
	}
}
