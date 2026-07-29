package insane96mcp.insanesurvivaloverhaul.module.client;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.GuiAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOClientModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@LoadFeature(module = ISOClientModules.CLIENT)
public class Misc extends Feature {

    @Config(description = "If true, the outline around blocks will be red if the tool in hand will make drops not ... drop.")
    public static Boolean redBlockOutlineWithWrongTool = true;

    @Config(description = "If true, camera will not tilt when taking magic, wither, on fire, cramming, drowning and thorns damage.")
    public static Boolean noTiltingWithNonDirectionalDamageTypes = true;

    @Config(min = 0, description = "Moves the hotbar this amount of pixels up (like bedrock edition). Other mods' GUI elements should work flawlessly if registered correctly and using the correct fields")
    public static Integer floatyHotbar = 2;
    @Config(description = "Vanilla has cut the selected-slot highlight texture for ... reasons. This fixes the size of the slot selected highlight and enables a resource pack that fixes the texture.")
    public static Boolean slotSelectionResourcePack = true;

    @Config(description = "If true, hunger will be rendered even when the player is in riding an entity. Also hides the jump bar unless jumping (showing the XP bar)")
    public static Boolean fixMountsGui = true;

    @Config(description = "If true, health will not shake if player's max health is below 4")
    public static Boolean preventShakeBasedOffMaxHealth = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addClientPack("fix_selected_slot_highlight", "Insane's Survival Overhaul Fix Selected Slot Highlight", () -> this.isEnabled() && slotSelectionResourcePack);
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
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void riseLeftAndRightHeight(final RenderGuiEvent.Pre event) {
        if (!shouldRaiseHotbar())
            return;

        Minecraft.getInstance().gui.leftHeight += floatyHotbar;
        Minecraft.getInstance().gui.rightHeight += floatyHotbar;
    }

    public static boolean shouldRaiseHotbar() {
        return Feature.isEnabled(Misc.class) && floatyHotbar > 0;
    }

    /**
     * Replaces the vanilla experience bar layer to also show XP while riding a mount
     * that has no active jump charge, so the XP bar is visible when the jump meter is hidden.
     * Runs at NORMAL priority so mods that cancel this layer at HIGHER priority (e.g. RuneEnchanting
     * disabling XP entirely) are respected automatically, since a cancelled event isn't delivered here.
     */
    @SubscribeEvent
    public void showXpBarWhileMounted(final RenderGuiLayerEvent.Pre event) {
        if (!fixMountsGui() || !event.getName().equals(VanillaGuiLayers.EXPERIENCE_BAR))
            return;

        event.setCanceled(true);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null
                && (player.jumpableVehicle() == null || player.getJumpRidingScale() == 0)
                && Minecraft.getInstance().gameMode.hasExperience()) {
            int x = event.getGuiGraphics().guiWidth() / 2 - 91;
            ((GuiAccessor) Minecraft.getInstance().gui).callRenderExperienceBar(event.getGuiGraphics(), x);
        }
    }

    public static boolean shouldPreventHealthShake() {
        return Feature.isEnabled(Misc.class) && preventShakeBasedOffMaxHealth;
    }
}
