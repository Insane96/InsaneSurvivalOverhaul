package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.data.ObjTagValue;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = ISOModules.MOVEMENT, description = "Slowdown based off the terrain entities walking on. Custom Terrain Slowdown are controlled via json in this feature's folder")
public class TerrainSlowdown extends JsonFeature {
	private static final ResourceLocation TERRAIN_SLOWDOWN_ID = InsaneSO.id("terrain_slowdown");
	public static final TagKey<Item> SNOW_SLOWDOWN_IGNORE = ISOItemTagsProvider.create("snow_slowdown_ignore");

	public static final ArrayList<ObjTagValue<Block>> CUSTOM_TERRAIN_SLOWDOWN_DEFAULT = new ArrayList<>(List.of(
			ObjTagValue.of("#minecraft:ice", 0.55d, Registries.BLOCK)
	));
	public static final ArrayList<ObjTagValue<Block>> customTerrainSlowdown = new ArrayList<>();
	public static final ArrayList<ObjTagValue<Block>> CUSTOM_IN_TERRAIN_SLOWDOWN_DEFAULT = new ArrayList<>(List.of(
			ObjTagValue.of("minecraft:snow", 0.075d, Registries.BLOCK),
			ObjTagValue.of("minecraft:powder_snow", 0.5d, Registries.BLOCK)
	));
	public static final ArrayList<ObjTagValue<Block>> customInTerrainSlowdown = new ArrayList<>();
	public static final ResourceLocation CUSTOM_TERRAIN_SLOWDOWN_SYNC_ID = InsaneSO.id("custom_terrain_slowdown");
	public static final ResourceLocation CUSTOM_IN_TERRAIN_SLOWDOWN_SYNC_ID = InsaneSO.id("custom_in_terrain_slowdown");

	@Config
	public static Boolean frostWalkerReducesIceSlowdown = true;

	@Config(description = "If true, snow slowdown is ignored if wearing leather boots (or any item in insanesurvivaloverhaul:snow_slowdown_ignore item tag)")
	public static Boolean snowSlowdownIgnoredWithLeatherBoots = true;

