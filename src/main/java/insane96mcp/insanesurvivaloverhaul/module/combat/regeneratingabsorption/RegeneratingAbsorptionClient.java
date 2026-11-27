package insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption;

import com.mojang.blaze3d.systems.RenderSystem;
import insane96mcp.insanelib.core.feature.ConfigType;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.util.ClientUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@ConfigType(ModConfig.Type.CLIENT)
@LoadFeature(module = ISOModules.COMBAT, canBeDisabled = false)
public class RegeneratingAbsorptionClient extends Feature {
    public static final ResourceLocation GUI_ICONS = InsaneSO.id("textures/gui/sprites/hud/regenerating_absorption.png");

    private static final BarState PLAYER_BAR = new BarState();
    private static final BarState VEHICLE_BAR = new BarState();

    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        Minecraft mc = Minecraft.getInstance();
        event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, InsaneSO.id("regenerating_absorption"), (guiGraphics, partialTicks) -> {
            if (Feature.isEnabled(RegeneratingAbsorption.class) && mc.player != null && mc.gameMode != null && mc.gameMode.canHurtPlayer() && !mc.options.hideGui)
                renderAbsorption(mc.gui, guiGraphics, mc.player, PLAYER_BAR, false);
        });
        // Like vanilla's vehicle health, shown regardless of the game mode
        event.registerAbove(VanillaGuiLayers.VEHICLE_HEALTH, InsaneSO.id("vehicle_regenerating_absorption"), (guiGraphics, partialTicks) -> {
            if (Feature.isEnabled(RegeneratingAbsorption.class) && mc.player != null && !mc.options.hideGui
                    && mc.player.getVehicle() instanceof LivingEntity vehicle && vehicle.showVehicleHealth())
                renderAbsorption(mc.gui, guiGraphics, vehicle, VEHICLE_BAR, true);
        });
    }

    /**
     * Renders the regenerating absorption bar of the entity above the bars already rendered on the same side of the hotbar.
     *
     * @param rightSide if true the bar is rendered on the right side of the hotbar and filled from right to left, like the vehicle health, otherwise on the left side like the player health.
     */
    protected static void renderAbsorption(Gui gui, GuiGraphics guiGraphics, LivingEntity entity, BarState state, boolean rightSide) {
        Minecraft mc = Minecraft.getInstance();
        mc.getProfiler().push("regen_absorption");

        RenderSystem.enableBlend();
        int width = guiGraphics.guiWidth();
        int rowStart = rightSide ? width / 2 + 91 - 9 : width / 2 - 91;
        int step = rightSide ? -8 : 8;
        int x = rowStart;
        int y = guiGraphics.guiHeight() - (rightSide ? gui.rightHeight : gui.leftHeight);

        int absorption = Mth.ceil(RegeneratingAbsorption.getCurrentAbsorption(entity));
        // Don't blink when the bar starts showing a different entity (e.g. switching mount)
        if (state.entityId != entity.getId()) {
            state.entityId = entity.getId();
            state.lastAbsorption = absorption;
            state.displayAbsorption = absorption;
            state.blinkTime = 0;
        }
        boolean highlight = state.blinkTime > (long) gui.getGuiTicks() && (state.blinkTime - (long) gui.getGuiTicks()) / 3L % 2L == 1L;
        int v = highlight ? 9 : 0;

        if (absorption < state.lastAbsorption && entity.invulnerableTime > 0) {
            state.lastAbsorptionTime = Util.getMillis();
            state.displayAbsorption = state.lastAbsorption;
            state.blinkTime = gui.getGuiTicks() + 20;
        }
        else if (absorption > state.lastAbsorption) {
            state.displayAbsorption = absorption;
            state.blinkTime = gui.getGuiTicks() + 10;
        }

        if (Util.getMillis() - state.lastAbsorptionTime > 1000L) {
            state.lastAbsorption = absorption;
            state.displayAbsorption = absorption;
            state.lastAbsorptionTime = Util.getMillis();
        }

        state.lastAbsorption = absorption;
        int addedHeight = 0;
        for (int i = 1; i <= state.displayAbsorption; i++) {
            if (i > absorption)
                ClientUtils.setRenderColor(1, 0, 0, 1f);
            // Each icon is made of two points, drawn with the two complementary halves of the texture
            int u = i % 2 == 0 ? 0 : 9;
            guiGraphics.blit(GUI_ICONS, x, y, u, v, 9, 9, 18, 18);
            if (i % 20 == 0 && i != state.displayAbsorption) {
                x = rowStart;
                y -= 10;
                addedHeight += 10;
            }
            else if (i % 2 == 0)
                x += step;
            if (i > absorption)
                ClientUtils.resetRenderColor();
        }
        if (state.displayAbsorption > 0)
            addedHeight += 10;
        if (rightSide)
            gui.rightHeight += addedHeight;
        else
            gui.leftHeight += addedHeight;

        RenderSystem.disableBlend();
        mc.getProfiler().pop();
    }

    /**
     * Rendering state of an absorption bar, used to make it blink and show the lost absorption
     */
    protected static class BarState {
        int entityId = -1;
        int lastAbsorption = 0;
        long lastAbsorptionTime = 0;
        long blinkTime = 0;
        int displayAbsorption = 0;
    }
}
