package insane96mcp.insanesurvivaloverhaul.module.mining;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@LoadFeature(module = ISOModules.MINING, description = "Various mining changes")
public class MiningMisc extends Feature {
	@Config(name = "Faster slabs, stairs and walls", description = "Makes slabs, stairs and walls take less time to break")
	public static Boolean fastSlabsStairsWalls = true;
	@Config(description = "In vanilla there's a 5 tick delay (0.25 secs) between breaking blocks. The tick delay is reduced by 1 tick every 2.5 tool mining speed.")
	public static Boolean miningSpeedBasedDestroyDelay = true;
	@Config(description = "Tools with a mining speed set via the minecraft:tool component show it as a tooltip.")
	public static Boolean miningSpeedTooltip = true;

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
		/*if (ModList.get().isLoaded("caverns_and_chasms") && cavernsChasmsIntegration) {
			CCConfig.COMMON.chainmailArmorIncreasesDamage.set(false);
			CCConfig.COMMON.goldenArmorIncreasesSpeed.set(false);
			CCConfig.COMMON.creeperExplosionNerf.set(false);
		}*/
	}

	@SubscribeEvent
	public void onBreak(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled()
				|| !fastSlabsStairsWalls)
			return;

		if (event.getState().getBlock() instanceof SlabBlock && (event.getState().getValue(SlabBlock.TYPE) == SlabType.TOP || event.getState().getValue(SlabBlock.TYPE) == SlabType.BOTTOM))
			event.setNewSpeed(event.getOriginalSpeed() * 2f);
		if (event.getState().getBlock() instanceof StairBlock)
			event.setNewSpeed(event.getOriginalSpeed() * 1.3333333f);
		if (event.getState().getBlock() instanceof WallBlock)
			event.setNewSpeed(event.getOriginalSpeed() * 1.5f);
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled() || !miningSpeedTooltip)
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

	public static int destroyDelay(ItemStack stack, DiggerItem item, BlockState state) {
		if (!isEnabled(MiningMisc.class)
				|| !miningSpeedBasedDestroyDelay)
			return 5;
		float delay = 5f - (int) (item.getTier().getSpeed() / 2.5f);
		//int efficiency = stack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY);
		//if (efficiency > 0)
		//	delay -= efficiency / 2f;
		return (int) Math.max(delay, 1);
	}
}
