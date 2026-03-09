package insane96mcp.insanesurvivaloverhaul.module.hungerhealth.exhaustion;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.event.PlayerExhaustionEvent;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.combat.bows.Bows;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LoadFeature(module = ISOModules.HUNGER_HEALTH, description = "Make the player consume more hunger with different actions. Please note that even if hunger is disabled, exhaustion will still be user by Tiredness.")
public class Exhaustion extends Feature {
	@Config(min = 0d, max = 128d, description = "When you break a block you'll get exhaustion equal to the block hardness multiplied by this value. Setting this to 0 will default to the vanilla exhaustion (0.005). (It's not affected by the Global Hardness Features)")
	public static Double blockBreakExhaustionMultiplier = 0d;
	@Config(min = 0d, max = 128d, description = "When breaking block you'll get exhaustion every tick during the breaking.")
	public static Double exhaustionOnBlockBreaking = 0.005d;
	@Config(min = 0d, max = 128d, description = "Every second the player will get this exhaustion.")
	public static Double passiveExhaustion = 0.005d;
	@Config(min = 0d, max = 128d, description = "Every tick of the player's rowing will get this exhaustion.")
	public static Double rowingExhaustion = 0.005d;
	@Config(min = 0d, max = 128d, description = "Every tick of the player's charging an arrow on a bow/crossbow will get this exhaustion.")
	public static Double bowChargeExhaustion = 0.005d;
	@Config(description = "When affected by the hunger effect, exhaustion will be doubled per level of the effect")
	public static Boolean effectiveHunger = true;

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
		if (!this.isEnabled()
				|| !effectiveHunger
				|| !event.getEntity().hasEffect(MobEffects.HUNGER))
			return;

		//noinspection ConstantConditions
		int amp = event.getEntity().getEffect(MobEffects.HUNGER).getAmplifier() + 1;
		event.setAmount(event.getAmount() * (amp * 1f + 1));
	}

	/* Sync exhaustion & saturation */
	private static final Map<UUID, Float> lastExhaustionLevels = new HashMap<>();
	private static final Map<UUID, Float> lastSaturationLevels = new HashMap<>();

	@SubscribeEvent
	public void onLivingTickEvent(EntityTickEvent event) {
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
