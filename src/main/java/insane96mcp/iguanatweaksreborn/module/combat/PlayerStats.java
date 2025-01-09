package insane96mcp.iguanatweaksreborn.module.combat;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.SerializableAttributeModifier;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Label(name = "Player Stats", description = "Apply attribute modifiers to players. Attributes can be added in the json config of this feature's folder. Changing attribute modifiers requires player rejoin")
@LoadFeature(module = Modules.Ids.COMBAT)
public class PlayerStats extends JsonFeature {
	public static final UUID MOVEMENT_SPEED_REDUCTION_UUID = UUID.fromString("a88ac0d1-e2b3-4cf1-bb0e-9577486c874a");
	public static final UUID BLOCK_REACH_REDUCTION_UUID = UUID.fromString("bae34f6a-c58e-4622-b2ab-f1b89b73b781");
	@Config
	@Label(name = "No damage when spamming", description = "In vanilla, if you attack as soon as you just attacked you already deal 20% of the full damage. This changes that to 0%.")
	public static Boolean noDamageWhenSpamming = true;

	public static final ArrayList<SerializableAttributeModifier> ATTRIBUTE_MODIFIERS_DEFAULT = new ArrayList<>(List.of(
			new SerializableAttributeModifier(MOVEMENT_SPEED_REDUCTION_UUID, "Player Stats' Movement Speed modifier", List.of(), () -> Attributes.MOVEMENT_SPEED, -0.05d, AttributeModifier.Operation.MULTIPLY_BASE),
			new SerializableAttributeModifier(BLOCK_REACH_REDUCTION_UUID, "Player Stats' Block reach modifier", List.of(), ForgeMod.BLOCK_REACH, -1d, AttributeModifier.Operation.ADDITION)
	));
	public static final ArrayList<SerializableAttributeModifier> attributeModifiers = new ArrayList<>();

	public PlayerStats(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		JSON_CONFIGS.add(new JsonConfig<>("players_attribute_modifiers.json", attributeModifiers, ATTRIBUTE_MODIFIERS_DEFAULT, SerializableAttributeModifier.LIST_TYPE));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSurvivalOverhaul.CONFIG_FOLDER;
	}

	public static boolean noDamageWhenSpamming() {
		return isEnabled(PlayerStats.class) && noDamageWhenSpamming;
	}

	@SubscribeEvent
	public void onPlayerJoinLevel(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof net.minecraft.world.entity.player.Player player))
			return;

		for (SerializableAttributeModifier modifier : attributeModifiers)
			MCUtils.applyModifier(player, modifier.attribute().get(), modifier.uuid(), modifier.name(), modifier.amount(), modifier.operation(), false);
	}

}