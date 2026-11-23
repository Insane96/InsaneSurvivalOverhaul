package insane96mcp.insanesurvivaloverhaul.module.mining.miningcharge;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.MINING, description = "Adds the Mining Charge, a directional charge that, after a short fuse, digs a tunnel in the direction it's facing. All the blocks it destroys are dropped and entities in the tunnel are hurt.")
public class MiningCharge extends Feature {
	public static final SimpleBlockWithItem BLOCK = SimpleBlockWithItem.register("mining_charge", () -> new MiningChargeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TNT)));

	public static final DeferredHolder<EntityType<?>, EntityType<PrimedMiningCharge>> PRIMED_MINING_CHARGE = ISORegistries.ENTITY_TYPES.register("mining_charge", () -> EntityType.Builder.<PrimedMiningCharge>of(PrimedMiningCharge::new, MobCategory.MISC)
			.fireImmune()
			.sized(0.75f, 0.75f)
			.eyeHeight(0.15f)
			.clientTrackingRange(10)
			.updateInterval(10)
			.build("mining_charge"));

	public static final DeferredHolder<SoundEvent, SoundEvent> PRIMED_SOUND = ISORegistries.SOUND_EVENTS.register("mining_charge_primed", () -> SoundEvent.createVariableRangeEvent(InsaneSO.id("mining_charge_primed")));
	public static final DeferredHolder<SoundEvent, SoundEvent> EXPLODE_SOUND = ISORegistries.SOUND_EVENTS.register("mining_charge_explode", () -> SoundEvent.createVariableRangeEvent(InsaneSO.id("mining_charge_explode")));

	@Config(min = 1, max = 64, description = "Length of the tunnel dug by the Mining Charge, starting from the block in front of it.")
	public static Integer tunnelLength = 8;
	@Config(min = 0, max = 8, description = "Radius of the tunnel dug by the Mining Charge. 0 is a 1x1 tunnel, 1 is 3x3, 2 is 5x5, etc.")
	public static Integer tunnelRadius = 1;
	@Config(min = 0, description = "Only blocks with an explosion resistance lower than this are destroyed by the Mining Charge. Stone and Deepslate have 6, Obsidian 1200.")
	public static Double maxBlockResistance = 10d;
	@Config(min = 0, description = "Damage dealt to entities in the tunnel dug by the Mining Charge.")
	public static Double entityDamage = 10d;
}
