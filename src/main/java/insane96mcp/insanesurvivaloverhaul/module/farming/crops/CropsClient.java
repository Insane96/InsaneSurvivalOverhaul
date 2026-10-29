package insane96mcp.insanesurvivaloverhaul.module.farming.crops;

import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.InputEvent;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.FARMING, canBeDisabled = false)
public class CropsClient extends Feature {
	@SubscribeEvent
	public void onPickBlock(InputEvent.InteractionKeyMappingTriggered event) {
		if (!isEnabled(Crops.class)
				|| !event.getKeyMapping().same(Minecraft.getInstance().options.keyPickItem)
				|| Minecraft.getInstance().player == null
				|| Minecraft.getInstance().level == null
				|| Minecraft.getInstance().gameMode == null
				|| Minecraft.getInstance().hitResult == null
				|| Minecraft.getInstance().hitResult.getType() != HitResult.Type.BLOCK)
			return;
		Minecraft mc = Minecraft.getInstance();
		boolean isCreative = mc.player.getAbilities().instabuild;
		HitResult hitResult = mc.hitResult;

		BlockPos blockpos = ((BlockHitResult) hitResult).getBlockPos();
		BlockState blockstate = mc.level.getBlockState(blockpos);
		if (blockstate.isAir())
			return;

		Block block = blockstate.getBlock();
		ItemStack stack = null;
		if (block == Blocks.CARROTS)
			stack = new ItemStack(Crops.CARROT_SEEDS.get());
		else if (block == Blocks.POTATOES)
			stack = new ItemStack(Crops.ROOTED_POTATO.get());
		if (stack == null)
			return;
		Inventory inventory = mc.player.getInventory();

		int i = inventory.findSlotMatchingItem(stack);
		if (isCreative) {
			inventory.setPickedItem(stack);
			mc.gameMode.handleCreativeModeItemAdd(mc.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + inventory.selected);
		}
		else if (i != -1) {
			if (Inventory.isHotbarSlot(i))
				inventory.selected = i;
			else
				mc.gameMode.handlePickItem(i);
		}
		event.setCanceled(true);
	}
}
