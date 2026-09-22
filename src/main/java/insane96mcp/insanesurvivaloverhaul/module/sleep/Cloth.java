package insane96mcp.insanesurvivaloverhaul.module.sleep;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.SLEEP, description = """
		Adds Cloth and enables a Data Pack that integrates it:
		* Makes zombies drop cloth instead of rotten flesh
		* Beds require Cloth to be crafted
		* Bundles can be made with Cloth
		* Chainmail armor is made craftable with Cloth and chains
		* Adds advancements for this feature and moves vanilla beds advancements to cloth advancement""")
public class Cloth extends Feature {

	public static final DeferredHolder<Item, Item> ITEM = ISORegistries.ITEMS.register("cloth", () -> new Item(new Item.Properties()));

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("cloth", "Insane's Survival Overhaul Cloth", () -> this.isEnabled() && !Packs.disableAllDataPacks);
	}
}