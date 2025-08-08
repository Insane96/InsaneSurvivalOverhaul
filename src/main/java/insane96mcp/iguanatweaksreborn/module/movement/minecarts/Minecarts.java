package insane96mcp.iguanatweaksreborn.module.movement.minecarts;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

@LoadFeature(module = Modules.Ids.MOVEMENT)
public class Minecarts extends Feature {

	public static final SimpleBlockWithItem GOLDEN_POWERED_RAIL = SimpleBlockWithItem.register("golden_powered_rail", () -> new ISOPoweredRail(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL), 1f, 0.18f));
	public static final SimpleBlockWithItem COPPER_POWERED_RAIL = SimpleBlockWithItem.register("copper_powered_rail", () -> new ISOPoweredRail(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL).sound(SoundType.COPPER), 0.4f, 0.05f));

    /*@Config
    public static Boolean speedUpMinecartsUnderwater = true;*/

	@Config(description = "If true, enables a data pack that makes rails cheaper and adds recipes for new rail. Also adds a global loot modifier that replaces vanilla rails with golden powered rails")
	public static Boolean dataPack = true;

	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		InsaneSO.addServerPack("better_rails", "Insane's Survival Overhaul Better Rails", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && dataPack);
	}

    /*public static boolean speedUpMinecartsUnderwater() {
        return Feature.isEnabled(Minecarts.class) && speedUpMinecartsUnderwater;
    }*/
}