package insane96mcp.insanesurvivaloverhaul.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import insane96mcp.insanesurvivaloverhaul.module.mining.anvilcrafting.AnvilRecipeReloadListener;
import net.minecraft.world.item.Items;

@EmiEntrypoint
public class ISOEmiPlugin implements EmiPlugin {
	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(EmiIsoAnvilRecipe.CATEGORY);
		registry.addWorkstation(EmiIsoAnvilRecipe.CATEGORY, EmiStack.of(Items.ANVIL));
		registry.addWorkstation(EmiIsoAnvilRecipe.CATEGORY, EmiStack.of(Items.CHIPPED_ANVIL));
		registry.addWorkstation(EmiIsoAnvilRecipe.CATEGORY, EmiStack.of(Items.DAMAGED_ANVIL));

		if (AnvilRecipeReloadListener.RECIPES != null) {
			for (var recipe : AnvilRecipeReloadListener.RECIPES) {
				registry.addRecipe(new EmiIsoAnvilRecipe(recipe));
			}
		}
	}
}
