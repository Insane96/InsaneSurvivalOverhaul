package insane96mcp.insanesurvivaloverhaul.module.misc.beacon;

import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.data.ObjTagValue;
import insane96mcp.insanelib.util.SnapshotList;
import insane96mcp.insanelib.world.effect.ILMobEffect;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import insane96mcp.insanesurvivaloverhaul.setup.SimpleBlockWithItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = ISOModules.MISC, description = "Beacon has been redesigned to have more effects and range based off blocks used for pyramid. Effects and blocks ranges are controlled via json config in this feature's folder.")
public class BeaconFeature extends JsonFeature {
    public static final SimpleBlockWithItem BEACON = SimpleBlockWithItem.register("beacon",
            () -> new ISOBeaconBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BEACON)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ISOBeaconBlockEntity>> BEACON_BLOCK_ENTITY =
            ISORegistries.BLOCK_ENTITIES.register("beacon",
                    () -> BlockEntityType.Builder.of(ISOBeaconBlockEntity::new, BEACON.block().get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<ISOBeaconMenu>> BEACON_MENU_TYPE =
            ISORegistries.MENU_TYPES.register("beacon", () -> new MenuType<>(ISOBeaconMenu::new, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MobEffect, MobEffect> BLOCK_REACH = ISORegistries.MOB_EFFECTS.register("block_reach",
            () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x818894)
                    .addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE, InsaneSO.id("block_reach_effect"), 0.5d, AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<MobEffect, MobEffect> ENTITY_REACH = ISORegistries.MOB_EFFECTS.register("entity_reach",
            () -> new ILMobEffect(MobEffectCategory.BENEFICIAL, 0x818894)
                    .addAttributeModifier(Attributes.ENTITY_INTERACTION_RANGE, InsaneSO.id("entity_reach_effect"), 0.075d, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

    private static final ResourceLocation EFFECTS_SYNC_ID = InsaneSO.id("beacon_effects");

    public static final ArrayList<ObjTagValue<Block>> BLOCKS_RANGES_DEFAULT = new ArrayList<>(List.of(
            ObjTagValue.of("minecraft:iron_block", 1d, Registries.BLOCK),
            ObjTagValue.of("minecraft:emerald_block", 1d, Registries.BLOCK),
            ObjTagValue.of("minecraft:gold_block", 2d, Registries.BLOCK),
            ObjTagValue.of("minecraft:diamond_block", 3d, Registries.BLOCK),
            ObjTagValue.of("minecraft:netherite_block", 4d, Registries.BLOCK)
    ));
    public static final ArrayList<ObjTagValue<Block>> blocksRanges = new ArrayList<>();

    public static final ArrayList<BeaconEffect> EFFECTS_DEFAULT = new ArrayList<>(List.of(
            new BeaconEffect(MobEffects.MOVEMENT_SPEED, 2, 1),
            new BeaconEffect(MobEffects.JUMP, 2, 1),
            new BeaconEffect("insanesurvivaloverhaul:block_reach", 2, 1),
            new BeaconEffect(MobEffects.DIG_SPEED, 2, 2),
            new BeaconEffect(MobEffects.FIRE_RESISTANCE, 0, 2),
            new BeaconEffect("stamina:vigour", 1, 2),
            new BeaconEffect(MobEffects.INVISIBILITY, 0, 2),
            new BeaconEffect(MobEffects.DAMAGE_BOOST, 2, 3),
            new BeaconEffect(MobEffects.DAMAGE_RESISTANCE, 2, 3),
            new BeaconEffect(MobEffects.NIGHT_VISION, 0, 3),
            new BeaconEffect(MobEffects.SLOW_FALLING, 0, 3),
            new BeaconEffect(MobEffects.REGENERATION, 0, 4),
            new BeaconEffect("insanesurvivaloverhaul:regenerating_absorption", 1, 4),
            new BeaconEffect("insanesurvivaloverhaul:entity_reach", 2, 4)
    ));
    public static final SnapshotList<BeaconEffect> effects = new SnapshotList<>();

    @Config(min = 0, description = "Range of the beacon before adding the bonus from the blocks of the pyramid.")
    public static Integer baseRange = 16;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        getJsonConfigs().add(new JsonConfig<>("blocks_ranges.json", blocksRanges, BLOCKS_RANGES_DEFAULT, ObjTagValue.LIST_TYPE).withRegistryFor(Registries.BLOCK));
        addSyncType(EFFECTS_SYNC_ID, new SyncType(json -> loadAndReadJson(json, effects, EFFECTS_DEFAULT, BeaconEffect.LIST_TYPE)));
        getJsonConfigs().add(new JsonConfig<>("effects.json", effects, EFFECTS_DEFAULT, BeaconEffect.LIST_TYPE).syncToClient(EFFECTS_SYNC_ID));
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSO.CONFIG_FOLDER;
    }

    /**
     * Le voci i cui effetti non sono registrati (es. mod non installata) restano nel json ma non sono selezionabili.
     */
    public static List<BeaconEffect> getAvailableEffects() {
        return effects.stream().filter(beaconEffect -> beaconEffect.getEffect() != null).toList();
    }

    @Nullable
    public static BeaconEffect getBeaconEffect(Holder<MobEffect> effect) {
        for (BeaconEffect beaconEffect : effects) {
            if (effect.is(beaconEffect.getLocation()))
                return beaconEffect;
        }
        return null;
    }
}
