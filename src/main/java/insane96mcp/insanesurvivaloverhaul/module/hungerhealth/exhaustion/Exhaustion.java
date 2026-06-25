package insane96mcp.insanesurvivaloverhaul.module.hungerhealth.exhaustion;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.event.PlayerExhaustionEvent;
import insane96mcp.insanelib.util.ILRangedAttribute;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LoadFeature(module = ISOModules.HUNGER_HEALTH, description = "Make the player consume more hunger with different actions. Please note that even if hunger is disabled, exhaustion will still be user by Tiredness.")
public class Exhaustion extends Feature {

	public static final DeferredHolder<Attribute, Attribute> EXHAUSTION_MULTIPLIER = ISORegistries.ATTRIBUTES.register("exhaustion_multiplier", () -> new ILRangedAttribute(1d, 0d, Double.MAX_VALUE));

	@Config(min = 0d, description = "When you break a block you'll get exhaustion equal to the block hardness multiplied by this value. Setting this to 0 will default to the vanilla exhaustion (0.005). (It's not affected by the Global Hardness Features)")
	public static Double blockBreakExhaustionMultiplier = 0d;
	@Config(min = 0d, description = "When breaking block you'll get exhaustion every tick during the breaking. Vanilla is 0.005")
	public static Double exhaustionOnBlockBreaking = 0.005d;
	@Config(min = 0d, description = "Every second the player will get this exhaustion. Vanilla is 0")
	public static Double passiveExhaustion = 0.005d;
	@Config(min = 0d, description = "Every tick of the player's rowing will get this exhaustion. Vanilla is 0")
	public static Double rowingExhaustion = 0.005d;
	@Config(min = 0d, description = "Every tick of the player's charging an arrow on a bow/crossbow will get this exhaustion. Vanilla is 0")
	public static Double bowChargeExhaustion = 0.005d;
	@Config(min = 0d, description = "How much exhaustion is given to the player when sprinting per block traveled. Vanilla is 0.1")
	public static Double sprintExhaustion = 0.5d;
	@Config(min = 0d, description = "How much exhaustion is given to the player when jumps and it's not sprinting. Vanilla is 0.05")
	public static Double jumpExhaustion = 0.10d;
	@Config(min = 0d, description = "How much exhaustion is given to the player when jumps and it's sprinting. Vanilla is 0.2")
	public static Double jumpExhaustionSprint = 0.5d;
	@Config(description = "When affected by the hunger effect, exhaustion will be doubled per level of the effect. Requires Minecraft restart.")
	public static Boolean effectiveHunger = true;

	private static boolean effectiveHungerInitialized = false;

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
		if (effectiveHunger && !effectiveHungerInitialized) {
			MobEffects.HUNGER.value().addAttributeModifier(EXHAUSTION_MULTIPLIER, InsaneSO.id("effective_hunger"), 1, AttributeModifier.Operation.ADD_VALUE);
			effectiveHungerInitialized = true;
		}
	}

	public static void addAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (entityType != EntityType.PLAYER)
				continue;
			if (!event.has(entityType, EXHAUSTION_MULTIPLIER))
				event.add(entityType, EXHAUSTION_MULTIPLIER);
		}
	}

	@SubscribeEvent
	public void breakExhaustion(BlockEvent.BreakEvent event) {
		if (!this.isEnabled()
				|| blockBreakExhaustionMultiplier == 0d)
			return;
		ServerLevel world = (ServerLevel) event.getLevel();
		BlockState state = world.getBlockState(event.getPos());
		double hardness = state.getDestroySpeed(event.getLevel(), event.getPos());
		double exhaustion = (hardness * blockBreakExhaustionMultiplier) - 0.005f;
		exhaustion = Math.max(exhaustion, 0d);
		event.getPlayer().causeFoodExhaustion((float) exhaustion);
	}

	@SubscribeEvent
	public void onBreakingBlock(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled()
				|| exhaustionOnBlockBreaking == 0d)
			return;
		event.getEntity().causeFoodExhaustion(exhaustionOnBlockBreaking.floatValue());
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (!this.isEnabled()
				|| player.level().isClientSide)
			return;

		if (passiveExhaustion > 0d
				&& player.tickCount % 20 == 0)
			player.causeFoodExhaustion(passiveExhaustion.floatValue());

		if (rowingExhaustion > 0d && player.getVehicle() != null && player.zza != 0)
			player.causeFoodExhaustion(rowingExhaustion.floatValue());

		if (bowChargeExhaustion > 0d && (player.getUseItem().is(Items.BOW) || player.getUseItem().is(Items.CROSSBOW) || player.getUseItem().is(Bows.SHORTBOW.get())))
			player.causeFoodExhaustion(bowChargeExhaustion.floatValue());
	}

	@SubscribeEvent
	public void onExhaustion(PlayerExhaustionEvent event) {
		if (!this.isEnabled())
			return;

		event.setAmount((float) (event.getAmount() * event.getEntity().getAttributeValue(EXHAUSTION_MULTIPLIER)));
	}

	/* Sync exhaustion & saturation */
	private static final Map<UUID, Float> lastExhaustionLevels = new HashMap<>();
	private static final Map<UUID, Float> lastSaturationLevels = new HashMap<>();

	@SubscribeEvent
	public void onLivingTickEvent(EntityTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		Float lastSaturationLevel = lastSaturationLevels.get(player.getUUID());
		if (lastSaturationLevel == null || lastSaturationLevel != player.getFoodData().getSaturationLevel()) {
			ClientboundSaturationPacket.sync(player, player.getFoodData().getSaturationLevel());
			lastSaturationLevels.put(player.getUUID(), player.getFoodData().getSaturationLevel());
		}

		Float lastExhaustionLevel = lastExhaustionLevels.get(player.getUUID());
		float exhaustionLevel = player.getFoodData().getExhaustionLevel();
		if (lastExhaustionLevel == null || Math.abs(lastExhaustionLevel - exhaustionLevel) >= 0.01f) {
			ClientboundExhaustionPacket.sync(player, exhaustionLevel);
			lastExhaustionLevels.put(player.getUUID(), exhaustionLevel);
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer))
			return;
		lastExhaustionLevels.remove(event.getEntity().getUUID());
		lastSaturationLevels.remove(event.getEntity().getUUID());
	}
}
