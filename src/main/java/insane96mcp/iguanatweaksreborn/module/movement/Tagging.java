package insane96mcp.iguanatweaksreborn.module.movement;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Tagging", description = "Players's slowed down for a brief moment when hit.")
@LoadFeature(module = Modules.Ids.MOVEMENT)
public class Tagging extends Feature {

	@Config(min = 0, max = 10)
	@Label(name = "Slowness level", description = "Which level of Slowness is applied to the player (level 0 is Slowness I).")
	public static Integer slownessLevel = 0;
	@Config(min = 0, max = 100)
	@Label(name = "Duration multiplier", description = "Slowness is applied for damage_taken * this_value ticks.")
	public static Integer durationMultiplier = 7;

	public Tagging(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onDamageTaken(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof Player playerEntity))
			return;
		if (event.getSource().getEntity() instanceof LivingEntity) {
			int duration = (int) (event.getAmount() * durationMultiplier);
			if (duration > 0)
				playerEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, slownessLevel, false, false, true));
		}
	}
}