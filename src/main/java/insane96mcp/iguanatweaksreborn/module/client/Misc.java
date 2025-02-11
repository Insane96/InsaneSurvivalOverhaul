package insane96mcp.iguanatweaksreborn.module.client;

import insane96mcp.iguanatweaksreborn.module.ClientModules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Misc", description = "Misc client side changes")
@LoadFeature(module = ClientModules.Ids.CLIENT)
public class Misc extends Feature {
    @Config
    @Label(name = "No tilting with non-directional damage types", description = "If true, camera will not tilt when taking magic, wither, on fire, cramming, drowning and thorns damage.")
    public static Boolean noTiltingWithNonDirectionalDamageTypes = true;

    @Config
    @Label(name = "Red block outline with wrong tool", description = "If true, the outline around blocks will be red if the tool in hand will make drops not ... drop.")
    public static Boolean redBlockOutlineWithWrongTool = true;

    @Config(min = 0)
    @Label(name = "Floaty hotbar", description = "Moves the hotbar this amount of pixels up (like bedrock edition). Other mods' GUI elements should work flawlessly if using the correct Forge GUI fields")
    public static Integer floatyHotbar = 2;
    @Config
    @Label(name = "Fix mounts GUI", description = "If true, hunger will be rendered event when the player is in riding an entity. Also hides the jump bar unless jumping (showing the XP bar)")
    public static Boolean fixMountsGui = true;

    public Misc(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static boolean shouldDisableTiltingWithNonDirectionalDamageTypes() {
        return isEnabled(Misc.class) && noTiltingWithNonDirectionalDamageTypes;
    }

    public static boolean fixMountsGui() {
        return isEnabled(Misc.class) && fixMountsGui;
    }

    public static float getRedOutlineAmount(float original) {
        if (!isEnabled(Misc.class) || !redBlockOutlineWithWrongTool)
            return original;
        return 0.35f;
    }

    //Render before Regenerating absorption
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void riseLeftAndRightHeight(final RenderGuiOverlayEvent.Pre event) {
        if (!shouldRaiseHotbar())
            return;

        if (event.getOverlay().equals(VanillaGuiOverlay.VIGNETTE.type())) {
            ((ForgeGui) Minecraft.getInstance().gui).rightHeight += floatyHotbar;
            ((ForgeGui) Minecraft.getInstance().gui).leftHeight += floatyHotbar;
        }
    }

    public static boolean shouldRaiseHotbar() {
        return Feature.isEnabled(Misc.class) && floatyHotbar > 0;
    }
}
