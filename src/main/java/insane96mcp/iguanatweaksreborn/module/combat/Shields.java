package insane96mcp.iguanatweaksreborn.module.combat;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

@LoadFeature(module = Modules.Ids.COMBAT, description = "This feature disables itself if Shields+ is installed")
public class Shields extends Feature {
	@Config(min = 0, description = "In vanilla when you start blocking with a shield, there's a 0.25 seconds (5 ticks) window where you are still not blocking. By default the windup is removed.")
	public static Integer shieldWindup = 0;
	@Config(min = 0d, max = Float.MAX_VALUE, description = "The minimum damage dealt to the player for the shield to take damage. Vanilla is 3. E.g. With this set to 3, the shield will not be damaged if damage received is lower than.")
	public static Double minShieldHurtDamage = 0d;
	@Config(min = 0, description = "The amount of damage a shield blocks. Set to 0 to disable")
	public static Integer shieldBlockDamage = 5;

	public Shields(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

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
	public void blockEvent(ShieldBlockEvent event) {
		if (!this.isEnabled()
				|| shieldBlockDamage == 0)
			return;

		event.setBlockedDamage(shieldBlockDamage);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemTooltips(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| shieldBlockDamage == 0)
			return;

		event.getToolTip().add(CommonComponents.space().append(Component.translatable("iguanatweaksreborn.shield_block_damage").withStyle(ChatFormatting.DARK_GREEN)));
	}

}