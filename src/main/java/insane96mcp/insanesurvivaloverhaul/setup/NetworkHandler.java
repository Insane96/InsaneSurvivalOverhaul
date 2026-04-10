package insane96mcp.insanesurvivaloverhaul.setup;

import insane96mcp.insanesurvivaloverhaul.module.client.death.ClientboundDeathStatsPacket;
import insane96mcp.insanesurvivaloverhaul.module.combat.attackspeedbasedinvincibility.ClientboundInvulnerableTimePacket;
import insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption.ClientboundRegenAbsorptionPacket;
import insane96mcp.insanesurvivaloverhaul.module.combat.unfaironeshot.UnfairOneShotPacket;
import insane96mcp.insanesurvivaloverhaul.module.farming.livestock.ClientboundMilkCooldownPacket;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.exhaustion.ClientboundExhaustionPacket;
import insane96mcp.insanesurvivaloverhaul.module.hungerhealth.exhaustion.ClientboundSaturationPacket;
import insane96mcp.insanesurvivaloverhaul.module.mining.blockdefinition.ClientboundBlockDefinitionPacket;
import insane96mcp.insanesurvivaloverhaul.module.misc.tweaks.ClientboundDiscreteNameTagsPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(ClientboundDeathStatsPacket.TYPE, ClientboundDeathStatsPacket.STREAM_CODEC, ClientboundDeathStatsPacket::handle);
        registrar.playToClient(ClientboundRegenAbsorptionPacket.TYPE, ClientboundRegenAbsorptionPacket.STREAM_CODEC, ClientboundRegenAbsorptionPacket::handle);
        registrar.playToClient(ClientboundInvulnerableTimePacket.TYPE, ClientboundInvulnerableTimePacket.STREAM_CODEC, ClientboundInvulnerableTimePacket::handle);
        registrar.playToClient(UnfairOneShotPacket.TYPE, UnfairOneShotPacket.STREAM_CODEC, UnfairOneShotPacket::handle);
        registrar.playToClient(ClientboundDiscreteNameTagsPacket.TYPE, ClientboundDiscreteNameTagsPacket.STREAM_CODEC, ClientboundDiscreteNameTagsPacket::handle);
        registrar.playToClient(ClientboundSaturationPacket.TYPE, ClientboundSaturationPacket.STREAM_CODEC, ClientboundSaturationPacket::handle);
        registrar.playToClient(ClientboundExhaustionPacket.TYPE, ClientboundExhaustionPacket.STREAM_CODEC, ClientboundExhaustionPacket::handle);
        registrar.playToClient(ClientboundBlockDefinitionPacket.TYPE, ClientboundBlockDefinitionPacket.STREAM_CODEC, ClientboundBlockDefinitionPacket::handle);
        registrar.playToClient(ClientboundMilkCooldownPacket.TYPE, ClientboundMilkCooldownPacket.STREAM_CODEC, ClientboundMilkCooldownPacket::handle);
    }
}
