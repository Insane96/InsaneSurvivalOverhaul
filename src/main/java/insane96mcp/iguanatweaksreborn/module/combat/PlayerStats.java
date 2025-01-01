package insane96mcp.iguanatweaksreborn.module.combat;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@Label(name = "Player Stats", description = "Various changes from weapons damage to bows, arrows and effects")
@LoadFeature(module = Modules.Ids.COMBAT)
public class PlayerStats extends Feature {
	public static final UUID ATTACK_RANGE_REDUCTION_UUID = UUID.fromString("0dd017a7-274c-4101-85b4-78af20a24c54");
	public static final UUID MOVEMENT_SPEED_REDUCTION_UUID = UUID.fromString("a88ac0d1-e2b3-4cf1-bb0e-9577486c874a");
	public static final UUID BLOCK_REACH_REDUCTION_UUID = UUID.fromString("bae34f6a-c58e-4622-b2ab-f1b89b73b781");
	@Config(min = -4d, max = 4d)
	@Label(name = "Attack range modifier", description = "Adds this to players' attack range")
	public static Double attackRangeModifier = 0d;
	@Config
	@Label(name = "No damage when spamming", description = "In vanilla, if you attack as soon as you just attacked you already deal 20% of the full damage. This changes that to 0%.")
	public static Boolean noDamageWhenSpamming = true;
	@Config
	@Label(name = "Movement speed reduction", description = "Reduces movement speed for players by this percentage.")
	public static Double movementSpeedReduction = 0.05d;
	@Config(min = -4, max = 0)
	@Label(name = "Mining Range reduction", description = "Reduce the range at which players can interact with blocks")
	public static Double miningRangeReduction = -1d;

	public PlayerStats(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	public static boolean noDamageWhenSpamming() {
		return isEnabled(PlayerStats.class) && noDamageWhenSpamming;
	}

	@SubscribeEvent
	public void onPlayerJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof net.minecraft.world.entity.player.Player player))
			return;

		if (attackRangeModifier != 0f)
			MCUtils.applyModifier(player, ForgeMod.ENTITY_REACH.get(), ATTACK_RANGE_REDUCTION_UUID, "Entity Reach reduction", attackRangeModifier, AttributeModifier.Operation.ADDITION, false);
		if (movementSpeedReduction != 0d)
			MCUtils.applyModifier(player, Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED_REDUCTION_UUID, "Movement Speed reduction", -movementSpeedReduction, AttributeModifier.Operation.MULTIPLY_BASE, false);
		if (miningRangeReduction != 0)
			MCUtils.applyModifier(player, ForgeMod.BLOCK_REACH.get(), BLOCK_REACH_REDUCTION_UUID, "Block reach reduction", PlayerStats.miningRangeReduction, AttributeModifier.Operation.ADDITION, false);
	}

}