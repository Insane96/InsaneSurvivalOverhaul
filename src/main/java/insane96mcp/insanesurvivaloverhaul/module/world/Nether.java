package insane96mcp.insanesurvivaloverhaul.module.world;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOBlockTagsProvider;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.PortalShapeAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.apache.commons.lang3.mutable.MutableInt;

@LoadFeature(module = ISOModules.WORLD)
public class Nether extends Feature {
    public static final MutableComponent REQUIRES_CORNERS_MESSAGE = Component.translatable(InsaneSO.lang("requires_corners"));
    public static final MutableComponent NO_NETHER_MESSAGE = Component.translatable(InsaneSO.lang("no_nether"));
    public static final TagKey<Block> PORTAL_CORNERS = ISOBlockTagsProvider.create("portal_corners");

    @Config(name = "Disable Nether Roof and reduce block ratio", description = "Enables a data pack that makes the nether 128 blocks high instead of 256, effectively disabling the \"Nether Roof\" and reduces the 8 block ratio between nether and other dimensions to 2. This will break if another mod changes the nether dimension")
    public static Boolean netherTweaks = true;
    @Config(description = "If true, lava pockets in the nether are removed. If quark is installed, this is disabled")
    public static Boolean removeLavaPockets = true;
    @Config(description = "If true you cannot lit nether portals")
    public static Boolean disableNether = false;
    @Config(description = "The portal requires Crying Obsidian in the corners to turn it on (in the overworld). The block tag 'insanesurvivaloverhaul:portal_corners' can be used to change the required blocks for the corners")
    public static Boolean portalRequiresCryingObsidian = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("nether_tweaks", "Insane's Survival Overhaul Nether Tweaks", () -> this.isEnabled() && !Packs.disableAllDataPacks && netherTweaks);
    }

    public static boolean shouldDisableLavaPockets(SpringConfiguration configuration) {
        return isEnabled(Nether.class) && removeLavaPockets && !configuration.requiresBlockBelow && configuration.state.is(FluidTags.LAVA);
    }

    @SubscribeEvent
    public void onPortalTryToActivate(BlockEvent.PortalSpawnEvent event) {
        PortalShapeAccessor portalSize = (PortalShapeAccessor) event.getPortalSize();
        if (!this.isEnabled()
                || portalSize.getBottomLeft() == null)
            return;

        AABB aabb = AABB.ofSize(event.getPos().getCenter(), 8, 8, 8);
        if (disableNether) {
            Level level = (Level) event.getLevel();
            for (Player player : level.players()) {
                if (aabb.contains(player.getX(), player.getY(), player.getZ()))
                    player.sendSystemMessage(NO_NETHER_MESSAGE);
            }
            event.setCanceled(true);
        }
        else if (portalRequiresCryingObsidian) {
            Level level = (Level) event.getLevel();
            if (!level.dimension().equals(Level.OVERWORLD))
                return;

            MutableInt cryingObsidian = new MutableInt();
            BlockPos.betweenClosed(portalSize.getBottomLeft().below().relative(portalSize.getRightDir().getOpposite(), 1),
                            portalSize.getBottomLeft().relative(Direction.UP, portalSize.getHeight()).relative(portalSize.getRightDir(), portalSize.getWidth()))
                    .forEach((pos) -> {
                        if (level.getBlockState(pos).is(PORTAL_CORNERS))
                            cryingObsidian.increment();
                    });
            if (cryingObsidian.getValue() < 4) {
                for (Player player : level.players()) {
                    if (aabb.contains(player.getX(), player.getY(), player.getZ())) {
                        player.sendSystemMessage(REQUIRES_CORNERS_MESSAGE);
                    }
                }
                event.setCanceled(true);
            }
        }
    }
}
