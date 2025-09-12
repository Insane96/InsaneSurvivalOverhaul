package insane96mcp.iguanatweaksreborn.module.items.repairkit;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.RegistryObject;

@LoadFeature(module = Modules.Ids.MINING, description = "Add repair kits, making you able to repair items in the crafting grid")
public class RepairKits extends Feature {

	public static final RegistryObject<SimpleCraftingRecipeSerializer<RepairKitRepairRecipe>> REPAIR_RECIPE_SERIALIZER = ISORegistries.RECIPE_SERIALIZERS.register("crafting_special_repairingkit", () -> new SimpleCraftingRecipeSerializer<>(RepairKitRepairRecipe::new));
	public static final RegistryObject<Item> ITEM = ISORegistries.ITEMS.register("repair_kit", () -> new RepairKitItem(new Item.Properties().stacksTo(16)));

	@Config(min = 1, description = "Default how many materials worth does a repair kit repair. Each repair kit can have it's own ratio")
	public static Integer repairKitMaterialRatio = 1;
	@Config(min = 0, max = 1, name = "Max repair", description = "Default maximum repair percentage of an item. Each repair kit can have it's own max repair")
	public static Double maxRepair = 0.75d;
}