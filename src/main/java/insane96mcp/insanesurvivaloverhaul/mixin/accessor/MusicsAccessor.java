package insane96mcp.insanesurvivaloverhaul.mixin.accessor;

import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Musics.class)
public interface MusicsAccessor {
    @Accessor("CREATIVE")
    @Mutable
    static void setCreative(Music music) {
        throw new AssertionError();
    }
    @Accessor("END")
    @Mutable
    static void setEnd(Music music) {
        throw new AssertionError();
    }
    @Accessor("UNDER_WATER")
    @Mutable
    static void setUnderWater(Music music) {
        throw new AssertionError();
    }
    @Accessor("GAME")
    @Mutable
    static void setGame(Music music) {
        throw new AssertionError();
    }
}
