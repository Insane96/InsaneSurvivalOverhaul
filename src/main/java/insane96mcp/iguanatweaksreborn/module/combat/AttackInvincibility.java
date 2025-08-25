package insane96mcp.iguanatweaksreborn.module.combat;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.Packs;
import insane96mcp.iguanatweaksreborn.network.message.InvulnerableTimeMessageSync;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.COMBAT, description = "Less invincibility frames and none with arrows.")
public class AttackInvincibility extends Feature {

	@Config(description = "If true less invincibility frames are applied to mobs only if using an item with attack speed modifier")
	public static Boolean attackSpeedBasedInvincibilityFrames = true;
	@Config(description = "If true, a data pack is enabled that makes Arrows and magic damage ignore invincibility frames.")
	public static Boolean arrowsMagicNoInvincFrames = true;

	public AttackInvincibility(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("attack_invincibility", "Insane's Survival Overhaul Attack Invincibility", () -> super.isEnabled() && !Packs.disableAllDataPacks && arrowsMagicNoInvincFrames);
	}

	@SubscribeEvent
	public void onAttack(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| !attackSpeedBasedInvincibilityFrames
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