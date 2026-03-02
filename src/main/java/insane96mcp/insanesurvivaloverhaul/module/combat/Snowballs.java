package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.IntegratedPack;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@LoadFeature(module = ISOModules.COMBAT)
public class Snowballs extends Feature {
	@Config(min = 0d, max = 100d , description = "Snowballs deal this amount of damage.")
	public static Double damage = 0.5d;
	@Config(min = 0, description = "Snowballs fill freeze entities for this amount of ticks.")
	public static Integer freezingTicks = 35;
	@Config(description = "If true, freezing stacks each hit.")
	public static Boolean freezingStacks = true;
	@Config(description = "Ticks between throwing snowball.")
	public static Integer cooldown = 4;
	@Config(description = "Enables a data pack that increases snowballs stack size to 64")
	public static Boolean snowballStackSize = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addServerPack(InsaneSO.MOD_ID, "snowball_stack_size", "InsaneLib's Snowball Stack Size", () -> this.isEnabled() && snowballStackSize);
	}

	@SubscribeEvent
	public void onSnowballThrow(PlayerInteractEvent.RightClickItem event) {
		if (!this.isEnabled()
				|| !(event.getItemStack().is(Items.SNOWBALL)))
			return;

		event.getEntity().getCooldowns().addCooldown(Items.SNOWBALL, cooldown);
	}

	@SubscribeEvent
	public void onLivingHurt(LivingIncomingDamageEvent event) {
		if (!this.isEnabled()
				|| damage == 0d
				|| !(event.getSource().getDirectEntity() instanceof Snowball)
				|| event.getEntity() instanceof Blaze)
			return;

		event.setAmount(damage.floatValue());
	}

	@SubscribeEvent
	public void onLivingHurt(LivingDamageEvent.Pre event) {
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