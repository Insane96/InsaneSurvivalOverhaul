package insane96mcp.insanesurvivaloverhaul.module.mining;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.BlockStateBaseAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@LoadFeature(module = ISOModules.MINING, description = "Various mining changes")
public class MiningMisc extends Feature {
	@Config(name = "Insta-Mine Silverfish", description = "Silverfish blocks will insta-mine like pre-1.17")
	public static Boolean instaMineSilverfish = true;
	@Config(name = "Insta-Mine Heads", description = "Heads will insta-break")
	public static Boolean instaMineHeads = true;

	@Config(name = "Faster slabs, stairs and walls", description = "Makes slabs, stairs and walls take less time to break")
	public static Boolean fastSlabsStairsWalls = true;
	@Config(description = "In vanilla there's a 5 tick delay (0.25 secs) between breaking blocks. The tick delay is reduced by 1 tick every 2.5 tool mining speed.")
	public static Boolean efficiencyBasedDestroyDelay = true;
	//@Config(description = "Changes some Caverns and Chasms config options.")
	//public static Boolean cavernsChasmsIntegration = false;

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
		for (Block block : BuiltInRegistries.BLOCK.stream().toList()) {
			if ((instaMineHeads && block instanceof AbstractSkullBlock)
					|| (instaMineSilverfish && block instanceof InfestedBlock))
				block.getStateDefinition().getPossibleStates().forEach(blockState -> ((BlockStateBaseAccessor) blockState).setDestroySpeed(0f));
		}
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

	public static int destroyDelay(ItemStack stack, DiggerItem item, BlockState state) {
		if (!isEnabled(MiningMisc.class)
				|| !efficiencyBasedDestroyDelay)
			return 5;
		float delay = 5f - (int) (item.getTier().getSpeed() / 2.5f);
		//int efficiency = stack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY);
		//if (efficiency > 0)
		//	delay -= efficiency / 2f;
		return (int) Math.max(delay, 1);
	}
}
