package insane96mcp.iguanatweaksreborn.module.combat;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraftforge.fml.ModList;

@Label(name = "Shields+ Integration")
@LoadFeature(module = Modules.Ids.COMBAT)
public class ShieldsPlus extends Feature {
    @Config
    @Label(name = "Shields+ Compat DataPack", description = "Rebalances shields to fit the other mods' changes")
    public static Boolean dataPack = true;

    public ShieldsPlus(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        InsaneSurvivalOverhaul.addServerPack("shieldsplus_integration", "Insane's Survival Overhaul Shields+ Integration", () -> super.isEnabled() && !DataPacks.disableAllDataPacks && dataPack && ModList.get().isLoaded("shieldsplus"));
    }
}