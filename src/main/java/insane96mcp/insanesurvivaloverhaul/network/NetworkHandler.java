package insane96mcp.insanesurvivaloverhaul.network;

import insane96mcp.insanesurvivaloverhaul.network.message.RegenAbsorptionSyncMessage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(RegenAbsorptionSyncMessage.TYPE, RegenAbsorptionSyncMessage.STREAM_CODEC, RegenAbsorptionSyncMessage::handle);
    }
}
