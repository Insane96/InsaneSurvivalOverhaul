package insane96mcp.iguanatweaksreborn.mixin.client;

import insane96mcp.iguanatweaksreborn.module.client.Death;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "handlePlayerCombatKill", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", shift = At.Shift.AFTER))
	public void onClientPlayerDeath(ClientboundPlayerCombatKillPacket pPacket, CallbackInfo ci) {
		Death.onDeath();
	}
}
