package insane96mcp.insanesurvivaloverhaul.module.misc.beacon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import insane96mcp.insanelib.data.ObjTagValue;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.LockCode;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ISOBeaconBlockEntity extends BlockEntity implements MenuProvider, Nameable {
    public static final int MAX_LAYERS = 4;
    public static final int DATA_EFFECT = 0;
    public static final int DATA_AMPLIFIER = 1;
    public static final int DATA_LAYERS = 2;
    public static final int DATA_RANGE = 3;
    public static final int DATA_COUNT = 4;
    private static final int BLOCKS_CHECK_PER_TICK = 10;
    private static final int EFFECT_DURATION = 240;
    private static final Component DEFAULT_NAME = Component.translatable("container.beacon");
    private static final String TAG_EFFECT = "effect";
    private static final String TAG_AMPLIFIER = "amplifier";

    private List<BeaconBeamSection> beamSections = Lists.newArrayList();
    private List<BeaconBeamSection> checkingBeamSections = Lists.newArrayList();
    private int layers;
    private int lastCheckY;
    @Nullable
    private Holder<MobEffect> effect;
    private int amplifier;
    private int range;
    @Nullable
    private Component name;
    private LockCode lockKey = LockCode.NO_LOCK;
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int id) {
            return switch (id) {
                case DATA_EFFECT -> BeaconMenu.encodeEffect(ISOBeaconBlockEntity.this.effect);
                case DATA_AMPLIFIER -> ISOBeaconBlockEntity.this.amplifier;
                case DATA_LAYERS -> ISOBeaconBlockEntity.this.layers;
                case DATA_RANGE -> ISOBeaconBlockEntity.this.range;
                default -> 0;
            };
        }

        @Override
        public void set(int id, int value) {
            switch (id) {
                case DATA_EFFECT -> {
                    if (!ISOBeaconBlockEntity.this.level.isClientSide && !ISOBeaconBlockEntity.this.beamSections.isEmpty())
                        playSound(ISOBeaconBlockEntity.this.level, ISOBeaconBlockEntity.this.worldPosition, SoundEvents.BEACON_POWER_SELECT);
                    ISOBeaconBlockEntity.this.effect = BeaconMenu.decodeEffect(value);
                }
                case DATA_AMPLIFIER -> {
                    ISOBeaconBlockEntity.this.amplifier = value;
                    ISOBeaconBlockEntity.this.updateRange();
                }
                case DATA_LAYERS -> ISOBeaconBlockEntity.this.layers = value;
                case DATA_RANGE -> ISOBeaconBlockEntity.this.range = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ISOBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(BeaconFeature.BEACON_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ISOBeaconBlockEntity beacon) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        BlockPos checkPos;
        if (beacon.lastCheckY < y) {
            checkPos = pos;
            beacon.checkingBeamSections = Lists.newArrayList();
            beacon.lastCheckY = pos.getY() - 1;
        }
        else {
            checkPos = new BlockPos(x, beacon.lastCheckY + 1, z);
        }

        BeaconBeamSection section = beacon.checkingBeamSections.isEmpty() ? null : beacon.checkingBeamSections.get(beacon.checkingBeamSections.size() - 1);
        int ySurface = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

        for (int i = 0; i < BLOCKS_CHECK_PER_TICK && checkPos.getY() <= ySurface; i++) {
            BlockState blockState = level.getBlockState(checkPos);
            Integer color = blockState.getBeaconColorMultiplier(level, checkPos, pos);
            if (color != null) {
                if (beacon.checkingBeamSections.size() <= 1) {
                    section = new BeaconBeamSection(color);
                    beacon.checkingBeamSections.add(section);
                }
                else if (section != null) {
                    if (color == section.color) {
                        section.increaseHeight();
                    }
                    else {
                        section = new BeaconBeamSection(FastColor.ARGB32.average(section.color, color));
                        beacon.checkingBeamSections.add(section);
                    }
                }
            }
            else {
                if (section == null || blockState.getLightBlock(level, checkPos) >= 15 && !blockState.is(Blocks.BEDROCK)) {
                    beacon.checkingBeamSections.clear();
                    beacon.lastCheckY = ySurface;
                    break;
                }
                section.increaseHeight();
            }

            checkPos = checkPos.above();
            beacon.lastCheckY++;
        }

        int previousLayers = beacon.layers;
        if (level.getGameTime() % 80L == 0L) {
            if (!beacon.beamSections.isEmpty())
                beacon.layers = updateBase(level, x, y, z);

            if (beacon.layers > 0 && !beacon.beamSections.isEmpty()) {
                beacon.updateRange();
                beacon.applyEffects();
                playSound(level, pos, SoundEvents.BEACON_AMBIENT);
            }
        }

        if (beacon.lastCheckY >= ySurface) {
            beacon.lastCheckY = level.getMinBuildHeight() - 1;
            boolean hadLayers = previousLayers > 0;
            beacon.beamSections = beacon.checkingBeamSections;
            if (!level.isClientSide) {
                boolean hasLayers = beacon.layers > 0;
                if (!hadLayers && hasLayers) {
                    playSound(level, pos, SoundEvents.BEACON_ACTIVATE);
                    for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, new AABB(x, y, z, x, y - 4, z).inflate(10.0, 5.0, 10.0))) {
                        CriteriaTriggers.CONSTRUCT_BEACON.trigger(serverPlayer, beacon.layers);
                    }
                }
                else if (hadLayers && !hasLayers) {
                    playSound(level, pos, SoundEvents.BEACON_DEACTIVATE);
                }
            }
        }
    }

    private static int updateBase(Level level, int beaconX, int beaconY, int beaconZ) {
        int layers = 0;
        for (int layer = 1; layer <= MAX_LAYERS; layers = layer++) {
            int y = beaconY - layer;
            if (y < level.getMinBuildHeight())
                break;

            boolean isValidLayer = true;
            for (int x = beaconX - layer; x <= beaconX + layer && isValidLayer; x++) {
                for (int z = beaconZ - layer; z <= beaconZ + layer; z++) {
                    if (!level.getBlockState(new BlockPos(x, y, z)).is(BlockTags.BEACON_BASE_BLOCKS)) {
                        isValidLayer = false;
                        break;
                    }
                }
            }

            if (!isValidLayer)
                break;
        }
        return layers;
    }

    /**
     * Applies the selected effect only if the current pyramid allows it, without changing the selection:
     * rebuilding the pyramid makes the chosen effect and level active again.
     */
    private void applyEffects() {
        if (this.level == null || this.level.isClientSide || this.effect == null)
            return;

        BeaconEffect beaconEffect = BeaconFeature.getBeaconEffect(this.effect);
        if (beaconEffect == null || !beaconEffect.isAvailableAt(this.layers))
            return;

        int effectiveAmplifier = beaconEffect.clampAmplifier(this.amplifier, this.layers);
        AABB aabb = new AABB(this.worldPosition).inflate(this.range).expandTowards(0.0, this.level.getHeight(), 0.0);
        List<LivingEntity> targets = this.level.getEntitiesOfClass(LivingEntity.class, aabb, ISOBeaconBlockEntity::isValidTarget);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(this.effect, EFFECT_DURATION, effectiveAmplifier, true, true));
        }
    }

    private static boolean isValidTarget(LivingEntity entity) {
        return entity instanceof Player
                || entity instanceof Villager
                || (entity instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() != null);
    }

    private void updateRange() {
        if (this.level == null || this.level.isClientSide)
            return;
        this.range = this.layers > 0 ? (int) (BeaconFeature.baseRange + this.getPyramidRange()) : 0;
    }

    private double getPyramidRange() {
        Map<Block, Integer> blocksCount = new HashMap<>();
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int layer = 1; layer <= this.layers; layer++) {
            int y = this.worldPosition.getY() - layer;
            for (int x = this.worldPosition.getX() - layer; x <= this.worldPosition.getX() + layer; x++) {
                for (int z = this.worldPosition.getZ() - layer; z <= this.worldPosition.getZ() + layer; z++) {
                    BlockState blockState = this.level.getBlockState(checkPos.set(x, y, z));
                    if (blockState.is(BlockTags.BEACON_BASE_BLOCKS))
                        blocksCount.merge(blockState.getBlock(), 1, Integer::sum);
                }
            }
        }

        double pyramidRange = 1d;
        for (Map.Entry<Block, Integer> entry : blocksCount.entrySet()) {
            for (ObjTagValue<Block> blockRange : BeaconFeature.blocksRanges) {
                if (blockRange.id.matches(entry.getKey())) {
                    pyramidRange += blockRange.value * entry.getValue() / this.layers;
                    break;
                }
            }
        }

        BeaconEffect beaconEffect = this.effect == null ? null : BeaconFeature.getBeaconEffect(this.effect);
        int effectiveAmplifier = beaconEffect == null ? 0 : beaconEffect.clampAmplifier(this.amplifier, this.layers);
        return pyramidRange / (effectiveAmplifier * 0.25f + 1);
    }

    @Override
    public void setRemoved() {
        playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    public static void playSound(Level level, BlockPos pos, SoundEvent sound) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public List<BeaconBeamSection> getBeamSections() {
        return this.layers == 0 ? ImmutableList.of() : this.beamSections;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.effect = null;
        if (tag.contains(TAG_EFFECT, 8)) {
            ResourceLocation effectId = ResourceLocation.tryParse(tag.getString(TAG_EFFECT));
            if (effectId != null)
                this.effect = BuiltInRegistries.MOB_EFFECT.getHolder(effectId).orElse(null);
        }
        this.amplifier = tag.getInt(TAG_AMPLIFIER);
        if (tag.contains("CustomName", 8))
            this.name = parseCustomNameSafe(tag.getString("CustomName"), registries);
        this.lockKey = LockCode.fromTag(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.effect != null)
            this.effect.unwrapKey().ifPresent(key -> tag.putString(TAG_EFFECT, key.location().toString()));
        tag.putInt(TAG_AMPLIFIER, this.amplifier);
        tag.putInt("Levels", this.layers);
        if (this.name != null)
            tag.putString("CustomName", Component.Serializer.toJson(this.name, registries));
        this.lockKey.addToTag(tag);
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return BaseContainerBlockEntity.canUnlock(player, this.lockKey, this.getDisplayName())
                ? new ISOBeaconMenu(containerId, this.dataAccess, ContainerLevelAccess.create(this.level, this.getBlockPos()))
                : null;
    }

    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    @Override
    public Component getName() {
        return this.name != null ? this.name : DEFAULT_NAME;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        this.name = componentInput.get(DataComponents.CUSTOM_NAME);
        this.lockKey = componentInput.getOrDefault(DataComponents.LOCK, LockCode.NO_LOCK);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, this.name);
        if (!this.lockKey.equals(LockCode.NO_LOCK))
            components.set(DataComponents.LOCK, this.lockKey);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("CustomName");
        tag.remove("Lock");
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        this.lastCheckY = level.getMinBuildHeight() - 1;
    }

    public static class BeaconBeamSection {
        final int color;
        private int height;

        public BeaconBeamSection(int color) {
            this.color = color;
            this.height = 1;
        }

        protected void increaseHeight() {
            this.height++;
        }

        public int getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}
