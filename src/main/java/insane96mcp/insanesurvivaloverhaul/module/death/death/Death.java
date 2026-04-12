package insane96mcp.insanesurvivaloverhaul.module.death.death;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.module.base.TagsFeature;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.insanelib.world.scheduled.ScheduledTasks;
import insane96mcp.insanelib.world.scheduled.ScheduledTickTask;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.items.UnvanishableItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

import java.util.List;

@LoadFeature(module = ISOModules.DEATH,
		name = "Death",
		description = "Makes you lose a percentage of items and durability on death. Controlled via game rules. Also adds a bounty system for the mob that killed the player. Items percentage lost can be configured with the insanesurvivaloverhaul:death_lose_items_percentage, insanesurvivaloverhaul:death_durability_penalty and insanesurvivaloverhaul:death_destroy_items game rules. Please note that this feature controls the vanilla keep_inventory game rule.")
public class Death extends Feature {
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHLOSEITEMSPERCENTAGE = GameRules.register("insanesurvivaloverhaul:death_lose_items_percentage", GameRules.Category.PLAYER, GameRules.IntegerValue.create(30));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHDURABILITYPENALTY = GameRules.register("insanesurvivaloverhaul:death_durability_penalty", GameRules.Category.PLAYER, GameRules.IntegerValue.create(15));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_DEATHDESTROYITEMS = GameRules.register("insanesurvivaloverhaul:death_destroy_items", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

	//TODO Allow multiple players killed
	public static ResourceLocation KILLED_PLAYER;
	public static final String PLAYER_KILLER_LANG = InsaneSO.MOD_ID + ".player_killer";
	public static final TagKey<EntityType<?>> KILLER_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(InsaneSO.MOD_ID, "killer_blacklist"));

	@Config(name = "Players' killer bounty", description = "If true, the player's killer will not despawn and when killed will drop 4x more items and experience.")
	public static Boolean vindicationVsKiller = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		KILLED_PLAYER = this.createDataKey("killed_player");
	}

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

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof ServerPlayer player))
			return;

		vindication(player, event.getSource());
	}

	public static void vindication(ServerPlayer player, DamageSource source) {
        if (!vindicationVsKiller
                || !(source.getEntity() instanceof Mob killer)
				|| ModNBTData.contains(killer, KILLED_PLAYER)
				|| killer.isRemoved()
				|| killer.isDeadOrDying()
				|| killer.getType().is(KILLER_BLACKLIST))
            return;

        ScheduledTasks.schedule(new ScheduledTickTask(1) {
            @Override
            public void run() {
                killer.setPersistenceRequired();
                double experienceMultiplier = 5d;
				if (ModNBTData.contains(killer, TagsFeature.EXPERIENCE_MULTIPLIER))
					experienceMultiplier *= ModNBTData.get(killer, TagsFeature.EXPERIENCE_MULTIPLIER, Double.class);
				TagsFeature.setExperienceMultiplier(experienceMultiplier, killer);
				ModNBTData.put(killer, KILLED_PLAYER, player.getUUID());
                killer.setCustomName(Component.translatable(PLAYER_KILLER_LANG, player.getGameProfile().getName(), killer.getName()).withStyle(ChatFormatting.GRAY));
            }
        });
    }

	@SubscribeEvent
	public void onPlayerKillerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Mob mob)
				|| !mob.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)
				|| !ModNBTData.contains(mob, KILLED_PLAYER)
				|| mob.level().isClientSide)
			return;
		Component deathMessage = event.getEntity().getCombatTracker().getDeathMessage();

		//noinspection DataFlowIssue
		mob.level().getServer().getPlayerList().broadcastSystemMessage(deathMessage, false);
	}
}