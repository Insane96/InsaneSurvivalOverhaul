package insane96mcp.iguanatweaksreborn.module.world;

import insane96mcp.iguanatweaksreborn.IguanaTweaksReborn;
import insane96mcp.iguanatweaksreborn.data.generator.ITRBlockTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.mutable.MutableInt;

@Label(name = "Nether")
@LoadFeature(module = Modules.Ids.WORLD)
public class Nether extends Feature {
    public static final String REQUIRES_CORNERS_LANG = IguanaTweaksReborn.MOD_ID + ".requires_corners";
    public static final TagKey<Block> PORTAL_CORNERS = ITRBlockTagsProvider.create("portal_corners");

    @Config
    @Label(name = "Disable Nether Roof and reduce block ratio", description = "Enables a data pack that makes the nether 128 blocks high instead of 256, effectively disabling the \"Nether Roof\" and reduces the 8 block ratio between nether and other dimensions to 4.")
    public static Boolean netherTweaks = true;
    @Config
    @Label(name = "Remove Lava Pockets", description = "If true, lava pockets in the nether are removed. If quark is installed, this is disabled")
    public static Boolean removeLavaPockets = true;
    @Config
    @Label(name = "Disable Nether", description = "If true you cannot lit nether portals")
    public static Boolean disableNether = false;
    @Config
    @Label(name = "Portal wait time", description = "How much time inside the portal to pass it. Vanilla is 80 ticks")
    public static Integer portalWaitTime = 50;
    @Config
    @Label(name = "Portal requires Crying Obsidian", description = "The portal requires Crying Obsidian in the corners to turn it on (in the overworld). The block tag 'iguanatweaksreborn:portal_corners' can be used to change the required blocks for the corners")
    public static Boolean portalRequiresCryingObsidian = true;

    public Nether(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "nether_tweaks", Component.literal("Insane's Survival Tweaks Nether Tweaks"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && netherTweaks));
    }

    public static boolean shouldDisableLavaPockets(SpringConfiguration configuration) {
        return !ModList.get().isLoaded("quark") && isEnabled(Nether.class) && removeLavaPockets && !configuration.requiresBlockBelow && configuration.state.is(FluidTags.LAVA);
    }

    @SubscribeEvent
    public void onPortalTryToActivate(BlockEvent.PortalSpawnEvent event) {
        if (!this.isEnabled()
                || event.getPortalSize().bottomLeft == null)
            return;

        AABB aabb = AABB.ofSize(event.getPos().getCenter(), 8, 8, 8);
        if (disableNether) {
            Level level = (Level) event.getLevel();
            for (Player player : level.players()) {
                if (aabb.contains(player.getX(), player.getY(), player.getZ())) {
                    player.sendSystemMessage(Component.translatable("no_nether"));
                }
            }
            event.setCanceled(true);
        }
        else if (portalRequiresCryingObsidian) {
            Level level = (Level) event.getLevel();
            if (!level.dimension().equals(Level.OVERWORLD))
                return;

            MutableInt cryingObsidians = new MutableInt();
            BlockPos.betweenClosed(event.getPortalSize().bottomLeft.below().relative(event.getPortalSize().rightDir.getOpposite(), 1),
                            event.getPortalSize().bottomLeft.relative(Direction.UP, event.getPortalSize().height).relative(event.getPortalSize().rightDir, event.getPortalSize().width))
                    .forEach((pos) -> {
                        if (level.getBlockState(pos).is(PORTAL_CORNERS))
                            cryingObsidians.increment();
                    });
            if (cryingObsidians.getValue() < 4) {
                for (Player player : level.players()) {
                    if (aabb.contains(player.getX(), player.getY(), player.getZ())) {
                        player.sendSystemMessage(Component.translatable(REQUIRES_CORNERS_LANG));
                    }
                }
                event.setCanceled(true);
            }
        }
    }
}
