package insane96mcp.insanesurvivaloverhaul.module.misc;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.BlockBehaviourAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.event.config.ModConfigEvent;

@LoadFeature(module = ISOModules.MISC, description = "Sponges soak up more water and can dry/wet with the weather.")
public class Sponges extends Feature {

    @Config(description = "The maximum amount of blocks a sponge can soak. (Vanilla is 64, disabled if quark is installed)")
    public static Integer maxSoakBlocks = 256;
    @Config(description = "The maximum range at which sponges will check for soakable blocks. (Vanilla is 5, disabled if quark is installed)")
    public static Integer maxSoakRange = 10;
    @Config(description = "If exposed to the sun sponges may dry and if exposed to rain sponges might get wet")
    public static Boolean dryWetWeather = true;

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        ((BlockBehaviourAccessor) Blocks.WET_SPONGE).setIsRandomlyTicking(dryWetWeather);
        ((BlockBehaviourAccessor) Blocks.SPONGE).setIsRandomlyTicking(dryWetWeather);
    }

    public static int changeMaxSpongeSoakBlocks(int soakableBlocks) {
        if (!isEnabled(Sponges.class))
            return soakableBlocks;

        //Vanilla uses 65 and not 64
        return maxSoakBlocks + 1;
    }

    public static int changeSpongeMaxRange(int range) {
        if (!isEnabled(Sponges.class))
            return range;

        //Vanilla uses < instead of <=
        return maxSoakRange + 1;
    }

    public static void onSpongeTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!Feature.isEnabled(Sponges.class))
            return;

        if (!level.canSeeSky(pos.above()))
            return;
        if (state.is(Blocks.SPONGE) && level.isRaining())
            level.setBlockAndUpdate(pos, Blocks.WET_SPONGE.defaultBlockState());
        else if (state.is(Blocks.WET_SPONGE) && !level.isRaining() && level.isDay()) {
            int chance = 5;
            for (Direction direction : Direction.values()) {
                if (level.getBlockState(pos.relative(direction)).is(Blocks.WET_SPONGE)) {
                    chance *= 4;
                    break;
                }
            }
            if (random.nextInt(chance) == 0)
                level.setBlockAndUpdate(pos, Blocks.SPONGE.defaultBlockState());
        }
    }
}
