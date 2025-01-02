package insane96mcp.iguanatweaksreborn.module.movement.minecarts;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.iguanatweaksreborn.setup.registry.SimpleBlockWithItem;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import static insane96mcp.iguanatweaksreborn.IguanaTweaksReborn.MOD_ID;

@Label(name = "Minecarts")
@LoadFeature(module = Modules.Ids.MOVEMENT, canBeDisabled = false)
public class Minecarts extends Feature {

	public static final SimpleBlockWithItem GOLDEN_POWERED_RAIL = SimpleBlockWithItem.register("golden_powered_rail", () -> new ITEPoweredRail(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL), 1f, 0.18f));
	public static final SimpleBlockWithItem COPPER_POWERED_RAIL = SimpleBlockWithItem.register("copper_powered_rail", () -> new ITEPoweredRail(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL).sound(SoundType.COPPER), 0.4f, 0.05f));

	@Config
	@Label(name = "Data Pack", description = "If true, enables a data pack that makes rails cheaper and adds recipes for new rail. Also adds a global loot modifier that replaces vanilla rails with golden powered rails")
	public static Boolean dataPack = true;

	public Minecarts(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "better_rails", Component.literal("Insane's Survival Tweaks Better Rails"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && dataPack));
	}

	@SubscribeEvent
	public void remapFromITE(MissingMappingsEvent event) {
		event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
				.filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:golden_powered_rail"))
				.forEach(mapping -> mapping.remap(GOLDEN_POWERED_RAIL.item().get()));
		event.getMappings(ForgeRegistries.Keys.BLOCKS, MOD_ID).stream()
				.filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:golden_powered_rail"))
				.forEach(mapping -> mapping.remap(GOLDEN_POWERED_RAIL.block().get()));
		event.getMappings(ForgeRegistries.Keys.ITEMS, MOD_ID).stream()
				.filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:copper_powered_rail"))
				.forEach(mapping -> mapping.remap(COPPER_POWERED_RAIL.item().get()));
		event.getMappings(ForgeRegistries.Keys.BLOCKS, MOD_ID).stream()
				.filter(mapping -> mapping.getKey().getNamespace().contains("iguanatweaksexpanded:copper_powered_rail"))
				.forEach(mapping -> mapping.remap(COPPER_POWERED_RAIL.block().get()));
	}
}