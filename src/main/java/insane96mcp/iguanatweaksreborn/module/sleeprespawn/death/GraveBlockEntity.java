package insane96mcp.iguanatweaksreborn.module.sleeprespawn.death;

import insane96mcp.iguanatweaksreborn.module.experience.DroppedExperience;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GraveBlockEntity extends BlockEntity {
    public static final String ITEMS_TAG = "items";
    public static final String XP_STORED_TAG = "xp_stored";
    public static final String OWNER_TAG = "owner";
    public static final String DEATH_NUMBER_TAG = "death_number";
    public static final String MESSAGE_TAG = "message";
    private List<ItemStack> items = new ArrayList<>();
    private int xpStored = 0;
    private UUID owner;
    private int deathNumber;
    @Nullable
    private Component message;
    public GraveBlockEntity(BlockPos pos, BlockState state) {
        super(Death.GRAVE_BLOCK_ENTITY_TYPE.get(), pos, state);
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public void setItems(List<ItemStack> items) {
        this.items = items;
        this.setChanged();
    }

    public int getXpStored() {
        return this.xpStored;
    }

    public void setXpStored(int xpStored) {
        this.xpStored = xpStored;
        this.setChanged();
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public int getDeathNumber() {
        return this.deathNumber;
    }

    public void setDeathNumber(int deathNumber) {
        this.deathNumber = deathNumber;
    }

    public Component getMessage() {
        return this.message;
    }

    public void setMessage(Component message) {
        this.message = message;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        ListTag itemsList = compoundTag.getList(ITEMS_TAG, 10);

        for(int i = 0; i < itemsList.size(); ++i) {
            CompoundTag itemStackTag = itemsList.getCompound(i);
            this.items.add(ItemStack.of(itemStackTag));
        }
        this.xpStored = compoundTag.getInt(XP_STORED_TAG);
        if (compoundTag.contains(OWNER_TAG)) {
            this.owner = compoundTag.getUUID(OWNER_TAG);
            this.deathNumber = compoundTag.getInt(DEATH_NUMBER_TAG);
        }
        if (compoundTag.contains(MESSAGE_TAG)) {
            this.message = Component.Serializer.fromJson(compoundTag.getString(MESSAGE_TAG));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        ListTag itemsList = new ListTag();
        for (ItemStack itemStack : this.items) {
            CompoundTag itemStackTag = new CompoundTag();
            itemStack.save(itemStackTag);
            itemsList.add(itemStackTag);
        }
        compoundTag.put(ITEMS_TAG, itemsList);
        compoundTag.putInt(XP_STORED_TAG, this.xpStored);
        if (this.owner != null) {
            compoundTag.putUUID(OWNER_TAG, this.owner);
            compoundTag.putInt(DEATH_NUMBER_TAG, this.deathNumber);
        }
        if (this.message != null)
            compoundTag.putString(MESSAGE_TAG, Component.Serializer.toJson(this.message));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // Will get tag from #getUpdateTag
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T t) {
        if (level.getGameTime() % 20 != 9)
            return;

        GraveBlockEntity graveBlockEntity = (GraveBlockEntity) t;
        if (graveBlockEntity.owner == null)
            return;
        Optional<ServerPlayer> oPlayer = getPlayerOwner((ServerLevel) level, graveBlockEntity.owner);
        oPlayer.ifPresent(player -> {
            if (player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS)) != graveBlockEntity.deathNumber)
                graveBlockEntity.dropContent(pos);
        });
    }

    public void dropContent(BlockPos pos) {
        this.dropExperience(pos);
        this.dropItems(pos);
        this.owner = null;
    }

    public void dropExperience(BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel))
            return;
        int amount = this.getXpStored();
        if (amount > 0) {
            while(amount > 0) {
                int i = ExperienceOrb.getExperienceValue(amount);
                amount -= i;
                if (!ExperienceOrb.tryMergeToExisting(serverLevel, pos.getCenter(), i)) {
                    ExperienceOrb xpOrb = new ExperienceOrb(level, pos.getCenter().x, pos.getCenter().y, pos.getCenter().z, i);
                    level.addFreshEntity(xpOrb);
                    xpOrb.getPersistentData().putBoolean(DroppedExperience.XP_PROCESSED, true);
                }
            }
            this.setXpStored(0);
        }
    }

    public void dropItems(BlockPos pos) {
        this.getItems().forEach(itemStack -> dropItem(level, itemStack, pos));
        this.getItems().clear();
    }

    public static void dropItem(Level level, ItemStack stack, BlockPos pos) {
        if (stack.isEmpty())
            return;

        ItemEntity itementity = new ItemEntity(level, pos.getCenter().x, pos.getCenter().y + 0.8, pos.getCenter().z, stack);
        itementity.setPickUpDelay(5);
        //2 minutes
        itementity.lifespan = Death.despawnTime;

        float f = level.random.nextFloat() * 0.1F;
        float f1 = level.random.nextFloat() * ((float)Math.PI * 2F);
        itementity.setDeltaMovement((-Mth.sin(f1) * f), 0.1F, (Mth.cos(f1) * f));
        level.addFreshEntity(itementity);
    }

    public static Optional<ServerPlayer> getPlayerOwner(ServerLevel level, UUID playerUUID) {
        for (ServerPlayer player : level.players()) {
            UUID uuid = player.getUUID();
            if (uuid.equals(playerUUID))
                return Optional.of(player);
        }

        return Optional.empty();
    }
}
