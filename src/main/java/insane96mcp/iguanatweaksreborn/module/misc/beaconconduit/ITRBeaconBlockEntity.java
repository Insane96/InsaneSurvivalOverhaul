package insane96mcp.iguanatweaksreborn.module.misc.beaconconduit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import insane96mcp.iguanatweaksreborn.InsaneSurvivalTweaks;
import insane96mcp.insanelib.data.IdTagValue;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.*;

public class ITRBeaconBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, MenuProvider, Nameable, StackedContentsCompatible {
    private static final int MAX_LEVELS = 8;
    public static final int DATA_EFFECT = 0;
    public static final int DATA_AMPLIFIER = 1;
    public static final int DATA_TIME_LEFT = 2;
    public static final int DATA_LAYERS = 3;
    public static final int DATA_COUNT = 4;
    public static final int SLOT_COUNT = 1;
    private static final int BLOCKS_CHECK_PER_TICK = 10;
    private static final Component DEFAULT_NAME = Component.translatable("container.beacon");
    /**
     * A list of beam segments for this beacon.
     */
    List<BeaconBeamSection> beamSections = Lists.newArrayList();
    private List<BeaconBeamSection> checkingBeamSections = Lists.newArrayList();
    /**
     * The number of levels of this beacon's pyramid.
     */
    int layers;
    private int lastCheckY;
    /**
     * The effect given by this beacon.
     */
    @Nullable
    MobEffect effect;
    /**
     * The amplifier of the effect given by this beacon.
     */
    int amplifier;
    /**
     * The time left for this beacon to apply the effect
     */
    int timeLeft;
    //Increase based off layers (1, 2, 4, 8 hours)
    static final int MAX_TIME_LEFT = 576000; //8 hours
    /**
     * The custom name for this beacon.
     */
    @Nullable
    private Component name;
    protected NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final ContainerData dataAccess = new ContainerData() {
        public int get(int id) {
            switch (id) {
                case DATA_EFFECT -> {
                    return MobEffect.getIdFromNullable(ITRBeaconBlockEntity.this.effect);
                }
                case DATA_AMPLIFIER -> {
                    return ITRBeaconBlockEntity.this.amplifier;
                }
                case DATA_TIME_LEFT -> {
                    return ITRBeaconBlockEntity.this.timeLeft;
                }
                case DATA_LAYERS -> {
                    return ITRBeaconBlockEntity.this.layers;
                }
            }

            return 0;
        }

        public void set(int id, int value) {
            switch (id) {
                case DATA_EFFECT:
                    if (!ITRBeaconBlockEntity.this.level.isClientSide && !ITRBeaconBlockEntity.this.beamSections.isEmpty()) {
                        ITRBeaconBlockEntity.playSound(ITRBeaconBlockEntity.this.level, ITRBeaconBlockEntity.this.worldPosition, SoundEvents.BEACON_POWER_SELECT);
                    }

                    ITRBeaconBlockEntity.this.effect = MobEffect.byId(value);
                    break;
                case DATA_AMPLIFIER:
                    ITRBeaconBlockEntity.this.amplifier = value;
                    break;
                case DATA_TIME_LEFT:
                    ITRBeaconBlockEntity.this.timeLeft = value;
                    break;
                case DATA_LAYERS:
                    ITRBeaconBlockEntity.this.layers = value;
                    break;
            }

        }

        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ITRBeaconBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BeaconConduit.BEACON_BLOCK_ENTITY_TYPE.get(), pPos, pBlockState);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, ITRBeaconBlockEntity beacon) {
        int x = pPos.getX();
        int y = pPos.getY();
        int z = pPos.getZ();
        BlockPos blockpos;
        if (beacon.lastCheckY < y) {
            blockpos = pPos;
            beacon.checkingBeamSections = Lists.newArrayList();
            beacon.lastCheckY = pPos.getY() - 1;
        }
        else {
            blockpos = new BlockPos(x, beacon.lastCheckY + 1, z);
        }

        BeaconBeamSection SRBeaconBlockEntity$beaconbeamsection = beacon.checkingBeamSections.isEmpty() ? null : beacon.checkingBeamSections.get(beacon.checkingBeamSections.size() - 1);
        int ySurface = pLevel.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

        //Vanilla logic for the beam I guess
        for (int i1 = 0; i1 < BLOCKS_CHECK_PER_TICK && blockpos.getY() <= ySurface; ++i1) {
            BlockState blockstate = pLevel.getBlockState(blockpos);
            float[] afloat = blockstate.getBeaconColorMultiplier(pLevel, blockpos, pPos);
            if (afloat != null) {
                if (beacon.checkingBeamSections.size() <= 1) {
                    SRBeaconBlockEntity$beaconbeamsection = new BeaconBeamSection(afloat);
                    beacon.checkingBeamSections.add(SRBeaconBlockEntity$beaconbeamsection);
                }
                else if (SRBeaconBlockEntity$beaconbeamsection != null) {
                    if (Arrays.equals(afloat, SRBeaconBlockEntity$beaconbeamsection.color)) {
                        SRBeaconBlockEntity$beaconbeamsection.increaseHeight();
                    }
                    else {
                        SRBeaconBlockEntity$beaconbeamsection = new BeaconBeamSection(new float[]{(SRBeaconBlockEntity$beaconbeamsection.color[0] + afloat[0]) / 2.0F, (SRBeaconBlockEntity$beaconbeamsection.color[1] + afloat[1]) / 2.0F, (SRBeaconBlockEntity$beaconbeamsection.color[2] + afloat[2]) / 2.0F});
                        beacon.checkingBeamSections.add(SRBeaconBlockEntity$beaconbeamsection);
                    }
                }
            }
            else {
                if (SRBeaconBlockEntity$beaconbeamsection == null || blockstate.getLightBlock(pLevel, blockpos) >= 15 && !blockstate.is(Blocks.BEDROCK)) {
                    beacon.checkingBeamSections.clear();
                    beacon.lastCheckY = ySurface;
                    break;
                }

                SRBeaconBlockEntity$beaconbeamsection.increaseHeight();
            }

            blockpos = blockpos.above();
            ++beacon.lastCheckY;
        }
        int lvl = beacon.layers;
        if (!BeaconConduit.isValidEffect(beacon.effect))
            beacon.effect = null;
        if (lvl > 0 && beacon.effect != null && beacon.timeLeft > 0) {
            beacon.timeLeft -= BeaconConduit.getEffectTimeScale(beacon.effect, beacon.amplifier);
        }
        if (pLevel.getGameTime() % 80 == 0) {
            if (!beacon.beamSections.isEmpty()) {
                beacon.layers = updateBase(pLevel, x, y, z);
            }

            if (beacon.timeLeft > 0 && beacon.layers > 0 && !beacon.beamSections.isEmpty()) {
                applyEffects(pLevel, pPos, beacon.layers, beacon.effect, beacon.amplifier);
                playSound(pLevel, pPos, SoundEvents.BEACON_AMBIENT);
            }
        }
        if (beacon.effect != null && pLevel.getGameTime() % 10 == 0 && !beacon.items.get(0).isEmpty()) {
            int timeLeftAmount = BeaconConduit.getPaymentTime(beacon.items.get(0));
            if (beacon.timeLeft <= MAX_TIME_LEFT) {
                beacon.timeLeft += timeLeftAmount;
                beacon.removeItem(0, 1);
            }
        }
        if (beacon.lastCheckY >= ySurface) {
            beacon.lastCheckY = pLevel.getMinBuildHeight() - 1;
            boolean hasLevels = lvl > 0;
            beacon.beamSections = beacon.checkingBeamSections;
            if (!pLevel.isClientSide) {
                boolean flag1 = beacon.layers > 0;
                if (!hasLevels && flag1) {
                    playSound(pLevel, pPos, SoundEvents.BEACON_ACTIVATE);

                    pLevel.getEntitiesOfClass(ServerPlayer.class, new AABB(x, y, z, x, y - 4, z).inflate(10.0D, 5.0D, 10.0D))
                            .forEach(serverplayer -> CriteriaTriggers.CONSTRUCT_BEACON.trigger(serverplayer, beacon.layers));
                }
                else if (hasLevels && !flag1) {
                    playSound(pLevel, pPos, SoundEvents.BEACON_DEACTIVATE);
                }
            }
        }
    }

    private static int updateBase(Level level, int beaconX, int beaconY, int beaconZ) {
        int i = 0;

        for (int yRel = 1; yRel <= MAX_LEVELS; i = yRel++) {
            int y = beaconY - yRel;
            if (y < level.getMinBuildHeight())
                break;

            boolean isBeaconBaseBlock = true;

            for (int x = beaconX - yRel; x <= beaconX + yRel && isBeaconBaseBlock; ++x) {
                for (int z = beaconZ - yRel; z <= beaconZ + yRel; ++z) {
                    if (!level.getBlockState(new BlockPos(x, y, z)).is(BlockTags.BEACON_BASE_BLOCKS)) {
                        isBeaconBaseBlock = false;
                        break;
                    }
                }
            }

            if (!isBeaconBaseBlock)
                break;
        }

        return i;
    }

    /**
     * Marks this {@code BlockEntity} as no longer valid (removed from the level).
     */
    public void setRemoved() {
        playSound(this.level, this.worldPosition, SoundEvents.BEACON_DEACTIVATE);
        super.setRemoved();
    }

    private static void applyEffects(Level pLevel, BlockPos pPos, int layers, @Nullable MobEffect effect, int amplifier) {
        if (pLevel.isClientSide
                || effect == null)
            return;

        double d0 = 10 + getBeaconRange(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), layers);
        AABB aabb = (new AABB(pPos)).inflate(d0).expandTowards(0.0D, pLevel.getHeight(), 0.0D);
        List<LivingEntity> list = pLevel.getEntitiesOfClass(LivingEntity.class, aabb,
                livingEntity -> livingEntity instanceof Player || (livingEntity instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() != null));
        for (LivingEntity livingEntity : list) {
            livingEntity.addEffect(new MobEffectInstance(effect, 240, amplifier, true, true));
        }

    }

    private static double getBeaconRange(Level level, int x, int y, int z, int layers) {
        Map<Block, Integer> blocksCount = new HashMap<>();

        for (int layer = 1; layer <= layers; layer++) {
            int relativeY = y - layer;

            for (int relativeX = x - layer; relativeX <= x + layer; ++relativeX) {
                for (int relativeZ = z - layer; relativeZ <= z + layer; ++relativeZ) {
                    if (level.getBlockState(new BlockPos(relativeX, relativeY, relativeZ)).is(BlockTags.BEACON_BASE_BLOCKS)) {
                        Block block = level.getBlockState(new BlockPos(relativeX, relativeY, relativeZ)).getBlock();
                        blocksCount.merge(block, 1, Integer::sum);
                    }
                }
            }
        }

        double range = 1d;
        for (Map.Entry<Block, Integer> entry : blocksCount.entrySet()) {
            Optional<IdTagValue> optional = BeaconConduit.blocksList
                    .stream()
                    .filter(idTagValue -> idTagValue.id.matchesBlock(entry.getKey()))
                    .findFirst();
            if (optional.isPresent())
                range += optional.get().value * entry.getValue() / layers;
        }

        return range;
    }

    public static void playSound(Level pLevel, BlockPos pPos, SoundEvent pSound) {
        pLevel.playSound(null, pPos, pSound, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    public List<BeaconBeamSection> getBeamSections() {
        return this.layers == 0 ? ImmutableList.of() : this.beamSections;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.effect = MobEffect.byId(pTag.getInt("Effect"));
        this.amplifier = pTag.getInt("Amplifier");
        this.timeLeft = pTag.getInt("TimeLeft");
        if (pTag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(pTag.getString("CustomName"));
        }
    }

    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("Effect", MobEffect.getIdFromNullable(this.effect));
        pTag.putInt("Amplifier", this.amplifier);
        pTag.putInt("TimeLeft", this.timeLeft);
        pTag.putInt("Levels", this.layers);
        if (this.name != null) {
            pTag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
    }

    /**
     * Sets the custom name for this beacon.
     */
    public void setCustomName(@Nullable Component pName) {
        this.name = pName;
    }

    @Nullable
    public Component getCustomName() {
        return this.name;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(InsaneSurvivalTweaks.MOD_ID + ".container.beacon");
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
        return new ITRBeaconMenu(pContainerId, pInventory, this, this.dataAccess, ContainerLevelAccess.create(this.level, this.getBlockPos()));
    }

    public Component getDisplayName() {
        return this.getName();
    }

    public Component getName() {
        return this.name != null ? this.name : DEFAULT_NAME;
    }

    public void setLevel(Level pLevel) {
        super.setLevel(pLevel);
        this.lastCheckY = pLevel.getMinBuildHeight() - 1;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        //return new int[] {0};
        return new int[] {};
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction side) {
        return false;
        /*if (!pItemStack.is(ItemTags.BEACON_PAYMENT_ITEMS))
            return false;

        return side != Direction.DOWN;*/
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction side) {
        return false;
        //return side == Direction.DOWN;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return this.items.get(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return ContainerHelper.removeItem(this.items, pSlot, pAmount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return ContainerHelper.takeItem(this.items, pSlot);
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        this.items.set(pSlot, pStack);
        if (pStack.getCount() > this.getMaxStackSize()) {
            pStack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return Container.stillValidBlockEntity(this, pPlayer);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public void fillStackedContents(StackedContents pContents) {
        for(ItemStack itemstack : this.items) {
            pContents.accountStack(itemstack);
        }
    }

    public static class BeaconBeamSection {
        /**
         * The colors of this section of a beacon beam, in RGB float format.
         */
        final float[] color;
        private int height;

        public BeaconBeamSection(float[] pColor) {
            this.color = pColor;
            this.height = 1;
        }

        protected void increaseHeight() {
            ++this.height;
        }

        /**
         * @return The colors of this section of a beacon beam, in RGB float format.
         */
        public float[] getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}
