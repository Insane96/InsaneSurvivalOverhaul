package insane96mcp.iguanatweaksreborn.module.items;

import dev.gigaherz.toolbelt.ToolBelt;
import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

@Label(name = "gigaherz Tool Belt")
@LoadFeature(module = Modules.Ids.ITEMS)
public class ToolBeltIntegration extends Feature {

	@Config
	@Label(name = "Bigger base belt", description = "Enables a data pack that changes the crafting of the Tool Belt to give more slots (2 -> 4)")
	public static Boolean biggerBaseBelt = true;
	@Config
	@Label(name = "Reduce upgrade cost", description = "Reduces cost to apply pouches to tool belts")
	public static Boolean reduceUpgradeCost = true;

	public ToolBeltIntegration(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "toolbelt_integration", Component.literal("Insane's Survival Overhaul Tool Belt Integration"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && biggerBaseBelt));
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
		insane96mcp.iguanatweaksreborn.module.sleeprespawn.death.integration.ToolBelt.tryTickItemsIn(event.getEntity());
	}
}