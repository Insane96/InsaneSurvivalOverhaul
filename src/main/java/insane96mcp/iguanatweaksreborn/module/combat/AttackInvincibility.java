package insane96mcp.iguanatweaksreborn.module.combat;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.network.message.InvulnerableTimeMessageSync;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Attack invincibility", description = "Less invincibility frames and none with arrows.")
@LoadFeature(module = Modules.Ids.COMBAT)
public class AttackInvincibility extends Feature {

	@Config
	@Label(name = "Invincibility frames based off attack speed", description = "If true less invincibility frames are applied to mobs only if using an item with attack speed modifier")
	public static Boolean invincibilityFramesAttackSpeed = true;
	@Config
	@Label(name = "Arrows and magic ignore invincibility frames", description = "If true, a data pack is enabled that makes Arrows and magic damage ignore invincibility frames.")
	public static Boolean arrowsMagicNoInvincFrames = true;

	public AttackInvincibility(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		InsaneSurvivalOverhaul.addServerPack("attack_invincibility", "Insane's Survival Overhaul Attack Invincibility", () -> super.isEnabled() && !DataPacks.disableAllDataPacks && arrowsMagicNoInvincFrames);
	}

	@SubscribeEvent
	public void onAttack(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| !invincibilityFramesAttackSpeed
				|| !(event.getSource().getEntity() instanceof ServerPlayer serverPlayer)
				|| serverPlayer.getAttribute(Attributes.ATTACK_SPEED).getValue() < 2f
				|| !serverPlayer.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_SPEED))
			return;

		int time = (int) ((1f / serverPlayer.getAttribute(Attributes.ATTACK_SPEED).getValue()) * 20);
		event.getEntity().invulnerableTime = time;
		event.getEntity().hurtTime = time;
		InvulnerableTimeMessageSync.sync((ServerLevel) event.getEntity().level(), event.getEntity(), time);
	}
}