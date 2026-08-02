package insane96mcp.insanesurvivaloverhaul.module.death;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.insanesurvivaloverhaul.event.DeathPenaltyEvent;
import insane96mcp.insanesurvivaloverhaul.event.ISOEventHook;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.GameRulesIntegerValueAccessor;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.InventoryAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.items.UnvanishableItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@LoadFeature(module = ISOModules.DEATH,
		description = "Makes you lose a percentage of items and durability and xp on death, or max health. By default no items or xp is lost. Controlled via game rules.\nItems percentage lost can be configured with the insanesurvivaloverhaul:death_lose_items_percentage, insanesurvivaloverhaul:death_durability_penalty and insanesurvivaloverhaul:death_destroy_items game rules.\n insanesurvivaloverhaul:death_experience_lost for the percentage of experience dropped and insanesurvivaloverhaul:death_destroy_experience if the experience should be dropped or destroyed.\ninsanesurvivaloverhaul:death_health_lost controls how much max health is lost on death\nPlease note that this feature controls the vanilla keep_inventory game rule.")
public class DeathPenalty extends Feature {
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHLOSEITEMSPERCENTAGE = GameRules.register("insanesurvivaloverhaul:death_lose_items_percentage", GameRules.Category.PLAYER, GameRulesIntegerValueAccessor.invokeCreate(0, 0, 100, (server, value) -> {}));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHDURABILITYPENALTY = GameRules.register("insanesurvivaloverhaul:death_durability_penalty", GameRules.Category.PLAYER, GameRulesIntegerValueAccessor.invokeCreate(0, 0, 100, (server, value) -> {}));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DEATHDESTROYITEMS = GameRules.register("insanesurvivaloverhaul:death_destroy_items", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHEXPERIENCEDROPPED = GameRules.register("insanesurvivaloverhaul:death_experience_lost", GameRules.Category.PLAYER, GameRulesIntegerValueAccessor.invokeCreate(0, 0, 100, (server, value) -> {}));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DEATHDESTROYEXPERIENCE = GameRules.register("insanesurvivaloverhaul:death_destroy_experience", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHHEALTHLOST = GameRules.register("insanesurvivaloverhaul:death_health_lost", GameRules.Category.PLAYER, GameRules.IntegerValue.create(2));

	public static ResourceLocation DEATH_PENALTY_ID;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		DEATH_PENALTY_ID = this.createDataKey("death_penalty");
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof ServerPlayer player))
			return;
		loseMaxHealth(player);
		GameRules gameRules = player.level().getGameRules();
		int lostItemsPercentage = gameRules.getInt(RULE_DEATHLOSEITEMSPERCENTAGE);
		int lostDurabilityPercentage = gameRules.getInt(RULE_DEATHDURABILITYPENALTY);
		int lostXpPercentage = gameRules.getInt(RULE_DEATHEXPERIENCEDROPPED);
		if (lostItemsPercentage == 0 && lostDurabilityPercentage == 0 && lostXpPercentage == 0) {
			gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(false, player.server);
			return;
		}
		gameRules.getRule(GameRules.RULE_KEEPINVENTORY).set(true, player.server);
		boolean destroyItems = gameRules.getBoolean(RULE_DEATHDESTROYITEMS);
		boolean destroyXp = gameRules.getBoolean(RULE_DEATHDESTROYEXPERIENCE);
		DeathPenaltyEvent deathEvent = ISOEventHook.onDeathPenalty(player, lostItemsPercentage, lostDurabilityPercentage, destroyItems, lostXpPercentage, destroyXp);
		if (deathEvent == null)
			return;
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
	}

	@SubscribeEvent
	public void onPlayerRespawn(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Player player))
			return;
		AttributeInstance instance = player.getAttribute(Attributes.MAX_HEALTH);
		if (instance == null)
			return;
		instance.addOrReplacePermanentModifier(new AttributeModifier(DEATH_PENALTY_ID, ModNBTData.getPersisted(player, DEATH_PENALTY_ID, Double.class), AttributeModifier.Operation.ADD_VALUE));
	}

	private static void tryDropItems(ServerPlayer player, List<ItemStack> items, RandomSource random, int amount, int lostDurabilityPercentage, boolean destroyItems, @Nullable Consumer<ItemStack> itemDropHandler, Predicate<ItemStack> itemFilter) {
		for (int i = 0; i < items.size(); i++) {
			ItemStack stack = items.get(i);
            if (stack.isEmpty() || !itemFilter.test(stack))
                continue;

            if (stack.getItem().isDamageable(stack)) {
                int newDamage = Math.min(stack.getDamageValue() + stack.getMaxDamage() * lostDurabilityPercentage / 100, stack.getMaxDamage());
                stack.setDamageValue(newDamage);
                if (newDamage >= stack.getMaxDamage()) {
                    player.onEquippedItemBroken(stack.getItem(), player.getEquipmentSlotForItem(stack));
                    if (!Feature.isEnabled(UnvanishableItems.class) || !UnvanishableItems.isUnvanishable(stack)) {
                        items.set(i, ItemStack.EMPTY);
                        continue;
                    }
                }
            }
			if (amount >= 100) {
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

		int totalXp = getTotalXpForLevel(player.experienceLevel) + (int) (player.experienceProgress * player.getXpNeededForNextLevel());
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
	 * Returns the total XP points required to reach the given level from 0.
	 * Mirrors vanilla's getXpNeededForNextLevel() so the sum stays correct if the per-level costs ever change.
	 */
	private static int getTotalXpForLevel(int level) {
		int total = 0;
		for (int i = 0; i < level; i++)
			total += i >= 30 ? 112 + (i - 30) * 9 : i >= 16 ? 37 + (i - 16) * 6 : 7 + i * 2;
		return total;
	}

	private static void loseMaxHealth(ServerPlayer player) {
		GameRules gameRules = player.level().getGameRules();
		int healthLost = gameRules.getInt(RULE_DEATHHEALTHLOST);
		if (healthLost <= 0)
			return;

		AttributeInstance instance = player.getAttribute(Attributes.MAX_HEALTH);
		if (instance == null)
			return;
		AttributeModifier modifier = instance.getModifier(DEATH_PENALTY_ID);
		int newHealthPenalty = 0;
		if (modifier == null)
			newHealthPenalty -= healthLost;
		else
			newHealthPenalty = (int) (modifier.amount() - healthLost);
		ModNBTData.putPersisted(player, DEATH_PENALTY_ID, newHealthPenalty);
	}
}
