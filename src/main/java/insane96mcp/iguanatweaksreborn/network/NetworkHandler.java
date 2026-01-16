package insane96mcp.iguanatweaksreborn.network;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn.RespawnPointSelectedMessage;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn.RespawnPointsScreenMessage;
import insane96mcp.iguanatweaksreborn.module.world.spawners.SpawnerStatusSync;
import insane96mcp.iguanatweaksreborn.network.message.*;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
	private static final String PROTOCOL_VERSION = Integer.toString(6);
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			InsaneSO.location("network_channel"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	private static int index = 0;

	public static void init() {
		CHANNEL.registerMessage(++index, InvulnerableTimeMessageSync.class, InvulnerableTimeMessageSync::encode, InvulnerableTimeMessageSync::decode, InvulnerableTimeMessageSync::handle);
		CHANNEL.registerMessage(++index, AnvilRepairSync.class, AnvilRepairSync::encode, AnvilRepairSync::decode, AnvilRepairSync::handle);
		CHANNEL.registerMessage(++index, ItemDefinitionsSync.class, ItemDefinitionsSync::encode, ItemDefinitionsSync::decode, ItemDefinitionsSync::handle);
		CHANNEL.registerMessage(++index, StackSizesSync.class, StackSizesSync::encode, StackSizesSync::decode, StackSizesSync::handle);
		CHANNEL.registerMessage(++index, BackwardsSlowdownUpdate.class, BackwardsSlowdownUpdate::encode, BackwardsSlowdownUpdate::decode, BackwardsSlowdownUpdate::handle);
		CHANNEL.registerMessage(++index, ExhaustionSync.class, ExhaustionSync::encode, ExhaustionSync::decode, ExhaustionSync::handle);
		CHANNEL.registerMessage(++index, SaturationSync.class, SaturationSync::encode, SaturationSync::decode, SaturationSync::handle);
		CHANNEL.registerMessage(++index, GlobalHardnessSync.class, GlobalHardnessSync::encode, GlobalHardnessSync::decode, GlobalHardnessSync::handle);
		CHANNEL.registerMessage(++index, SpawnerStatusSync.class, SpawnerStatusSync::encode, SpawnerStatusSync::decode, SpawnerStatusSync::handle);
		CHANNEL.registerMessage(++index, TirednessSync.class, TirednessSync::encode, TirednessSync::decode, TirednessSync::handle);
		CHANNEL.registerMessage(++index, SetITRBeaconEffects.class, SetITRBeaconEffects::encode, SetITRBeaconEffects::decode, SetITRBeaconEffects::handle);
		CHANNEL.registerMessage(++index, RegenAbsorptionSync.class, RegenAbsorptionSync::encode, RegenAbsorptionSync::decode, RegenAbsorptionSync::handle);
		CHANNEL.registerMessage(++index, ForgeDataIntSync.class, ForgeDataIntSync::encode, ForgeDataIntSync::decode, ForgeDataIntSync::handle);
		CHANNEL.registerMessage(++index, BlockDefinitionSync.class, BlockDefinitionSync::encode, BlockDefinitionSync::decode, BlockDefinitionSync::handle);
		CHANNEL.registerMessage(++index, SyncExperienceFeature.class, SyncExperienceFeature::encode, SyncExperienceFeature::decode, SyncExperienceFeature::handle);
		CHANNEL.registerMessage(++index, BreakWithNoSound.class, BreakWithNoSound::encode, BreakWithNoSound::decode, BreakWithNoSound::handle);
		CHANNEL.registerMessage(++index, ExplodeParticles.class, ExplodeParticles::encode, ExplodeParticles::decode, ExplodeParticles::handle);
		CHANNEL.registerMessage(++index, FoggySync.class, FoggySync::encode, FoggySync::decode, FoggySync::handle);
		CHANNEL.registerMessage(++index, UnfairOneShotActivation.class, UnfairOneShotActivation::encode, UnfairOneShotActivation::decode, UnfairOneShotActivation::handle);
		CHANNEL.registerMessage(++index, SyncDiscreteNameTags.class, SyncDiscreteNameTags::encode, SyncDiscreteNameTags::decode, SyncDiscreteNameTags::handle);
		CHANNEL.registerMessage(++index, FoggyEnabledSync.class, FoggyEnabledSync::encode, FoggyEnabledSync::decode, FoggyEnabledSync::handle);
		CHANNEL.registerMessage(++index, RespawnPointsScreenMessage.class, RespawnPointsScreenMessage::encode, RespawnPointsScreenMessage::decode, RespawnPointsScreenMessage::handle);
		CHANNEL.registerMessage(++index, RespawnPointSelectedMessage.class, RespawnPointSelectedMessage::encode, RespawnPointSelectedMessage::decode, RespawnPointSelectedMessage::handle);
	}
}
