package insane96mcp.insanesurvivaloverhaul.module.world.fallingblocksdamage;

import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOBlockTagsProvider;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.FallingBlockEntityAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = ISOModules.WORLD, description = "All falling blocks (sand, gravel, concrete powder, etc.) hurt the entities they fall on. Damage per block can be changed in falling_blocks_damage.json in this feature's folder. Blocks in the insanesurvivaloverhaul:falling_block_damage_blacklist block tag are not changed. Falling blocks that already hurt entities (e.g. Anvils and Pointed Dripstone) are not changed.")
public class FallingBlocksDamage extends JsonFeature {
	public static final TagKey<Block> BLACKLIST = ISOBlockTagsProvider.create("falling_block_damage_blacklist");

	public static final ArrayList<FallingBlockDamage> FALLING_BLOCKS_DAMAGE_DEFAULT = new ArrayList<>(List.of(

	));
	public static final ArrayList<FallingBlockDamage> fallingBlocksDamage = new ArrayList<>();

	@Config(min = 0d, max = 1024d, description = "Damage dealt per block fallen (the first block fallen deals no damage) by falling blocks not in falling_blocks_damage.json. Anvils deal 2.")
	public static Double damagePerDistance = 1d;
	@Config(min = 0, max = 1024, description = "Max damage dealt by falling blocks not in falling_blocks_damage.json. Anvils deal up to 40.")
	public static Integer maxDamage = 20;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		getJsonConfigs().add(new JsonConfig<>("falling_blocks_damage.json", fallingBlocksDamage, FALLING_BLOCKS_DAMAGE_DEFAULT, FallingBlockDamage.LIST_TYPE).withRegistryFor(Registries.BLOCK));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
	}

	/**
	 * Fired from FallingBlockEntity#fall's addFreshEntity, before Anvils and Pointed Dripstone set their own damage, so they can still override it.
	 */
	@SubscribeEvent
	public void onFallingBlockJoin(EntityJoinLevelEvent event) {
		if (!this.isEnabled()
				|| event.getLevel().isClientSide()
				|| event.loadedFromDisk()
				|| !(event.getEntity() instanceof FallingBlockEntity fallingBlock)
				|| ((FallingBlockEntityAccessor) fallingBlock).insanesurvivaloverhaul$getHurtEntities())
			return;

		BlockState state = fallingBlock.getBlockState();
		if (state.is(BLACKLIST))
			return;

		for (FallingBlockDamage fallingBlockDamage : fallingBlocksDamage) {
			if (fallingBlockDamage.id.matches(state.getBlockHolder())) {
				setHurtsEntities(fallingBlock, fallingBlockDamage.damagePerDistance, fallingBlockDamage.maxDamage);
				return;
			}
		}
		setHurtsEntities(fallingBlock, damagePerDistance.floatValue(), maxDamage);
	}

	private static void setHurtsEntities(FallingBlockEntity fallingBlock, float damagePerDistance, int maxDamage) {
		if (damagePerDistance <= 0f || maxDamage <= 0)
			return;
		fallingBlock.setHurtsEntities(damagePerDistance, maxDamage);
	}

	/**
	 * Returns true if falling blocks of this state hurt entities thanks to this feature
	 */
	public static boolean hurtsEntities(BlockState state) {
		if (state.is(BLACKLIST))
			return false;
		for (FallingBlockDamage fallingBlockDamage : fallingBlocksDamage) {
			if (fallingBlockDamage.id.matches(state.getBlockHolder()))
				return fallingBlockDamage.damagePerDistance > 0f && fallingBlockDamage.maxDamage > 0;
		}
		return damagePerDistance > 0d && maxDamage > 0;
	}
}
