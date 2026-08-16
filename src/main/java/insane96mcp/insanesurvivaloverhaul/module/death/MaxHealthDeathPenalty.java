package insane96mcp.insanesurvivaloverhaul.module.death;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.DEATH,
		description = "Makes players lose max health on death and adds a new item to gain them back.\nControlled via the insanesurvivaloverhaul:death_health_lost game rule.")
public class MaxHealthDeathPenalty extends Feature {
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHHEALTHLOST = GameRules.register("insanesurvivaloverhaul:death_health_lost", GameRules.Category.PLAYER, GameRules.IntegerValue.create(2));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_MAXHEALTHFROMCRYSTAL = GameRules.register("insanesurvivaloverhaul:max_health_from_crystal", GameRules.Category.PLAYER, GameRules.IntegerValue.create(30));
	public static final GameRules.Key<GameRules.IntegerValue> RULE_DEATHMINHEALTH = GameRules.register("insanesurvivaloverhaul:death_min_health", GameRules.Category.PLAYER, GameRules.IntegerValue.create(6));

	public static final DeferredHolder<Item, CrystalHeartItem> CRYSTAL_HEART = ISORegistries.ITEMS.register("crystal_heart", () -> new CrystalHeartItem(new Item.Properties().rarity(Rarity.RARE)));

	public static ResourceLocation DEATH_PENALTY_ID;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		DEATH_PENALTY_ID = this.createDataKey("death_penalty");
		InsaneSO.addServerPack("max_health_death_penalty", "Insane's Survival Overhaul Max Health Death Penalty", () -> this.isEnabled() && !Packs.disableAllDataPacks);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPlayerDeath(LivingDeathEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof ServerPlayer player))
			return;

		GameRules gameRules = player.level().getGameRules();
		int healthLost = gameRules.getInt(RULE_DEATHHEALTHLOST);
		if (healthLost <= 0)
			return;
		int minHealth = gameRules.getInt(RULE_DEATHMINHEALTH);

		AttributeInstance instance = player.getAttribute(Attributes.MAX_HEALTH);
		if (instance == null)
			return;
		AttributeModifier modifier = instance.getModifier(DEATH_PENALTY_ID);
		int newHealthPenalty;
		if (modifier == null)
			newHealthPenalty = -healthLost;
		else
			newHealthPenalty = (int) (modifier.amount() - healthLost);
		if (newHealthPenalty < -20 + minHealth)
			newHealthPenalty = -20 + minHealth;
		ModNBTData.putPersisted(player, DEATH_PENALTY_ID, newHealthPenalty);
	}

	@SubscribeEvent
	public void onPlayerJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Player player)
				|| ModNBTData.getPersisted(player, DEATH_PENALTY_ID, Double.class) == 0)
			return;
		double healthPenalty = ModNBTData.getPersisted(player, DEATH_PENALTY_ID, Double.class);
		AttributeInstance instance = player.getAttribute(Attributes.MAX_HEALTH);
		if (instance == null)
			return;
		instance.addOrReplacePermanentModifier(new AttributeModifier(DEATH_PENALTY_ID, healthPenalty, AttributeModifier.Operation.ADD_VALUE));
		ModNBTData.removePersisted(player, DEATH_PENALTY_ID);
	}

	public static class CrystalHeartItem extends Item {

		public CrystalHeartItem(Properties properties) {
			super(properties);
		}

		@Override
		public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
			int maxHealth = level.getGameRules().getInt(RULE_MAXHEALTHFROMCRYSTAL);
			AttributeInstance instance = player.getAttribute(Attributes.MAX_HEALTH);
			if (instance == null)
				return super.use(level, player, usedHand);
			AttributeModifier modifier = instance.getModifier(DEATH_PENALTY_ID);
			if (modifier == null)
				return super.use(level, player, usedHand);
			int playerMaxHealth = (int) (20 + modifier.amount());
			if (playerMaxHealth >= maxHealth) {
				if (!level.isClientSide)
					player.displayClientMessage(Component.translatable("insanesurvivaloverhaul.crystal_heart.max_health"), true);
				return super.use(level, player, usedHand);
			}
			instance.addOrReplacePermanentModifier(new AttributeModifier(DEATH_PENALTY_ID, modifier.amount() + 2, AttributeModifier.Operation.ADD_VALUE));
			ItemStack stackInHand = player.getItemInHand(usedHand);
			stackInHand.shrink(1);
			player.playSound(SoundEvents.CONDUIT_ACTIVATE, 1f, 0.5f);
			player.swing(usedHand);
			if (!level.isClientSide)
				player.displayClientMessage(Component.translatable("insanesurvivaloverhaul.crystal_heart.use"), true);
			return InteractionResultHolder.consume(stackInHand);
		}
	}
}
