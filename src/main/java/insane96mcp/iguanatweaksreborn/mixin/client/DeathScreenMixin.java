package insane96mcp.iguanatweaksreborn.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.iguanatweaksreborn.module.client.Death;
import insane96mcp.iguanatweaksreborn.module.client.Misc;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin {

	@Shadow private Component deathScore;

	@WrapOperation(method = "setButtonsActive", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/components/Button;active:Z"))
	public void onSetButtonsActive(Button instance, boolean value, Operation<Void> original) {
		instance.visible = value;
		original.call(instance, value);
	}

	@ModifyExpressionValue(method = "tick", at = @At(value = "CONSTANT", args = "intValue=20"))
	public int onTick(int original) {
		if (!Feature.isEnabled(Misc.class)
				|| !Death.thirdPerson)
			return original;
		return 40;
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", ordinal = 2))
	public void onFinishInit(GuiGraphics instance, Font pFont, Component pText, int pX, int pY, int pColor, Operation<Void> original) {
		if (!Feature.isEnabled(Misc.class)
				|| !Death.removeScore)
			original.call(instance, pFont, pText, pX, pY, pColor);
	}
}
