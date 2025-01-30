package insane96mcp.iguanatweaksreborn.module.mobs.villager.villagers;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.iguanatweaksreborn.module.mobs.villager.SerializableTrade;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Label(name = "Villagers", description = "Nerfs to villagers + change their trades via json config")
@LoadFeature(module = Modules.Ids.MOBS)
public class Villagers extends Feature {

	private static final String CURE_DISCOUNT_REMOVED = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "cure_discount_removed";

	public static final Supplier<ArrayList<SerializableTrade>> VILLAGER_TRADES_DEFAULT = () -> new ArrayList<>(List.of(
			new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.WHEAT_SEEDS), 4),
			new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Crops.CARROT_SEEDS.get()), 4),
			new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Crops.ROOTED_POTATO.get()), 4),
			new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.BEETROOT_SEEDS), 4),
			new SerializableTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.BROWN_MUSHROOM), 4),
			new SerializableTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.RED_MUSHROOM), 4),
			new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.MELON), 2),
			new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.PUMPKIN), 2),
			new SerializableTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.VINE), 4),
			new SerializableTrade(new ItemStack(Items.EMERALD, 1), new ItemStack(Items.LILY_PAD, 2), 4),
			new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.POINTED_DRIPSTONE, 2), 3),
			new SerializableTrade(new ItemStack(Items.EMERALD, 2), new ItemStack(Items.SEA_PICKLE), 3),
			new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.CACTUS), 3),
			new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.KELP), 3),
			new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.ACACIA_SAPLING), 3),
			new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.BIRCH_SAPLING), 3),
			new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.CHERRY_SAPLING), 3),
			new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.SPRUCE_SAPLING), 3),
			new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.OAK_SAPLING), 3),
			new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.DARK_OAK_SAPLING), 3),
			new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.JUNGLE_SAPLING), 3),
			new SerializableTrade(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.NAUTILUS_SHELL), 5),
			new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.TROPICAL_FISH_BUCKET), 4),
			new SerializableTrade(new ItemStack(Items.EMERALD, 3), new ItemStack(Items.PUFFERFISH_BUCKET), 4)
	));
	public static final ArrayList<VillagerTrade> trades = new ArrayList<>();

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

	public Villagers(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
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