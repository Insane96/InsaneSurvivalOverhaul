package insane96mcp.insanesurvivaloverhaul.module.items.explosivebarrel;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

@LoadFeature(module = ISOModules.ITEMS, description = "Adds the Explosive Barrel, a block that explodes instantly (no fuse) when powered, set on fire, hit by a burning projectile or caught in another explosion. All the blocks destroyed by its explosion are dropped.")
public class ExplosiveBarrel extends Feature {
	public static final SimpleBlockWithItem BLOCK = SimpleBlockWithItem.register("explosive_barrel", () -> new ExplosiveBarrelBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).ignitedByLava().instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD)));

	@Config(min = 0.5, description = "Explosion power of the Explosive Barrel. TNT is 4.")
	public static Double explosionPower = 4d;
}
