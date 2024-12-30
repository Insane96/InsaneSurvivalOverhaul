package insane96mcp.iguanatweaksreborn.module.client;

import insane96mcp.iguanatweaksreborn.module.ClientModules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.client.CameraType;
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

    @Config(min = 0d, max = 1d)
    @Label(name = "World Border Transparency", description = "Multiplies the world border transparency by this value")
    public static Double worldBorderTransparency = 0.4d;

    @Config
    @Label(name = "Shorter world border", description = "If true, the world border height is reduced by 4 times.")
    public static Boolean shorterWorldBorder = true;

    @Config(min = 1d)
    @Label(name = "Cap world border height", description = "Set the max height of the world border.")
    public static Double capWorldBorderHeight = 128d;

    @Config
    @Label(name = "No tilting with non-directional damage types", description = "If true, camera will not tilt when taking magic, wither, on fire, cramming, drowning and thorns damage.")
    public static Boolean noTiltingWithNonDirectionalDamageTypes = true;

    @Config
    @Label(name = "Red block outline with wrong tool", description = "If true, the outline around blocks will be red if the tool in hand will make drops not ... drop.")
    public static Boolean redBlockOutlineWithWrongTool = true;

    @Config
    @Label(name = "Third person on death", description = "If true, when you die, you switch to third person camera.")
    public static Boolean thirdPersonOnDeath = true;
    @Config
    @Label(name = "Remove score", description = "Why is that still a thing?.")
    public static Boolean removeScore = true;

    @Config(min = 0)
    @Label(name = "Floaty hotbar", description = "Moves the hotbar this amount of pixels up (like bedrock edition). Other mods' GUI elements should work flowlessly if using the correct Forge GUI fields")
    public static Integer floatyHotbar = 2;

    public Misc(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    public static float getWorldBorderTransparencyMultiplier() {
        if (isEnabled(Misc.class))
            return worldBorderTransparency.floatValue();
        return 1f;
    }

    public static boolean shouldShortenWorldBorder() {
        return isEnabled(Misc.class) && shorterWorldBorder;
    }

    public static boolean shouldDisableTiltingWithNonDirectionalDamageTypes() {
        return isEnabled(Misc.class) && noTiltingWithNonDirectionalDamageTypes;
    }

    public static float getRedOutlineAmount(float original) {
        if (!isEnabled(Misc.class) || !redBlockOutlineWithWrongTool)
            return original;
        return 0.42f;
    }

    public static boolean dead = false;
    public static void onDeath() {
        if (!Feature.isEnabled(Misc.class)
                || !thirdPersonOnDeath)
            return;

        CameraType cameratype = Minecraft.getInstance().options.getCameraType();
        if (cameratype != CameraType.FIRST_PERSON)
            return;
        Minecraft.getInstance().options.setCameraType(CameraType.THIRD_PERSON_BACK);
        dead = true;
    }

    //Render before Regenerating absorption
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void removeExperienceBar(final RenderGuiOverlayEvent.Pre event) {
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
