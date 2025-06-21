package insane96mcp.iguanatweaksreborn.module.world.leaves;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.world.scheduled.ScheduledTasks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.WORLD)
public class FasterDecayLeaves extends Feature {

	@Config(min = 1)
	public static Integer minTicksToDecay = 50;

	@Config(min = 1)
	public static Integer maxTicksToDecay = 300;

	public FasterDecayLeaves(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public void loadConfigOptions() {
		super.loadConfigOptions();
	}

	@SubscribeEvent
	public void onLogOrLeavesBroken(BlockEvent.BreakEvent event) {
		if (!(event.getLevel() instanceof ServerLevel serverLevel)
				|| (!serverLevel.getBlockState(event.getPos()).is(BlockTags.LOGS) && !serverLevel.getBlockState(event.getPos()).is(BlockTags.LEAVES)))
			return;
		decayAdjacentLeaves(serverLevel, event.getPos(), event.getLevel().getRandom(), .5f);
	}

	public static void decayAdjacentLeaves(ServerLevel level, BlockPos pos, RandomSource random, float multiplier) {
		if (!Feature.isEnabled(FasterDecayLeaves.class))
			return;

		Direction.stream().forEach(direction -> {
			BlockState adjacentState = level.getBlockState(pos.relative(direction));
			if (!adjacentState.is(BlockTags.LEAVES))
				return;
			ScheduledTasks.schedule(new LeafDecayScheduledTick(Mth.nextInt(random, (int) (minTicksToDecay * multiplier), (int) (maxTicksToDecay * multiplier)), level, pos.relative(direction), random));
		});
	}
}