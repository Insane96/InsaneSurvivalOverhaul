package insane96mcp.insanesurvivaloverhaul.module.client;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOClientModules;

@LoadFeature(module = ISOClientModules.CLIENT)
public class Misc extends Feature {
    @Config(description = "If true, camera will not tilt when taking magic, wither, on fire, cramming, drowning and thorns damage.")
    public static Boolean noTiltingWithNonDirectionalDamageTypes = true;

    @Config(description = "If true, the outline around blocks will be red if the tool in hand will make drops not ... drop.")
    public static Boolean redBlockOutlineWithWrongTool = true;

    //@Config(min = 0, description = "Moves the hotbar this amount of pixels up (like bedrock edition). Other mods' GUI elements should work flawlessly if using the correct Forge GUI fields")
    //public static Integer floatyHotbar = 2;
    //@Config(description = "If true, hunger will be rendered even when the player is in riding an entity. Also hides the jump bar unless jumping (showing the XP bar)")
    //public static Boolean fixMountsGui = true;

    //@Config(description = "If true, health will not shake if player's max health is below 4")
    //public static Boolean preventShakeBasedOffMaxHealth = true;

    public static boolean shouldDisableTiltingWithNonDirectionalDamageTypes() {
        return isEnabled(Misc.class) && noTiltingWithNonDirectionalDamageTypes;
    }

    /*public static boolean fixMountsGui() {
        return isEnabled(Misc.class) && fixMountsGui;
    }*/

    public static float getRedOutlineAmount(float original) {
        if (!isEnabled(Misc.class) || !redBlockOutlineWithWrongTool)
            return original;
        return 0.35f;
    }

    //Render before Regenerating absorption
    /*@OnlyIn(Dist.CLIENT)
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

    public static boolean shouldPreventHealthShake() {
        return Feature.isEnabled(Misc.class) && preventShakeBasedOffMaxHealth;
    }*/
}
