package insane96mcp.iguanatweaksreborn.module.sleeprespawn;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.minecraftforge.registries.RegistryObject;

import static insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul.MOD_ID;

@Label(name = "Cloth", description = "Zombies drop cloth instead of Rotten Flesh. Cloth is used to make beds and various other stuff.")
@LoadFeature(module = Modules.Ids.SLEEP_RESPAWN)
public class Cloth extends Feature {

	public static final RegistryObject<Item> CLOTH = ISORegistries.ITEMS.register("cloth", () -> new Item(new Item.Properties()));

	@Config
	@Label(name = "Data Pack", description = """
			Enables a Data Pack that integrated Cloth:
			* Makes zombies drop cloth instead of rotten flesh
			* Beds require Cloth to be crafted
			* Bundles can be made with Cloth
			* Chainmail armor is made craftable with Cloth and chains
			* Adds advancements for this feature and moves vanilla beds advancements to cloth advancement""")
	public static Boolean dataPack = true;

	public Cloth(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "cloth", Component.literal("Insane's Survival Overhaul Cloth"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && dataPack));
	}

	@SubscribeEvent
	public void remapFromITE(MissingMappingsEvent event) {
		event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
				.filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:cloth"))
				.forEach(mapping -> mapping.remap(CLOTH.get()));
	}
}