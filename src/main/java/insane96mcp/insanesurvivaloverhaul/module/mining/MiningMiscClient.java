package insane96mcp.insanesurvivaloverhaul.module.mining;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.component.Tool;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.MINING, canBeDisabled = false)
public class MiningMiscClient extends Feature {
	@Config(description = "Tools with a mining speed set via the minecraft:tool component show it as a tooltip.")
	public static Boolean miningSpeedTooltip = true;

	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent event) {
		if (!isEnabled(MiningMisc.class) || !miningSpeedTooltip)
			return;
		Tool toolComponent = event.getItemStack().get(DataComponents.TOOL);
		if (toolComponent == null)
			return;
		toolComponent.rules().stream()
				.filter(r -> r.speed().isPresent())
				.mapToDouble(r -> r.speed().get())
				.findFirst()
				.ifPresent(speed -> {
					var tips = event.getToolTip();
					Component line = CommonComponents.space()
							.append(Component.translatable("insanesurvivaloverhaul.tool_mining_speed", speed))
							.withStyle(ChatFormatting.DARK_GREEN);
					int index = tips.size();
					for (int i = 1; i < tips.size(); i++) {
						if (tips.get(i).getContents() instanceof TranslatableContents tc
								&& tc.getKey().startsWith("insanesurvivaloverhaul.")
								&& tc.getKey().contains("durability")) {
							index = i;
							break;
						}
					}
					tips.add(index, line);
				});
	}
}
