package insane96mcp.iguanatweaksreborn.module.hungerhealth;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.combat.bows.Bows;
import insane96mcp.iguanatweaksreborn.network.NetworkHandler;
import insane96mcp.iguanatweaksreborn.network.message.ExhaustionSync;
import insane96mcp.iguanatweaksreborn.network.message.SaturationSync;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.event.PlayerExhaustionEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LoadFeature(module = Modules.Ids.HUNGER_HEALTH, description = "Make the player consume more hunger with different actions. Please note that if hunger is disabled, exhaustion still applies to Tiredness.")
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
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (!this.isEnabled()
				|| event.phase != TickEvent.Phase.END
				|| event.player.level().isClientSide)
			return;

		if (passiveExhaustion > 0d
				&& event.player.tickCount % 20 == 0)
			event.player.causeFoodExhaustion(passiveExhaustion.floatValue());

		if (rowingExhaustion > 0d && event.player.getVehicle() != null && event.player.zza != 0)
			event.player.causeFoodExhaustion(rowingExhaustion.floatValue());

		if (bowChargeExhaustion > 0d && (event.player.getUseItem().is(Items.BOW) || event.player.getUseItem().is(Items.CROSSBOW) || event.player.getUseItem().is(Bows.SHORTBOW.get())))
			event.player.causeFoodExhaustion(bowChargeExhaustion.floatValue());
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

	/*
	 * Sync exhaustion & saturation
	 */
	private static final Map<UUID, Float> lastExhaustionLevels = new HashMap<>();
	private static final Map<UUID, Float> lastSaturationLevels = new HashMap<>();

	@SubscribeEvent
	public void onLivingTickEvent(LivingEvent.LivingTickEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;

		Float lastSaturationLevel = lastSaturationLevels.get(player.getUUID());
		if (lastSaturationLevel == null || lastSaturationLevel != player.getFoodData().getSaturationLevel()) {
			Object msg = new SaturationSync(player.getFoodData().getSaturationLevel());
			NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			lastSaturationLevels.put(player.getUUID(), player.getFoodData().getSaturationLevel());
		}

		Float lastExhaustionLevel = lastExhaustionLevels.get(player.getUUID());
		float exhaustionLevel = player.getFoodData().exhaustionLevel;
		if (lastExhaustionLevel == null || Math.abs(lastExhaustionLevel - exhaustionLevel) >= 0.01f) {
			Object msg = new ExhaustionSync(exhaustionLevel);
			NetworkHandler.CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
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
