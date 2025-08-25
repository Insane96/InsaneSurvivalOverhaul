package insane96mcp.iguanatweaksreborn.module.mobs;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.integration.EnhancedAIIntegration;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.Packs;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@LoadFeature(module = Modules.Ids.MOBS, description = "Rebalances monsters using a data pack and Mobs Properties Randomness. If Enhanced AI is installed 'Follow Range Override' and 'XRay Range Override' will be disabled. If Pehkui is installed mobs will also have different sizes, like varying zombies and smaller spiders.", enabledByDefault = false)
public class StatsBuffs extends Feature {

    public StatsBuffs(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("mobs_buffs", "Insane's Survival Overhaul Mobs Stats Buffs", () -> this.isEnabled() && !Packs.disableAllDataPacks);
    }

    @Override
    public void postReadConfig(ModConfigEvent event) {
        super.readConfig(event);
        if (ModList.get().isLoaded("enhancedai") && this.isEnabled())
            EnhancedAIIntegration.setupStatsBuffs(event);
    }
}
