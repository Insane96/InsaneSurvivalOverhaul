package insane96mcp.iguanatweaksreborn.module.combat;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.Packs;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraftforge.fml.ModList;

@LoadFeature(module = Modules.Ids.COMBAT, name = "Shields+ Integration")
public class ShieldsPlus extends Feature {
    @Config(name = "Compat DataPack", description = "Rebalances shields durabilities to fit the mods' changes")
    public static Boolean dataPack = true;

    public ShieldsPlus(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("shieldsplus_integration", "Insane's Survival Overhaul Shields+ Integration", () -> super.isEnabled() && !Packs.disableAllDataPacks && dataPack && ModList.get().isLoaded("shieldsplus"));
    }
}