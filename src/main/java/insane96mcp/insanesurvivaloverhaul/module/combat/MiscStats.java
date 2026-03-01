package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

@LoadFeature(module = ISOModules.COMBAT)
public class MiscStats extends Feature {
    /*@Config(description = "Vanilla tooltips on gear don't sum up multiple modifiers (e.g. a sword would have \"4 Attack Damage\" and \"-2 Attack Damage\" instead of \"2 Attack Damage\". This might break other mods messing with these Tooltips (e.g. Quark's improved tooltips)")
    public static Boolean fixTooltips = true;*/
    @Config(description = "If enabled, tools will not take 2 damage when used to hurt entities")
    public static Boolean oneDamageForToolAttacking = true;
    @Config(description = "Rework Sweeping attack. Sweeping is no longer on swords, instead it's on hoes. Also, the sweeping attack deals full damage and the range is increased. A data pack is enabled that removes the sweeping enchantment.")
    public static Boolean sweepingOverhaul = true;

    /*@Config(description = "Enables a data pack that reworks armor, weapons and tools.")
    public static Boolean combatReworkDataPack = true;*/

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        //InsaneSO.addServerPack("combat_rework", "Insane's Survival Overhaul Combat Rework", () -> this.isEnabled() && !Packs.disableAllDataPacks && combatReworkDataPack);
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        if (sweepingOverhaul) {
            ItemAbilities.DEFAULT_SWORD_ACTIONS.remove(ItemAbilities.SWORD_SWEEP);
            ItemAbilities.DEFAULT_HOE_ACTIONS.add(ItemAbilities.SWORD_SWEEP);
        }
        else {
            ItemAbilities.DEFAULT_SWORD_ACTIONS.add(ItemAbilities.SWORD_SWEEP);
            ItemAbilities.DEFAULT_HOE_ACTIONS.remove(ItemAbilities.SWORD_SWEEP);
        }
    }

    @SubscribeEvent
    public void onCritDisableSweep(CriticalHitEvent event) {
        if (!this.isEnabled()
                || !sweepingOverhaul)
            return;

        event.setDisableSweep(false);
    }
}