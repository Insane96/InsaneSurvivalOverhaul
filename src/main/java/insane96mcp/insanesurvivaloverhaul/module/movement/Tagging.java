package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@LoadFeature(module = ISOModules.MOVEMENT, description = "Players's slowed down for a brief moment when hit.")
public class Tagging extends Feature {

	@Config(min = 0, max = 10, description = "Which level of Slowness is applied to the player (level 0 is Slowness I).")
	public static Integer slownessLevel = 0;
	@Config(min = 0, max = 100, description = "Slowness is applied for damage_taken * this_value ticks.")
	public static Integer durationMultiplier = 7;

	@SubscribeEvent
	public void onDamageTaken(LivingDamageEvent.Pre event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Player playerEntity))
			return;
		if (event.getSource().getEntity() instanceof LivingEntity) {
			int duration = (int) (event.getNewDamage() * durationMultiplier);
			if (duration > 0)
				playerEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slownessLevel, false, false, true));
		}
	}
}