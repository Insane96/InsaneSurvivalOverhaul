package insane96mcp.iguanatweaksreborn.module.movement;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagValue;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Label(name = "Terrain Slowdown", description = "Slowdown based off the terrain entities walking on. Custom Terrain Slowdown are controlled via json in this feature's folder")
@LoadFeature(module = Modules.Ids.MOVEMENT)
public class TerrainSlowdown extends JsonFeature {
	private static final UUID TERRAIN_SLOWDOWN = UUID.fromString("a849043f-b280-4789-bafd-5da8e8e1078e");
	public static final TagKey<Item> SNOW_SLOWDOWN_IGNORE = ISOItemTagsProvider.create("snow_slowdown_ignore");

	public static final ArrayList<IdTagValue> CUSTOM_TERRAIN_SLOWDOWN_DEFAULT = new ArrayList<>(List.of(
			IdTagValue.newTag("minecraft:ice", 0.55d)
	));
	public static final ArrayList<IdTagValue> customTerrainSlowdown = new ArrayList<>();
	public static final ArrayList<IdTagValue> CUSTOM_IN_TERRAIN_SLOWDOWN_DEFAULT = new ArrayList<>(List.of(
			IdTagValue.newId("minecraft:snow", 0.075d),
			IdTagValue.newId("minecraft:powder_snow", 0.5d)
	));
	public static final ArrayList<IdTagValue> customInTerrainSlowdown = new ArrayList<>();

	@Config
	public static Boolean frostWalkerReducesIceSlowdown = true;

	@Config
	@Label(description = "If true, snow slowdown is ignored if wearing leather boots (or any item in iguanatweaksreborn:snow_slowdown_ignore item tag)")
	public static Boolean snowSlowdownIgnoredWithLeatherBoots = true;

	public TerrainSlowdown(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		addSyncType(new ResourceLocation(InsaneSO.MOD_ID, "custom_terrain_slowdown"), new SyncType(json -> loadAndReadJson(json, customTerrainSlowdown, CUSTOM_TERRAIN_SLOWDOWN_DEFAULT, IdTagValue.LIST_TYPE)));
		JSON_CONFIGS.add(new JsonConfig<>("custom_terrain_slowdown.json", customTerrainSlowdown, CUSTOM_TERRAIN_SLOWDOWN_DEFAULT, IdTagValue.LIST_TYPE, true, new ResourceLocation(InsaneSO.MOD_ID, "custom_terrain_slowdown")));
		addSyncType(new ResourceLocation(InsaneSO.MOD_ID, "custom_in_terrain_slowdown"), new SyncType(json -> loadAndReadJson(json, customInTerrainSlowdown, CUSTOM_IN_TERRAIN_SLOWDOWN_DEFAULT, IdTagValue.LIST_TYPE)));
		JSON_CONFIGS.add(new JsonConfig<>("custom_in_terrain_slowdown.json", customInTerrainSlowdown, CUSTOM_IN_TERRAIN_SLOWDOWN_DEFAULT, IdTagValue.LIST_TYPE, true, new ResourceLocation(InsaneSO.MOD_ID, "custom_in_terrain_slowdown")));
	}

	@Override
	public String getModConfigFolder() {
		return InsaneSO.CONFIG_FOLDER;
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (!this.isEnabled()
				|| event.phase != TickEvent.Phase.START)
			return;

		if (event.player.onGround())
			applyTerrainSlowdown(event.player, 1f);
		else if (event.player.isInFluidType())
			removeTerrainSlowdown(event.player);
	}

	@SubscribeEvent
	public void onLivingTick(LivingEvent.LivingTickEvent event) {
		if (!this.isEnabled()
				|| event.getEntity() instanceof Player
				|| (event.getEntity().tickCount + event.getEntity().getId()) % 5 != 0)
			return;

		applyTerrainSlowdown(event.getEntity(), 0.65f);
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
				for (IdTagValue idTagValue : customTerrainSlowdown) {
					if (idTagValue.id.matchesBlock(state.getBlock())) {
						blockSlowdown = idTagValue.value;
						if (state.is(BlockTags.ICE) && frostWalkerReducesIceSlowdown) {
							int lvl = entity.getItemBySlot(EquipmentSlot.FEET).getEnchantmentLevel(Enchantments.FROST_WALKER);
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
					for (IdTagValue idTagValue : customInTerrainSlowdown) {
						if (idTagValue.id.matchesBlock(state.getBlock())) {
							blockSlowdown = idTagValue.value;
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
		AttributeModifier modifier = entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(TERRAIN_SLOWDOWN);
		if (slowdown == 0d) {
			removeTerrainSlowdown(entity, modifier);
			return;
		}
		if (modifier == null || modifier.getAmount() != -slowdown) {
			if (modifier != null)
				removeTerrainSlowdown(entity, modifier);
			MCUtils.applyModifier(entity, Attributes.MOVEMENT_SPEED, TERRAIN_SLOWDOWN, "terrain slowdown", -slowdown, AttributeModifier.Operation.MULTIPLY_BASE, false);
		}
	}

	public static void removeTerrainSlowdown(LivingEntity entity, @Nullable AttributeModifier modifier) {
		if (modifier != null)
			entity.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(TERRAIN_SLOWDOWN);
	}

	public static void removeTerrainSlowdown(LivingEntity entity) {
		AttributeModifier modifier = entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(TERRAIN_SLOWDOWN);
		removeTerrainSlowdown(entity, modifier);
	}
}