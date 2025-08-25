package insane96mcp.iguanatweaksreborn.module.mobs.villager.villagers;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.mixin.VillagerDataAccessor;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.Packs;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.ModNBTData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@LoadFeature(module = Modules.Ids.MOBS, description = "Nerfs to villagers + change their trades via json config")
public class Villagers extends Feature {

	public static ResourceLocation CURE_DISCOUNT_REMOVED;
	public static ResourceLocation WAS_CONVERTED_ZOMBIE;
	private static final int[] VANILLA_LEVEL_UP_XP = new int[]{0, 10, 70, 150, 250};

	@Config(description = "If true, villagers will be given 1 trading experience as soon as they choose their job to lock the trades.")
	public static Boolean lockTrades = true;
	@Config(description = "If true, villagers will always be transformed into Zombies no matter the difficulty.")
	public static Boolean alwaysConvertZombie = true;
	@Config(min = 0d, max = 1d, description = "Define a max percentage discount that villagers can give.")
	public static Double maxDiscount = 0.5d;
	@Config(description = "If true, discount from curing zombie villagers will only be applied to zombie villagers that were naturally spawned.")
	public static Boolean preventCureDiscount = true;
	@Config(description = "When villagers restock, they update the 'demand'. Demand is a trade modifier that increases the price whenever a trade is done many times, BUT when a trade is not performed, at each restock the 'demand' goes negative, making possible for a trade to never increase it's price due to high negative demand. With this to true, negative demand will be capped at -max_uses of the trade (e.g. Carrot trade from a farmer will have it's minimum demand set to -16).")
	public static Boolean clampNegativeDemand = true;
	@Config(min = 0, max = 1, description = "Chance for a nitwit to spawn when two villagers breed")
	public static Double nitwitChance = 0.1d;
	@Config(description = "If true, the effect can no longer be applied to entities")
	public static Boolean removeBadOmen = false;
	@Config(description = "Multiplier to experience required by villagers to level up")
	public static Double experienceToLevelUpMultiplier = 1.3d;
	@Config(description = "Enables a data pack that changes villagers trades")
	public static Boolean tradesDataPack = true;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("villager_trades", "Insane's Survival Overhaul Villager Trades", () -> this.isEnabled() && !Packs.disableAllDataPacks && tradesDataPack);
		CURE_DISCOUNT_REMOVED = this.createDataKey("cure_discount_removed");
		WAS_CONVERTED_ZOMBIE = this.createDataKey("was_converted_zombie");
	}

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
		int[] newLevelUpXp = new int[5];
		for (int i = 0; i < newLevelUpXp.length; i++)
			newLevelUpXp[i] = (int) (VANILLA_LEVEL_UP_XP[i] * experienceToLevelUpMultiplier);
		VillagerDataAccessor.setNextLevelXpThresholds(newLevelUpXp);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onVillagerTradesHighPriority(VillagerTradesEvent event) {
		processVillagerTrades(event, false);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onVillagerTradesLowPriority(VillagerTradesEvent event) {
		processVillagerTrades(event, true);
	}

	private void processVillagerTrades(VillagerTradesEvent event, boolean isLowPriority) {
		if (!this.isEnabled())
			return;

		Int2ObjectMap<List<VillagerTrades.ItemListing>> itemListing = event.getTrades();
		itemListing.forEach((level, value) -> {
			VillagerTrade trades = VillagerTradesReloadListener.INSTANCE.getTradesOfLevel(event.getType(), level);
			if (trades == null
					|| trades.lowPriority != isLowPriority)
				return;
			if (trades.remove)
				value.clear();

			value.addAll(trades.trades);
		});
	}

	@SubscribeEvent
	public void onEffectAdded(MobEffectEvent.Applicable event) {
		if (!this.isEnabled()
				|| !removeBadOmen
				|| event.getEffectInstance().getEffect() != MobEffects.BAD_OMEN)
			return;

		event.setResult(Event.Result.DENY);
	}

	@SubscribeEvent
	public void onLivingTick(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Villager villager))
			return;

		tryRemovingCureDiscount(villager);
	}

	public void tryRemovingCureDiscount(Villager villager) {
		if (!preventCureDiscount
				|| ModNBTData.get(villager, CURE_DISCOUNT_REMOVED, Boolean.class)
				|| !ModNBTData.get(villager, WAS_CONVERTED_ZOMBIE, Boolean.class))
			return;

		Map<UUID, Object2IntMap<GossipType>> gossips = villager.getGossips().getGossipEntries();
		gossips.forEach(((uuid, gossipTypeObject2IntMap) -> {
			villager.getGossips().remove(uuid, GossipType.MAJOR_POSITIVE);
			villager.getGossips().remove(uuid, GossipType.MINOR_POSITIVE);
		}));
		ModNBTData.put(villager, CURE_DISCOUNT_REMOVED, true);
	}

	public static int clampSpecialPrice(int specialPriceDiff, final ItemStack baseCostA) {
		if (!isEnabled(Villagers.class)
				|| maxDiscount == 1d)
			return specialPriceDiff;

		if (specialPriceDiff < 0 && Mth.abs(specialPriceDiff) > baseCostA.getCount() * maxDiscount)
			return Mth.clamp(specialPriceDiff, (int) (baseCostA.getCount() * -maxDiscount), 0);

		return specialPriceDiff;
	}

	public static int clampDemand(int demand, int maxUses) {
		if (!isEnabled(Villagers.class)
				|| !clampNegativeDemand)
			return demand;

		return Math.max(demand, -maxUses);
	}

	public static void lockTrades(Villager villager) {
		if (!isEnabled(Villagers.class)
				|| !lockTrades)
			return;

		if (villager.getVillagerXp() == 0)
			villager.setVillagerXp(1);
	}

	public static boolean shouldConvertVillagerToZombie() {
		return isEnabled(Villagers.class) && alwaysConvertZombie;
	}

	public static boolean shouldSpawnAsNitwit(Villager villager) {
		return Feature.isEnabled(Villagers.class) && villager.getRandom().nextDouble() < nitwitChance;
	}
}