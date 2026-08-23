package insane96mcp.insanesurvivaloverhaul.module.respawn;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerRespawnPositionEvent;

import java.util.Optional;

@LoadFeature(module = ISOModules.RESPAWN, description = "Allows players to have a main bed where they will always respawn in if they lose a current bed.")
public class MainBed extends Feature {
	public static ResourceLocation MAIN_BED_POS;

	public static String SET_MAIN_BED_WITH_CROUCH = InsaneSO.lang("set_main_bed_with_crouch");
	public static String MAIN_BED_SET = InsaneSO.lang("main_bed_set");
	public static String MAIN_BED_SET_AS_RESPAWN_POINT = InsaneSO.lang("main_bed_set_as_respawn_point");

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		MAIN_BED_POS = this.createDataKey("main_bed_pos");
	}

	@SubscribeEvent
	public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		if (!this.isEnabled()
				|| !event.getLevel().getBlockState(event.getPos()).is(BlockTags.BEDS)
				|| event.getHand() == InteractionHand.OFF_HAND
				|| event.getLevel().isClientSide)
			return;

		if (event.getEntity().isCrouching()) {
			if (!event.getItemStack().isEmpty())
				return;
			setMainBedPos(event.getEntity(), event.getPos());
			event.getEntity().sendSystemMessage(Component.translatable(MAIN_BED_SET));
		}
		else {
			event.getEntity().sendSystemMessage(Component.translatable(SET_MAIN_BED_WITH_CROUCH));
		}
	}

	@SubscribeEvent
	public void onRespawnPosition(PlayerRespawnPositionEvent event) {
		if (!this.isEnabled()
				|| !event.getOriginalDimensionTransition().missingRespawnBlock()
				|| event.getOriginalDimensionTransition().newLevel().dimension() != Level.OVERWORLD)
			return;

		DimensionTransition originalDT = event.getOriginalDimensionTransition();

		Optional<BlockPos> mainBedPos = getMainBedPos(event.getEntity());
		if (mainBedPos.isEmpty())
			return;
		event.setCopyOriginalSpawnPosition(true);
		BlockPos pos = mainBedPos.get();
		Optional<Vec3> oRespawnPos = BedBlock.findStandUpPosition(EntityType.PLAYER, originalDT.newLevel(), pos, originalDT.newLevel().getBlockState(pos).getValue(BedBlock.FACING), 0);
		Vec3 respawnPos = oRespawnPos.orElse(pos.getBottomCenter());
		event.setDimensionTransition(new DimensionTransition(originalDT.newLevel(), respawnPos, originalDT.speed(), originalDT.xRot(), originalDT.yRot(), false, DimensionTransition.DO_NOTHING));
		event.getEntity().sendSystemMessage(Component.translatable(MAIN_BED_SET_AS_RESPAWN_POINT));
	}

	public static Optional<BlockPos> getMainBedPos(Player player) {
		Long mainBedPos = ModNBTData.getPersisted(player, MAIN_BED_POS, Long.class);
		if (mainBedPos == 0L)
			return Optional.empty();
		return Optional.of(BlockPos.of(mainBedPos));
	}

	public static void setMainBedPos(Player player, BlockPos pos) {
		ModNBTData.putPersisted(player, MAIN_BED_POS, pos.asLong());
	}
}
