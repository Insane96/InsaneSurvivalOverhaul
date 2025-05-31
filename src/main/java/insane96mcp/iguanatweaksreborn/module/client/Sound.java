package insane96mcp.iguanatweaksreborn.module.client;

import insane96mcp.iguanatweaksreborn.module.ClientModules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@LoadFeature(module = ClientModules.Ids.CLIENT,
        name = "Sounds & Music",
        description = "Changes to sounds and music. Disabling this feature requires a Minecraft restart.")
public class Sound extends Feature {

    @Config(name = "Music delay multiplier", description = "Multiplies the time it takes for music to play by this value (in vanilla, normal music plays each 10 to 20 minutes).")
    public static Double musicDelayMultiplier = 0.1d;

    public Sound(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);

        if (this.isEnabled()) {
            Musics.END = new Music(SoundEvents.MUSIC_END, (int) (6000 * musicDelayMultiplier), (int) (24000 * musicDelayMultiplier), true);
            Musics.UNDER_WATER = new Music(SoundEvents.MUSIC_UNDER_WATER, (int) (12000 * musicDelayMultiplier), (int) (24000 * musicDelayMultiplier), false);
            Musics.GAME = new Music(SoundEvents.MUSIC_GAME, (int) (12000 * musicDelayMultiplier), (int) (24000 * musicDelayMultiplier), false);
        }
    }
}
