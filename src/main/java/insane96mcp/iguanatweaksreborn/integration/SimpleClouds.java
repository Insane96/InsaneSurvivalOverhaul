package insane96mcp.iguanatweaksreborn.integration;

import dev.nonamecrackers2.simpleclouds.api.common.event.ModifyCloudSpeedEvent;
import dev.nonamecrackers2.simpleclouds.common.world.ServerCloudManager;
import dev.nonamecrackers2.simpleclouds.common.world.SyncType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

public class SimpleClouds {
	static int timeSkippedCloudsServer = 0;
	static int timeSkippedCloudsClient = 0;
	static final int TIME_SKIPPED_SPEED_SIMPLE_CLOUDS = 100;

	public static void onSleepSkipTime(int timeSkipped) {
		if (!ModList.get().isLoaded("simpleclouds"))
			return;

		timeSkippedCloudsServer += timeSkipped;
		//timeSkippedCloudsClient += timeSkipped;
	}

	public static void modifyCloudSpeed(ModifyCloudSpeedEvent event) {
		if (timeSkippedCloudsServer <= 0
				|| event.getLevel().isClientSide
				|| !event.getLevel().dimension().equals(Level.OVERWORLD))
			return;
		timeSkippedCloudsServer -= TIME_SKIPPED_SPEED_SIMPLE_CLOUDS;
		event.setSpeed(event.getCurrentSpeed() * TIME_SKIPPED_SPEED_SIMPLE_CLOUDS);
		if (timeSkippedCloudsServer <= 0)
			((ServerCloudManager)event.getCloudManager()).queueSync(SyncType.CLOUD_FORMATIONS);
	}
}
