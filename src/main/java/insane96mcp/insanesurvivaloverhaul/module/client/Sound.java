package insane96mcp.insanesurvivaloverhaul.module.client;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.MusicsAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOClientModules;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.fml.event.config.ModConfigEvent;

@LoadFeature(module = ISOClientModules.CLIENT,
        name = "Sounds & Music",
        description = "Changes to sounds and music. Disabling this feature requires a Minecraft restart.")
public class Sound extends Feature {

    @Config(name = "Music delay multiplier", description = "Multiplies the time it takes for music to play by this value (in vanilla, normal music plays each 10 to 20 minutes).")
    public static Double musicDelayMultiplier = 0.1d;

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);

        if (this.isEnabled()) {
            MusicsAccessor.setCreative(new Music(SoundEvents.MUSIC_CREATIVE, (int) (12000 * musicDelayMultiplier), (int) (24000 * musicDelayMultiplier), true));
            MusicsAccessor.setEnd(new Music(SoundEvents.MUSIC_END, (int) (6000 * musicDelayMultiplier), (int) (24000 * musicDelayMultiplier), true));
            MusicsAccessor.setUnderWater(new Music(SoundEvents.MUSIC_UNDER_WATER, (int) (12000 * musicDelayMultiplier), (int) (24000 * musicDelayMultiplier), false));
            MusicsAccessor.setGame(new Music(SoundEvents.MUSIC_GAME, (int) (12000 * musicDelayMultiplier), (int) (24000 * musicDelayMultiplier), false));
        }
    }
}
