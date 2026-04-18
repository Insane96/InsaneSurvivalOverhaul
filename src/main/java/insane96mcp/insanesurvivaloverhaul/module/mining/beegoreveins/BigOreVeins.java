package insane96mcp.insanesurvivaloverhaul.module.mining.beegoreveins;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.world.level.block.state.BlockBehaviour;

@LoadFeature(module = ISOModules.MINING, description = "Enables a Data Pack that generates large ore veins underground. On the surface you can find ore rocks indicating a huge vein below.")
public class BigOreVeins extends Feature {
    public static final SimpleBlockWithItem IRON_ORE_ROCK = SimpleBlockWithItem.register("iron_ore_rock", () -> new GroundRockBlock(BlockBehaviour.Properties.of().strength(0.5F, 2.0F).offsetType(BlockBehaviour.OffsetType.XZ).dynamicShape()));
    public static final SimpleBlockWithItem GOLD_ORE_ROCK = SimpleBlockWithItem.register("gold_ore_rock", () -> new GroundRockBlock(BlockBehaviour.Properties.of().strength(0.5F, 2.0F).offsetType(BlockBehaviour.OffsetType.XZ).dynamicShape()));
    public static final SimpleBlockWithItem COPPER_ORE_ROCK = SimpleBlockWithItem.register("copper_ore_rock", () -> new GroundRockBlock(BlockBehaviour.Properties.of().strength(0.5F, 2.0F).offsetType(BlockBehaviour.OffsetType.XZ).dynamicShape()));

    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("big_ore_veins", "Insane's Survival Overhaul Big Ore Veins", () -> this.isEnabled() && !Packs.disableAllDataPacks);
    }
}
