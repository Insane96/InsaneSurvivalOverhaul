package insane96mcp.insanesurvivaloverhaul.module.farming.hoes;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOBlockTagsProvider;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.BlockStateBaseAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@LoadFeature(module = ISOModules.FARMING, description = "Hoes can now scythe grass and flowers. Also makes them break faster when tilling farmland")
public class Hoes extends Feature {

	public static final String TOO_WEAK = InsaneSO.lang("weak_hoe");
	public static final String SCYTHE_RADIUS = InsaneSO.lang("scythe_radius");
	public static final TagKey<Block> CAN_SCYTHE = ISOBlockTagsProvider.create("can_scythe");

	@Config(min = 1)
	public static Integer durabilityOnRightClick = 4;
	@Config(description = "'Durability on right click' is only applied for farmland, and not e.g. when using hoes on rooted or coarse dirt")
	public static Boolean extraDurabilityOnlyForTilling = true;

	@SubscribeEvent
	public void onHoeUse(BlockEvent.BlockToolModificationEvent event) {
		if (!this.isEnabled()
				|| event.getPlayer() == null
				|| event.isSimulated()
				|| event.getItemAbility() != ItemAbilities.HOE_TILL)
			return;

		if (event.getPlayer() != null && event.getPlayer().level().isClientSide)
			return;
		BlockState finalState = event.getState().getBlock().getToolModifiedState(event.getState(), event.getContext(), event.getItemAbility(), true);
		if (finalState == null || (!finalState.is(Blocks.FARMLAND) && extraDurabilityOnlyForTilling))
			return;
		if (durabilityOnRightClick > 1) {
			//noinspection DataFlowIssue
			event.getHeldItemStack().hurtAndBreak(durabilityOnRightClick - 1, event.getPlayer(), LivingEntity.getSlotForHand(event.getContext().getHand()));
		}
	}

	//Prevents scything again when breaking the blocks around, since breaking them fires BreakEvent too
	private static boolean scything = false;

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBlockBreak(BlockEvent.BreakEvent event) {
		if (!this.isEnabled()
				|| scything
				|| event.isCanceled()
				|| !(event.getPlayer() instanceof ServerPlayer player)
				|| ((BlockStateBaseAccessor) event.getState()).getDestroySpeed() > 0f
				|| !event.getState().is(CAN_SCYTHE))
			return;
		Integer radius = player.getMainHandItem().get(ISORegistries.SCYTHE_RADIUS.get());
		if (radius == null || radius == 0)
			return;
		scything = true;
		try {
			BlockPos.betweenClosedStream(event.getPos().offset(-radius, -(radius - 1), -radius), event.getPos().offset(radius, radius - 1, radius))
					.forEach(pos -> {
						BlockState state = player.level().getBlockState(pos);
						if (!state.is(CAN_SCYTHE)
								|| ((BlockStateBaseAccessor) state).getDestroySpeed() > 0f
								|| pos.equals(event.getPos()))
							return;
						//Breaks the block as if the player broke it: fires BreakEvent (so protections are respected), no drops in creative
						if (player.gameMode.destroyBlock(pos.immutable()))
							//The player's client doesn't predict the break of these blocks, so it doesn't get the particles from playerWillDestroy
							player.connection.send(new ClientboundLevelEventPacket(LevelEvent.PARTICLES_DESTROY_BLOCK, pos.immutable(), Block.getId(state), false));
					});
		}
		finally {
			scything = false;
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled())
			return;

		Integer radius = event.getItemStack().get(ISORegistries.SCYTHE_RADIUS.get());
		if (radius != null && radius > 0)
			event.getToolTip().add(CommonComponents.space().append(Component.translatable(SCYTHE_RADIUS, radius).withStyle(ChatFormatting.DARK_GREEN)));
	}
}
