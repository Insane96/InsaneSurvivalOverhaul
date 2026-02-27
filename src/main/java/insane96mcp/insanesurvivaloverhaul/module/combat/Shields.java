package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@LoadFeature(module = ISOModules.COMBAT, description = "This feature disables itself if Shields+ is installed")
public class Shields extends Feature {
	@Config(min = 0, description = "In vanilla when you start blocking with a shield, there's a 0.25 seconds (5 ticks) window where you are still not blocking. By default the windup is removed.")
	public static Integer shieldWindup = 0;
	@Config(min = 0d, max = Float.MAX_VALUE, description = "The minimum damage dealt to the player for the shield to take damage. Vanilla is 3. E.g. With this set to 3, the shield will not be damaged if damage received is lower than 3.")
	public static Double minShieldHurtDamage = 0d;
	@Config(min = 0, description = "The amount of damage a shield blocks. Set to 0 to disable")
	public static Integer shieldBlockDamage = 5;

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && !ModList.get().isLoaded("shieldsplus");
	}

	public static int getShieldWindUp(int original) {
		return isEnabled(Shields.class) ? shieldWindup : original;
	}

	public static float getMinHurtDamage(float original) {
		return isEnabled(Shields.class) ? minShieldHurtDamage.floatValue() : original;
	}

	@SubscribeEvent
	public void blockEvent(LivingShieldBlockEvent event) {
		if (!this.isEnabled()
				|| shieldBlockDamage == 0)
			return;

		event.setBlockedDamage(shieldBlockDamage);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemTooltips(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| shieldBlockDamage == 0
				|| !event.getItemStack().is(Items.SHIELD))
			return;

		event.getToolTip().add(CommonComponents.space().append(Component.translatable(InsaneSO.lang("shield_blocked_damage"), shieldBlockDamage).withStyle(ChatFormatting.DARK_GREEN)));
	}

}