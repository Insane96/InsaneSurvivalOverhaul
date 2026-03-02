package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.network.message.InvulnerableTimeSyncMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@LoadFeature(module = ISOModules.COMBAT, description = "Invincibility frames based off attack speed, so faster attacks will give less invincibility frames and viceversa.")
public class AttackSpeedBasedInvincibility extends Feature {

	@Config(description = "If true, invincibility frames will not be increased for slower attacks.")
	public static Boolean onlyFasterAttackSpeed = false;

	@SubscribeEvent
	public void onAttack(LivingIncomingDamageEvent event) {
		if (!this.isEnabled()
				|| !(event.getSource().getEntity() instanceof LivingEntity livingEntity)
				|| event.getEntity().invulnerableTime > 10
				|| livingEntity.getAttribute(Attributes.ATTACK_SPEED) == null
				|| (onlyFasterAttackSpeed && livingEntity.getAttribute(Attributes.ATTACK_SPEED).getValue() < 2f)
				|| livingEntity.getMainHandItem().getAttributeModifiers().modifiers().stream().noneMatch(e -> e.attribute().is(Attributes.ATTACK_SPEED) && e.slot().test(EquipmentSlot.MAINHAND)))
			return;

		int time = (int) ((1f / livingEntity.getAttribute(Attributes.ATTACK_SPEED).getValue()) * 20 * 0.9f);
		event.setInvulnerabilityTicks(time + 10);
		InvulnerableTimeSyncMessage.sync((ServerLevel) event.getEntity().level(), event.getEntity(), time);
	}
}