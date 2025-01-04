package insane96mcp.iguanatweaksreborn.module.farming.hoes;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.data.generator.ISOBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.items.misc.ItemDefinition;
import insane96mcp.iguanatweaksreborn.module.items.misc.ItemDefinitionsReloadListener;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Hoes", description = "Hoes can now scythe grass and flowers. Also makes them break faster when tilling farmland")
@LoadFeature(module = Modules.Ids.FARMING)
public class Hoes extends Feature {

	public static final String TOO_WEAK = InsaneSurvivalOverhaul.MOD_ID + ".weak_hoe";
	public static final String SCYTHE_RADIUS = InsaneSurvivalOverhaul.MOD_ID + ".scythe_radius";
	public static final TagKey<Block> CAN_SCYTHE = ISOBlockTagsProvider.create("can_scythe");
	public static final TagKey<Item> DISABLED_HOES = ISOItemTagsProvider.create("disabled_hoes");

	@Config(min = 1)
	@Label(name = "Durability used on right-click")
	public static Integer durabilityOnRightClick = 4;
	@Config
	@Label(name = "Extra durability only for tilling", description = "'Durability used on right-click' is only applied for farmland, and not e.g. when using hoes on rooted dirt")
	public static Boolean extraDurabilityOnlyForTilling = true;

	public Hoes(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onHoeUse(BlockEvent.BlockToolModificationEvent event) {
		if (!this.isEnabled()
				|| event.getPlayer() == null
				|| event.isSimulated()
				|| event.getToolAction() != ToolActions.HOE_TILL)
			return;

		boolean isHoeDisabled = disabledHoes(event);
		if (event.getPlayer() != null && event.getPlayer().level().isClientSide)
			return;
		BlockState finalState = event.getState().getBlock().getToolModifiedState(event.getState(), event.getContext(), event.getToolAction(), true);
		if (finalState == null || (!finalState.is(Blocks.FARMLAND) && extraDurabilityOnlyForTilling))
			return;
		if (!isHoeDisabled && durabilityOnRightClick > 1) {
			//noinspection DataFlowIssue
			event.getHeldItemStack().hurtAndBreak(durabilityOnRightClick - 1, event.getPlayer(), (livingEntity) -> livingEntity.broadcastBreakEvent(livingEntity.getUsedItemHand()));
		}
	}

	public boolean disabledHoes(BlockEvent.BlockToolModificationEvent event) {
		if (!event.getHeldItemStack().is(DISABLED_HOES))
			return false;

		//noinspection ConstantConditions getPlayer can't be null as it's called from onHoeUse that checks if player's null
		event.getPlayer().displayClientMessage(Component.translatable(TOO_WEAK), true);
		event.setCanceled(true);
		return true;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		if (!this.isEnabled()
				|| event.getState().destroySpeed > 0f)
			return;
		for (ItemDefinition itemDefinition : ItemDefinitionsReloadListener.getDefinitions()) {
			if (!itemDefinition.matches(event.getPlayer().getMainHandItem())
					|| itemDefinition.scytheRadius() == null)
				continue;
			int radius = itemDefinition.scytheRadius();
			if (radius == 0)
				continue;
			BlockPos.betweenClosedStream(event.getPos().offset(-radius, -(radius - 1), -radius), event.getPos().offset(radius, radius - 1, radius))
					.forEach(pos -> {
						BlockState state = event.getPlayer().level().getBlockState(pos);
						state.getBlock().playerWillDestroy(event.getPlayer().level(), pos, state, event.getPlayer());
						if (!state.is(CAN_SCYTHE)
								|| state.destroySpeed > 0f
								|| pos.equals(event.getPos()))
							return;
						//event.getPlayer().level().removeBlock(pos, false);
						if (state.getBlock().canHarvestBlock(state, event.getPlayer().level(), pos, event.getPlayer()))
							state.getBlock().playerDestroy(event.getPlayer().level(), event.getPlayer(), pos, state, null, event.getPlayer().getMainHandItem());
						event.getLevel().destroyBlock(pos, false);
						event.getLevel().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
						//BreakWithNoSound.send((ServerPlayer) event.getPlayer(), pos, state);
					});
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled())
			return;

		if (event.getItemStack().is(DISABLED_HOES)) {
			event.getToolTip().add(Component.translatable(TOO_WEAK).withStyle(ChatFormatting.RED));
		}
		else {
			for (ItemDefinition itemDefinition : ItemDefinitionsReloadListener.getDefinitions()) {
				if (!itemDefinition.matches(event.getItemStack().getItem()))
					continue;

				int radius = itemDefinition.scytheRadius() == null ? 0 : itemDefinition.scytheRadius();
				if (radius == 0)
					continue;
				event.getToolTip().add(CommonComponents.space().append(Component.translatable(SCYTHE_RADIUS, radius).withStyle(ChatFormatting.DARK_GREEN)));
				break;
			}
		}
	}
}