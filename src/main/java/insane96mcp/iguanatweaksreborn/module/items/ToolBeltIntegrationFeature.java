package insane96mcp.iguanatweaksreborn.module.items;

import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.integration.ToolBeltIntegration;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

@LoadFeature(module = Modules.Ids.ITEMS, name = "gigaherz Tool Belt", enabledByDefault = false)
public class ToolBeltIntegrationFeature extends Feature {

	@Config(description = "Enables a data pack that changes the crafting of the Tool Belt to give more slots (2 -> 4)")
	public static Boolean biggerBaseBelt = true;
	@Config(description = "Reduces cost to apply pouches to tool belts")
	public static Boolean reduceUpgradeCost = true;

	public ToolBeltIntegrationFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("toolbelt_integration", "Insane's Survival Overhaul Tool Belt Integration", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && biggerBaseBelt);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && ModList.get().isLoaded("toolbelt");
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onAnvilUpdate(AnvilUpdateEvent event) {
		if (!this.isEnabled()
				|| !reduceUpgradeCost
				|| !event.getLeft().is(ToolBelt.BELT.get())
				|| !event.getRight().is(ToolBelt.POUCH.get()))
			return;

		int slots = ToolBeltItem.getSlotsCount(event.getLeft()) - 4;
		if (slots < 0)
			event.setCost(0);
		else
			event.setCost(ToolBeltItem.xpCost[slots]);
	}

	@SubscribeEvent
	public void onLivingTick(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled())
			return;
		ToolBeltIntegration.tryTickItemsIn(event.getEntity());
	}
}