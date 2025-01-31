package insane96mcp.iguanatweaksreborn.module.mobs.villager.villagers;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Label(name = "Villagers", description = "Nerfs to villagers + change their trades via json config")
@LoadFeature(module = Modules.Ids.MOBS)
public class Villagers extends Feature {

	private static final String CURE_DISCOUNT_REMOVED = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "cure_discount_removed";

	@Config
	@Label(name = "Lock Trades", description = "If true, villagers will be given 1 trading experience as soon as they choose their job to lock the trades.")
	public static Boolean lockTrades = true;
	@Config
	@Label(name = "Always convert villager to zombie", description = "If true, villagers will always be transformed into Zombies no matter the difficulty.")
	public static Boolean alwaysConvertZombie = true;
	@Config(min = 0d, max = 1d)
	@Label(name = "Max Discount Percentage", description = "Define a max percentage discount that villagers can give.")
	public static Double maxDiscount = 0.5d;
	@Config
	@Label(name = "Prevent Cure Discount", description = "If true, villagers will no longer get the discount when cured from Zombies to prevent over discounting.")
	public static Boolean preventCureDiscount = true;
	@Config
	@Label(name = "Clamp Negative Demand", description = "When villagers restock, they update the 'demand'. Demand is a trade modifier that increases the price whenever a trade is done many times, BUT when a trade is not performed, at each restock the 'demand' goes negative, making possible for a trade to never increase it's price due to high negative demand. With this to true, negative demand will be capped at -max_uses of the trade (e.g. Carrot trade from a farmer will have it's minimum demand set to -16).")
	public static Boolean clampNegativeDemand = true;
	@Config(min = 0)
	@Label(name = "Heal chance", description = "1 in X chance each tick for villagers to regain 1 health. Set to 0 to disable")
	public static Integer healChance = 200;
	@Config
	@Label(name = "Remove Bad Omen", description = "If true, the effect can no longer be applied to entities")
	public static Boolean removeBadOmen = false;
	@Config
	@Label(name = "Trades Data Pack", description = "Enables a data pack that changes villagers trades")
	public static Boolean tradesDataPack = true;

	public Villagers(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "villager_trades", Component.literal("Insane's Survival Overhaul Villager Trades"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && tradesDataPack));
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onVillagerTrades(VillagerTradesEvent event) {
		if (!this.isEnabled())
			return;

		Int2ObjectMap<List<VillagerTrades.ItemListing>> itemListing = event.getTrades();
		itemListing.forEach(((level, value) -> {
			VillagerTrade trades = VillagerTradesReloadListener.INSTANCE.getTradesOfLevel(event.getType(), level);
			if (trades == null)
				return;
			if (trades.remove)
				value.clear();

            value.addAll(trades.trades);
		}));
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
		tryHeal(villager);
	}

	public void tryRemovingCureDiscount(Villager villager) {
		if (!preventCureDiscount
				|| villager.getPersistentData().getBoolean(CURE_DISCOUNT_REMOVED))
			return;

		Map<UUID, Object2IntMap<GossipType>> gossips = villager.getGossips().getGossipEntries();
		gossips.forEach(((uuid, gossipTypeObject2IntMap) -> {
			villager.getGossips().remove(uuid, GossipType.MAJOR_POSITIVE);
			villager.getGossips().remove(uuid, GossipType.MINOR_POSITIVE);
		}));
		villager.getPersistentData().putBoolean(CURE_DISCOUNT_REMOVED, true);
	}

	public void tryHeal(Villager villager) {
		if (healChance == 0
				|| villager.getRandom().nextInt(healChance) != 0)
			return;

		villager.heal(1f);
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

		if (villager.getVillagerData().getProfession() != VillagerProfession.NONE && villager.getVillagerXp() == 0)
			villager.setVillagerXp(1);
	}

	public static boolean shouldConvertVillagerToZombie() {
		return isEnabled(Villagers.class) && alwaysConvertZombie;
	}
}