package insane96mcp.insanesurvivaloverhaul.module.death;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@LoadFeature(module = ISOModules.DEATH,
		description = "Makes players lose max health on death and adds a new item to gain them back.\nControlled via the insanesurvivaloverhaul:death_health_lost game rule.")
public class MaxHealthDeathPenalty extends Feature {
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

		GameRules gameRules = player.level().getGameRules();
		int healthLost = gameRules.getInt(RULE_DEATHHEALTHLOST);
		if (healthLost <= 0)
			return;

		AttributeInstance instance = player.getAttribute(Attributes.MAX_HEALTH);
		if (instance == null)
			return;
		AttributeModifier modifier = instance.getModifier(DEATH_PENALTY_ID);
		int newHealthPenalty;
		if (modifier == null)
			newHealthPenalty = -healthLost;
		else
			newHealthPenalty = (int) (modifier.amount() - healthLost);
		ModNBTData.putPersisted(player, DEATH_PENALTY_ID, newHealthPenalty);
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
}
