package insane96mcp.iguanatweaksreborn.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.iguanatweaksreborn.module.client.Death;
import insane96mcp.iguanatweaksreborn.module.client.Misc;
import insane96mcp.iguanatweaksreborn.module.world.seasons.Seasons;
import insane96mcp.insanelib.base.Feature;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen {

	@Shadow private Component deathScore;

	protected DeathScreenMixin(Component pTitle) {
		super(pTitle);
	}

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
				|| !Death.removeScore
				|| Death.replaceScoreWithTimeSinceLastDeath)
			original.call(instance, pFont, pText, pX, pY, pColor);
	}

	@Inject(method = "init", at = @At("TAIL"))
	public void onInit(CallbackInfo ci) {
		if (!Feature.isEnabled(Misc.class)
				|| !Death.replaceScoreWithTimeSinceLastDeath)
			return;
		int time = this.minecraft.player.getStats().getValue(Stats.CUSTOM, Stats.TIME_SINCE_DEATH) / 20;
		String sTime = String.format("%dh %dm %ds", time / 3600, time % 3600 / 60, time % 60);
		float dayDuration = 20f;
		if (ModList.get().isLoaded("sereneseasons"))
			dayDuration = Seasons.getDayDuration();
		float days = time / 60f / dayDuration;
		if ((int) days > 0)
			sTime += String.format(" (%.1f days)", days);
		this.deathScore = Component.translatable("deathScreen.sinceLastDeath")
				.append(": ")
				.append(Component.literal(sTime).withStyle(ChatFormatting.YELLOW));
	}
}
