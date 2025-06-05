package insane96mcp.iguanatweaksreborn.module.combat;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.COMBAT)
public class Snowballs extends Feature {
	@Config(min = 0d, max = 100d , description = "Snowballs deal this amount of damage.")
	public static Double damage = 0.5d;
	@Config(min = 0, description = "Snowballs fill freeze entities for this amount of ticks.")
	public static Integer freezingTicks = 30;
	@Config(description = "If true, freezing stacks each hit.")
	public static Boolean freezingStacks = true;

	public Snowballs(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onLivingHurt(LivingHurtEvent event) {
		if (!this.isEnabled()
				|| damage == 0d
				|| !(event.getSource().getDirectEntity() instanceof Snowball)
				|| event.getEntity() instanceof Blaze)
			return;

		event.setAmount(damage.floatValue());
	}

	@SubscribeEvent
	public void onLivingHurt(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| freezingTicks == 0
				|| !(event.getSource().getDirectEntity() instanceof Snowball))
			return;

		if (freezingStacks)
			event.getEntity().setTicksFrozen(event.getEntity().getTicksFrozen() + freezingTicks);
		else if (event.getEntity().getTicksFrozen() < freezingTicks)
			event.getEntity().setTicksFrozen(freezingTicks);
	}
}