package insane96mcp.insanesurvivaloverhaul.module.combat.regeneratingabsorption;

import com.mojang.blaze3d.systems.RenderSystem;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.util.ClientUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class RegeneratingAbsorptionClient {
    public static final ResourceLocation GUI_ICONS = InsaneSO.location("textures/gui/sprites/hud/regenerating_absorption.png");

    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        ResourceLocation aboveOverlay = VanillaGuiLayers.PLAYER_HEALTH;
        if (ModList.get().isLoaded("stamina"))
            aboveOverlay = ResourceLocation.parse("stamina:stamina_overlay");
        if (RegeneratingAbsorption.renderOnTheRight) {
            //if (ModList.get().isLoaded("nohunger") && NoHungerIntegration.doesRenderArmorAtHunger())
            //    aboveOverlay = InsaneSO.location("armor");
            //else
                aboveOverlay = VanillaGuiLayers.FOOD_LEVEL;
        }
        Minecraft mc = Minecraft.getInstance();
        Gui gui = mc.gui;
        event.registerAbove(aboveOverlay, InsaneSO.location("regenerating_absorption"), (guiGraphics, partialTicks) -> {
            if (Feature.isEnabled(RegeneratingAbsorption.class) && mc.gameMode != null && mc.gameMode.canHurtPlayer())
                renderAbsorption(gui, guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight());
        });
    }

    static int lastAbsorption = 0;
    static long lastAbsorptionTime = 0;
    static long absorptionBlinkTime = 0;
    static int displayAbsorption = 0;

    protected static void renderAbsorption(Gui gui, GuiGraphics guiGraphics, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;
        mc.getProfiler().push("regen_absorption");

        RenderSystem.enableBlend();
        int left = width / 2 + (!RegeneratingAbsorption.renderOnTheRight ? -91 : 82);
        int top = height - (!RegeneratingAbsorption.renderOnTheRight ? gui.leftHeight : gui.rightHeight);

        int absorption = Mth.ceil(ModNBTData.get(mc.player, RegeneratingAbsorption.REGEN_ABSORPTION_TAG, Float.class));
        boolean highlight = absorptionBlinkTime > (long) gui.getGuiTicks() && (absorptionBlinkTime - (long) gui.getGuiTicks()) / 3L % 2L == 1L;
        int v = highlight ? 9 : 0;

        if (absorption < lastAbsorption && player.invulnerableTime > 0)
        {
            lastAbsorptionTime = Util.getMillis();
            displayAbsorption = lastAbsorption;
            absorptionBlinkTime = gui.getGuiTicks() + 20;
        }
        else if (absorption > lastAbsorption)
        {
            //lastAbsorptionTime = Util.getMillis();
            displayAbsorption = absorption;
            absorptionBlinkTime = gui.getGuiTicks() + 10;
        }

        if (Util.getMillis() - lastAbsorptionTime > 1000L)
        {
            lastAbsorption = absorption;
            displayAbsorption = absorption;
            lastAbsorptionTime = Util.getMillis();
        }
        //player.displayClientMessage(Component.literal("Util.getMillis(): %s, lastAbsorption: %s, absorption: %s, absorptionBlinkTime: %s, displayAbsorption: %s".formatted(Util.getMillis() - lastAbsorptionTime, lastAbsorption, absorption, absorptionBlinkTime, displayAbsorption)), true);

        lastAbsorption = absorption;
        for (int i = 1; i <= displayAbsorption; i++)
        {
            if (i > absorption)
                ClientUtils.setRenderColor(1, 0, 0, 1f);
            //ClientUtils.blitVericallyMirrored(GUI_ICONS, guiGraphics, left, top, 9, v, 9, 9, 18, 18);
            int u = i % 2 == 0 ? 0 : 9;
            if (!RegeneratingAbsorption.renderOnTheRight)
                guiGraphics.blit(GUI_ICONS, left, top, u, v, 9, 9, 18, 18);
            else
                ClientUtils.blitVerticallyMirrored(GUI_ICONS, guiGraphics,left, top, u, v, 9, 9, 18, 18);
            if (i % 20 == 0 && i != displayAbsorption) {
                left = width / 2 + (!RegeneratingAbsorption.renderOnTheRight ? -91 : 82);
                top -= 10;
                if (!RegeneratingAbsorption.renderOnTheRight)
                    gui.leftHeight += 10;
                else
                    gui.rightHeight += 10;
            }
            else if (i % 2 == 0)
                left += RegeneratingAbsorption.renderOnTheRight ? -8 : 8;
            if (i > absorption)
                ClientUtils.resetRenderColor();
        }
        if (displayAbsorption > 0)
            if (!RegeneratingAbsorption.renderOnTheRight)
                gui.leftHeight += 10;
            else
                gui.rightHeight += 10;

        RenderSystem.disableBlend();
        mc.getProfiler().pop();
    }
}
