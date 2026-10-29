package insane96mcp.insanesurvivaloverhaul.module.hungerhealth;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.food.FoodData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

import java.text.DecimalFormat;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.HUNGER_HEALTH, canBeDisabled = false)
public class HungerAndHealthRegenClient extends Feature {
	@SubscribeEvent
	public void debugScreen(CustomizeGuiOverlayEvent.DebugText event) {
		if (!isEnabled(HungerAndHealthRegen.class)
			|| ModList.get().isLoaded("nohunger"))
			return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer playerEntity = mc.player;
		if (playerEntity == null)
			return;
		if (mc.getDebugOverlay().showDebugScreen() && !mc.showOnlyReducedInfo()) {
			FoodData foodStats = playerEntity.getFoodData();
			event.getLeft().add(String.format("Hunger: %d, Saturation: %s, Exhaustion: %s", foodStats.getFoodLevel(), new DecimalFormat("#.#").format(foodStats.getSaturationLevel()), new DecimalFormat("0.00").format(foodStats.getExhaustionLevel())));
		}
	}
}
