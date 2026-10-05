package insane96mcp.insanesurvivaloverhaul.mixin.module.death.client;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.death.TimeSinceLastDeath;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin_TimeSinceLastDeath extends Screen {

    @Shadow
    private Component deathScore;

    protected DeathScreenMixin_TimeSinceLastDeath(Component title) {
        super(title);
    }

    /** The time-since-last-death line computed on the first init, reused when the screen is re-initialized (e.g. on resize). */
    @Unique
    private Component insanesurvivaloverhaul$timeSinceDeath;

    /**
     * Replaces the vanilla score line with a formatted time-since-last-death string
     * when {@link TimeSinceLastDeath} is enabled and stats have been synced from the server.
     * The line is computed once per screen and reapplied on every subsequent init.
     */
    @Inject(method = "init", at = @At("TAIL"))
    public void insanesurvivaloverhaul$replaceScoreWithTimeSinceDeath(CallbackInfo ci) {
        if (this.insanesurvivaloverhaul$timeSinceDeath == null) {
            if (!Feature.isEnabled(TimeSinceLastDeath.class)
                    || TimeSinceLastDeath.syncedTimeSinceDeath == -1
                    || TimeSinceLastDeath.syncedDeaths == -1)
                return;
            int time = TimeSinceLastDeath.syncedTimeSinceDeath / 20;
            int deaths = TimeSinceLastDeath.syncedDeaths;
            String sTime = String.format("%dh %dm %ds", time / 3600, time % 3600 / 60, time % 60);
            float dayDuration = 20f;
            float days = time / 60f / dayDuration;
            if ((int) days > 0)
                sTime += String.format(" (%.1f days)", days);
            String lang = deaths <= 0 ? "deathScreen.firstDeath" : "deathScreen.sinceLastDeath";
            this.insanesurvivaloverhaul$timeSinceDeath = Component.translatable(lang)
                    .append(": ")
                    .append(Component.literal(sTime).withStyle(ChatFormatting.YELLOW));
            TimeSinceLastDeath.syncedTimeSinceDeath = -1;
            TimeSinceLastDeath.syncedDeaths = -1;
        }
        this.deathScore = this.insanesurvivaloverhaul$timeSinceDeath;
    }
}
