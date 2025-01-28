package insane96mcp.iguanatweaksreborn.module.mobs;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.UUID;

@Label(name = "Villagers Nerfs", description = "Small changes to villagers to make them less OP")
@LoadFeature(module = Modules.Ids.MOBS)
public class VillagersNerfs extends Feature {

	private static final String CURE_DISCOUNT_REMOVED = InsaneSurvivalOverhaul.RESOURCE_PREFIX + "cure_discount_removed";

	@Config
	@Label(name = "Lock Trades", description = "If true, villagers will be given 1 trading experience as soon as they choose their job to lock the trades.")
	public static Boolean lockTrades = true;
	@Config
	@Label(name = "Always Convert Zombie", description = "If true, villagers will always be transformed into Zombies, no matter the difficulty.")
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
	@Config
	@Label(name = "Remove Bad Omen", description = "If true, the effect can no longer be applied to entities")
	public static Boolean removeBadOmen = false;

	public VillagersNerfs(Module module, boolean enabledByDefault, boolean canBeDisabled) {
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
				|| !preventCureDiscount
				|| !(event.getEntity() instanceof Villager villager)
				|| villager.getPersistentData().getBoolean(CURE_DISCOUNT_REMOVED))
			return;

		Map<UUID, Object2IntMap<GossipType>> gossips = villager.getGossips().getGossipEntries();
		gossips.forEach(((uuid, gossipTypeObject2IntMap) -> {
			villager.getGossips().remove(uuid, GossipType.MAJOR_POSITIVE);
			villager.getGossips().remove(uuid, GossipType.MINOR_POSITIVE);
		}));
		villager.getPersistentData().putBoolean(CURE_DISCOUNT_REMOVED, true);
	}

	public static int clampSpecialPrice(int specialPriceDiff, final ItemStack baseCostA) {
		if (!isEnabled(VillagersNerfs.class)
				|| maxDiscount == 1d)
			return specialPriceDiff;

		if (specialPriceDiff < 0 && Mth.abs(specialPriceDiff) > baseCostA.getCount() * maxDiscount)
			return Mth.clamp(specialPriceDiff, (int) (baseCostA.getCount() * -maxDiscount), 0);

		return specialPriceDiff;
	}

	public static int clampDemand(int demand, int maxUses) {
		if (!isEnabled(VillagersNerfs.class)
				|| !clampNegativeDemand)
			return demand;

		return Math.max(demand, -maxUses);
	}

	public static void lockTrades(Villager villager) {
		if (!isEnabled(VillagersNerfs.class)
				|| !lockTrades)
			return;

		if (villager.getVillagerData().getProfession() != VillagerProfession.NONE && villager.getVillagerXp() == 0)
			villager.setVillagerXp(1);
	}

	public static void onZombieKillEntity(Zombie zombie, ServerLevel level, LivingEntity killedEntity) {
		if (!isEnabled(VillagersNerfs.class)
				|| !alwaysConvertZombie
				//If removed should mean that the Zombie Villager has already been converted
				|| killedEntity.isRemoved())
			return;

		if (killedEntity instanceof Villager villager && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(killedEntity, EntityType.ZOMBIE_VILLAGER, (timer) -> {})) {
			ZombieVillager zombievillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
			if (zombievillager == null)
				return;
			zombievillager.finalizeSpawn(level, level.getCurrentDifficultyAt(zombievillager.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true), null);
			zombievillager.setVillagerData(villager.getVillagerData());
			zombievillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
			zombievillager.setTradeOffers(villager.getOffers().createTag());
			zombievillager.setVillagerXp(villager.getVillagerXp());
			net.minecraftforge.event.ForgeEventFactory.onLivingConvert(killedEntity, zombievillager);
			if (!zombie.isSilent()) {
				level.levelEvent(null, 1026, zombie.blockPosition(), 0);
			}
		}
	}
}