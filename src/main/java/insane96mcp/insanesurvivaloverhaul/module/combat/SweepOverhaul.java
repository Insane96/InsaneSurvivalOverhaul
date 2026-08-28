package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

@LoadFeature(module = ISOModules.COMBAT, description = "Rework sweep attack. Sweep is no longer on swords, instead it's on hoes. Also, the sweep attack deals full damage and the range is increased. A data pack is enabled that removes the Sweeping Edge enchantment.")
public class SweepOverhaul extends Feature {

    @Config(description = "Vanilla = false")
    public static Boolean sweepOnHoes = true;
    @Config(description = "Vanilla = true")
    public static Boolean sweepOnSwords = false;
    @Config(description = "Vanilla = false")
    public static Boolean critAllowsSweep = true;
    @Config(description = "If true, the mobs hit by the sweep attack will take full damage instead of 1. Vanilla = false")
    public static Boolean fullSweepDamage = true;
    @Config(description = "Increases the horizontal sweep hit box by this value in blocks. Vanilla = 0")
    public static Double horizontalSweepInflation = 1.5d;
    @Config(description = "Increases the vertical sweep hit box by this value in blocks. Vanilla = 0")
    public static Double verticalSweepInflation = 0.15d;
    @Config(description = "Increases the sweep range by this value in blocks. Vanilla = 0")
    public static Double sweepRangeInflation = 1d;
    @Config(description = "Vanilla = false")
    public static Boolean allowSweepWhenAirborne = true;
    @Config(description = "Vanilla = false")
    public static Boolean allowSweepWhenSprinting = true;
    @Config(description = "Vanilla = false")
    public static Boolean allowSweepWhenMovingFast = true;
    @Config(description = "If true, scales the sweep knockback strength proportionally to the attack's knockback level instead of using the hardcoded 0.4 constant (so knockback enchantment actually works). Vanilla = false")
    public static Boolean scaleKnockback = true;

    @Config(description = "Enables a data pack that removes the Sweeping Edge enchantment and (if Rune Enchanting is installed) the Sweeping Edge rune")
    public static Boolean dataPack = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("sweep_overhaul", "Insane's Survival Overhaul Sweep Overhaul", () -> this.isEnabled() && dataPack && !Packs.disableAllDataPacks);
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        if (this.isEnabled()) {
            if (sweepOnSwords)
                ItemAbilities.DEFAULT_SWORD_ACTIONS.add(ItemAbilities.SWORD_SWEEP);
            else
                ItemAbilities.DEFAULT_SWORD_ACTIONS.remove(ItemAbilities.SWORD_SWEEP);

            if (sweepOnHoes)
                ItemAbilities.DEFAULT_HOE_ACTIONS.add(ItemAbilities.SWORD_SWEEP);
            else
                ItemAbilities.DEFAULT_HOE_ACTIONS.remove(ItemAbilities.SWORD_SWEEP);

        }
        else {
            ItemAbilities.DEFAULT_SWORD_ACTIONS.add(ItemAbilities.SWORD_SWEEP);
            ItemAbilities.DEFAULT_HOE_ACTIONS.remove(ItemAbilities.SWORD_SWEEP);
        }
    }

    @SubscribeEvent
    public void onCritDisableSweep(CriticalHitEvent event) {
        if (!this.isEnabled())
            return;

        event.setDisableSweep(!critAllowsSweep);
    }
}
