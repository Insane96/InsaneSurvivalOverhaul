package insane96mcp.insanesurvivaloverhaul.module.mining.recycling;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.MINING, description = "Allows recycling damaged armor, tools and weapons in the Blast Furnace, giving back raw material (nuggets or gems) proportional to the item's remaining durability.")
public class Recycling extends Feature {
	@Config(description = "Minimum amount of material given back by a single recycled item, even if it's almost completely worn out.")
	public static Integer minimumOutput = 1;
	@Config(description = "Multiplier applied to the durability-based output amount. Lower it below 1 to make recycling always lossy.")
	public static Double outputMultiplier = 1.0;
	@Config(description = "If true, enchanted items can be recycled. If false, only non-enchanted items can be recycled. If Rune Enchanting is installed, runed items count as enchanted.")
	public static Boolean allowEnchantedItems = false;

	public static final DeferredHolder<RecipeSerializer<?>, SimpleCookingSerializer<RecyclingRecipe>> SERIALIZER =
			ISORegistries.RECIPE_SERIALIZERS.register("recycling", () -> new SimpleCookingSerializer<>(RecyclingRecipe::new, 100));
}
