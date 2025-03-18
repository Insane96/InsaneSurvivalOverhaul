package insane96mcp.iguanatweaksreborn.module.mining;

import com.teamabnormals.caverns_and_chasms.core.CCConfig;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Label(name = "Misc", description = "Various mining changes")
@LoadFeature(module = Modules.Ids.MINING)
public class MiningMisc extends Feature {

	@Config
	@Label(name = "Insta-Mine Silverfish", description = "Silverfish blocks will insta-mine like pre-1.17")
	public static Boolean instaMineSilverfish = true;
	@Config
	@Label(name = "Insta-Mine Heads", description = "Heads will insta-break")
	public static Boolean instaMineHeads = true;

	@Config
	@Label(name = "Faster slabs, stairs and walls", description = "Makes slabs, stairs and walls take less time to break")
	public static Boolean fastSlabsStairsWalls = true;
	@Config
	@Label(name = "Efficiency based destroy delay", description = "In vanilla there's a 5 tick delay (0.25 secs) between breaking blocks. The tick delay is reduced by 1 tick every 2 tool mining speed.")
	public static Boolean efficiencyBasedDestroyDelay = true;
	@Config
	@Label(name = "Caverns and Chasms Integration", description = "Changes some Caverns and Chasms config options.")
	public static Boolean cavernsChasmsIntegration = true;

	public MiningMisc(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public void readConfig(ModConfigEvent event) {
		super.readConfig(event);
		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			if ((instaMineHeads && block instanceof AbstractSkullBlock)
					|| (instaMineSilverfish && block instanceof InfestedBlock))
				block.getStateDefinition().getPossibleStates().forEach(blockState -> blockState.destroySpeed = 0f);
		}
		if (ModList.get().isLoaded("caverns_and_chasms") && cavernsChasmsIntegration) {
			CCConfig.COMMON.chainmailArmorIncreasesDamage.set(false);
			CCConfig.COMMON.goldenArmorIncreasesSpeed.set(false);
			CCConfig.COMMON.creeperExplosionNerf.set(false);
		}
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
		return isEnabled(MiningMisc.class) && efficiencyBasedDestroyDelay ? Math.max(5 - (int) (item.speed / 2f), 1) : 5;
	}
}
