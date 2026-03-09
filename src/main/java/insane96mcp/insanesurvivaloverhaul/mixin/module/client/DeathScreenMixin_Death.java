package insane96mcp.insanesurvivaloverhaul.mixin.module.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.client.death.Death;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin_Death extends Screen {

    @Shadow
    private Component deathScore;

    protected DeathScreenMixin_Death(Component title) {
        super(title);
    }

    /**
     * Suppresses the vanilla score line on the death screen when {@link Death#removeScore} is enabled
     * and the time-since-death replacement is not active.
     */
    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", ordinal = 2))
    public void insanesurvivaloverhaul$removeDeathScore(GuiGraphics instance, Font pFont, Component pText, int pX, int pY, int pColor, Operation<Void> original) {
        if (!Feature.isEnabled(Death.class)
                || !Death.removeScore
                || Death.replaceScoreWithTimeSinceLastDeath)
            original.call(instance, pFont, pText, pX, pY, pColor);
    }

    /**
     * Replaces the vanilla score line with a formatted time-since-last-death string
     * when {@link Death#replaceScoreWithTimeSinceLastDeath} is enabled.
     */
    @Inject(method = "init", at = @At("TAIL"))
    public void insanesurvivaloverhaul$replaceScoreWithTimeSinceDeath(CallbackInfo ci) {
        if (!Feature.isEnabled(Death.class) || !Death.replaceScoreWithTimeSinceLastDeath)
            return;
        int time = Death.syncedTimeSinceDeath / 20;
        int deaths = Death.syncedDeaths;
        String sTime = String.format("%dh %dm %ds", time / 3600, time % 3600 / 60, time % 60);
        float dayDuration = 20f;
        float days = time / 60f / dayDuration;
        if ((int) days > 0)
            sTime += String.format(" (%.1f days)", days);
        String lang = deaths <= 0 ? "deathScreen.firstDeath" : "deathScreen.sinceLastDeath";
        this.deathScore = Component.translatable(lang)
                .append(": ")
                .append(Component.literal(sTime).withStyle(ChatFormatting.YELLOW));
    }
}