	@Override
	public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super.init(module, enabledByDefault, canBeDisabled);
		addSyncType(CUSTOM_TERRAIN_SLOWDOWN_SYNC_ID, new SyncType(json -> loadAndReadJson(json, customTerrainSlowdown, CUSTOM_TERRAIN_SLOWDOWN_DEFAULT, ObjTagValue.LIST_TYPE, Registries.BLOCK)));
		getJsonConfigs().add(new JsonConfig<>("custom_terrain_slowdown.json", customTerrainSlowdown, CUSTOM_TERRAIN_SLOWDOWN_DEFAULT, ObjTagValue.LIST_TYPE).syncToClient(CUSTOM_TERRAIN_SLOWDOWN_SYNC_ID).withRegistryFor(Registries.BLOCK));
		addSyncType(CUSTOM_IN_TERRAIN_SLOWDOWN_SYNC_ID, new SyncType(json -> loadAndReadJson(json, customInTerrainSlowdown, CUSTOM_IN_TERRAIN_SLOWDOWN_DEFAULT, ObjTagValue.LIST_TYPE, Registries.BLOCK)));
		getJsonConfigs().add(new JsonConfig<>("custom_in_terrain_slowdown.json", customInTerrainSlowdown, CUSTOM_IN_TERRAIN_SLOWDOWN_DEFAULT, ObjTagValue.LIST_TYPE).syncToClient(CUSTOM_IN_TERRAIN_SLOWDOWN_SYNC_ID).withRegistryFor(Registries.BLOCK));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Pre event) {
		if (!this.isEnabled())
			return;

		if (event.getEntity().onGround())
			applyTerrainSlowdown(event.getEntity(), 1f);
		else if (event.getEntity().isInFluidType())
			event.getEntity().getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(TERRAIN_SLOWDOWN_ID);
	}

	@SubscribeEvent
	public void onLivingTick(EntityTickEvent.Pre event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof LivingEntity livingEntity)
				|| livingEntity instanceof Player
				|| (livingEntity.tickCount + livingEntity.getId()) % 5 != 0)
			return;

		applyTerrainSlowdown(livingEntity, 0.65f);
	}

	public static void applyTerrainSlowdown(LivingEntity entity, float multiplier) {
		double onTerrainSlowdown = 0d;
		int blocks = 0;
		AABB bb = entity.getBoundingBox();
		int mX = Mth.floor(bb.minX);
		int mY = Mth.floor(bb.minY);
		int mZ = Mth.floor(bb.minZ);
		for (int x2 = mX; x2 < bb.maxX; x2++) {
			for (int z2 = mZ; z2 < bb.maxZ; z2++) {
				BlockState state = entity.level().getBlockState(BlockPos.containing(x2, entity.position().y - 0.02d, z2));
				if (state.isAir())
					continue;
				double blockSlowdown = 0d;
				for (ObjTagValue<Block> blockObjTagValue : customTerrainSlowdown) {
					if (blockObjTagValue.id.matches(state.getBlock())) {
						blockSlowdown = blockObjTagValue.value;
						if (state.is(BlockTags.ICE) && frostWalkerReducesIceSlowdown) {
							int lvl = entity.getItemBySlot(EquipmentSlot.FEET).getEnchantmentLevel(
									entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FROST_WALKER)
							);
							blockSlowdown -= blockSlowdown * (lvl * 0.5f);
						}
						else if (state.is(BlockTags.SNOW)
								&& entity.getItemBySlot(EquipmentSlot.FEET).is(SNOW_SLOWDOWN_IGNORE)
								&& snowSlowdownIgnoredWithLeatherBoots) {
							blockSlowdown = 0;
						}
						blocks++;
						break;
					}
				}
				onTerrainSlowdown += blockSlowdown;
			}
		}
		if (blocks != 0)
			onTerrainSlowdown /= blocks;

		double inTerrainSlowdown = 0d;
		blocks = 0;
		for (int x2 = mX; x2 < bb.maxX; x2++) {
			for (int y2 = mY; y2 < bb.maxY; y2++) {
				for (int z2 = mZ; z2 < bb.maxZ; z2++) {
					BlockState state = entity.level().getBlockState(new BlockPos(x2, y2, z2));
					if (state.isAir())
						continue;
					double blockSlowdown = 0d;
					for (ObjTagValue<Block> blockObjTagValue : customInTerrainSlowdown) {
						if (blockObjTagValue.id.matches(state.getBlock())) {
							blockSlowdown = blockObjTagValue.value;
							if (state.is(BlockTags.SNOW)
									&& entity.getItemBySlot(EquipmentSlot.FEET).is(SNOW_SLOWDOWN_IGNORE)
									&& snowSlowdownIgnoredWithLeatherBoots) {
								blockSlowdown = 0;
							}
							blocks++;
							break;
						}
					}
					inTerrainSlowdown += blockSlowdown;
				}
			}
		}
		if (blocks != 0)
			inTerrainSlowdown /= blocks;

		double slowdown = (onTerrainSlowdown + inTerrainSlowdown) * multiplier;
		applySlowdown(entity, slowdown);
	}

	public static void applySlowdown(LivingEntity entity, double slowdown) {
		AttributeInstance attribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attribute == null)
			return;
		AttributeModifier modifier = attribute.getModifier(TERRAIN_SLOWDOWN_ID);
		if (modifier != null) {
			if (modifier.amount() != -slowdown)
				attribute.removeModifier(modifier);
			else
				return;
		}
		if (slowdown == 0d)
			return;
		MCUtils.applyModifier(entity, Attributes.MOVEMENT_SPEED, TERRAIN_SLOWDOWN_ID, -slowdown, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
	}
}