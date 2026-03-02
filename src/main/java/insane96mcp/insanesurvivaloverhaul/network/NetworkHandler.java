package insane96mcp.insanesurvivaloverhaul.network;

import insane96mcp.insanesurvivaloverhaul.network.message.InvulnerableTimeSyncMessage;
import insane96mcp.insanesurvivaloverhaul.network.message.RegenAbsorptionSyncMessage;
import insane96mcp.insanesurvivaloverhaul.network.message.UnfairOneShotMessage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(RegenAbsorptionSyncMessage.TYPE, RegenAbsorptionSyncMessage.STREAM_CODEC, RegenAbsorptionSyncMessage::handle);
        registrar.playToClient(InvulnerableTimeSyncMessage.TYPE, InvulnerableTimeSyncMessage.STREAM_CODEC, InvulnerableTimeSyncMessage::handle);
        registrar.playToClient(UnfairOneShotMessage.TYPE, UnfairOneShotMessage.STREAM_CODEC, UnfairOneShotMessage::handle);
    }
}
