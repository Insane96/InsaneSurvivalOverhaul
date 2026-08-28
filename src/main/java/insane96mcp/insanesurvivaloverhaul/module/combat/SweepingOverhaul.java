package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

@LoadFeature(module = ISOModules.COMBAT, description = "Rework Sweeping attack. Sweeping is no longer on swords, instead it's on hoes. Also, the sweeping attack deals full damage and the range is increased. A data pack is enabled that removes the sweeping enchantment.")
public class SweepingOverhaul extends Feature {

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("sweeping_overhaul", "Insane's Survival Overhaul Sweeping Overhaul", () -> this.isEnabled() && !Packs.disableAllDataPacks);
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        if (this.isEnabled()) {
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
        if (!this.isEnabled())
            return;

        event.setDisableSweep(false);
    }
}
