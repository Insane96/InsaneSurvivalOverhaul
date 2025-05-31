package insane96mcp.iguanatweaksreborn.module.sleeprespawn.death;

import insane96mcp.iguanatweaksreborn.integration.CuriosIntegration;
import insane96mcp.iguanatweaksreborn.integration.ToolBeltIntegration;
import insane96mcp.iguanatweaksreborn.module.experience.DroppedExperience;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GraveBlockEntity extends BlockEntity {
    public static final String ITEMS_TAG = "items";
    public static final String XP_STORED_TAG = "xp_stored";
    public static final String OWNER_TAG = "owner";
    public static final String DEATH_NUMBER_TAG = "death_number";
    public static final String MESSAGE_TAG = "message";
    private List<SlottedStack> items = new ArrayList<>();
    private int xpStored = 0;
    private UUID owner;
    private int deathNumber;
    @Nullable
    private Component message;
    public GraveBlockEntity(BlockPos pos, BlockState state) {
        super(Death.GRAVE_BLOCK_ENTITY_TYPE.get(), pos, state);
    }

    public List<SlottedStack> getItems() {
        return this.items;
    }

    public void setItems(List<SlottedStack> items) {
        this.items = items;
        this.setChanged();
    }

    public void addItem(ItemStack stack) {
        this.items.add(new SlottedStack(stack));
        this.setChanged();
    }

    public void addItem(byte slot, ItemStack stack) {
        this.items.add(new SlottedStack(slot, stack));
        this.setChanged();
    }

    /// True if at least one item was added, false otherwise
    public boolean addPlayerItems(Player player) {
        NonNullList<ItemStack> itemStacks = player.getInventory().items;
        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack item = itemStacks.get(i);
            if (!item.isEmpty() && item.getEnchantmentLevel(Enchantments.VANISHING_CURSE) == 0)
                this.addItem((byte) i, item);
        }
        itemStacks = player.getInventory().armor;
        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack item = itemStacks.get(i);
            if (!item.isEmpty() && item.getEnchantmentLevel(Enchantments.VANISHING_CURSE) == 0)
                this.addItem((byte) (i + player.getInventory().items.size()), item);
        }
        itemStacks = player.getInventory().offhand;
        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack item = itemStacks.get(i);
            if (!item.isEmpty() && item.getEnchantmentLevel(Enchantments.VANISHING_CURSE) == 0)
                this.addItem((byte) (i + player.getInventory().items.size() + player.getInventory().armor.size()), item);
        }
        if (ModList.get().isLoaded("toolbelt"))
            ToolBeltIntegration.onDeath(this.items, player);
        if (ModList.get().isLoaded("curios"))
            CuriosIntegration.onDeath(this.items, player);
        this.setChanged();
        return !this.items.isEmpty();
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
            CompoundTag slottedStackTag = itemsList.getCompound(i);
            if (!slottedStackTag.contains("Slot"))
                this.items.add(new SlottedStack(net.minecraft.world.item.ItemStack.of(slottedStackTag)));
            else {
                byte j = slottedStackTag.getByte("Slot");
                this.items.add(new SlottedStack(j, net.minecraft.world.item.ItemStack.of(slottedStackTag)));
            }
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
    protected void saveAdditional(@NotNull CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        ListTag itemsList = new ListTag();
        for (SlottedStack slottedStack : this.items) {
            CompoundTag itemStackTag = new CompoundTag();
            if (slottedStack.slot != null)
                itemStackTag.putByte("Slot", slottedStack.slot);
            slottedStack.stack.save(itemStackTag);
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

    public void giveContentToPlayer(Player player) {
        this.items.stream()
                .filter(slottedStack -> slottedStack.slot != null)
                .forEach(slottedStack -> {
                    if (player.getInventory().getItem(slottedStack.slot).isEmpty())
                        player.getInventory().setItem(slottedStack.slot, slottedStack.stack);
                    else
                        slottedStack.invalidateSlot();
                });
        this.items.stream()
                .filter(slottedStack -> slottedStack.slot == null)
                .forEach(slottedStack -> player.getInventory().add(slottedStack.stack));
        this.items.clear();
        player.level().playSound(null, player, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
        this.dropExperience(player.blockPosition());
        this.owner = null;
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
        this.getItems().forEach(itemStack -> dropItem(level, itemStack.stack, pos));
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

    public static final class SlottedStack {
        @Nullable
        private Byte slot;
        private final ItemStack stack;

        public SlottedStack(byte slot, ItemStack stack) {
            this.slot = slot;
            this.stack = stack;
        }

        public SlottedStack(ItemStack stack) {
            this.slot = null;
            this.stack = stack;
        }

        @Nullable
        public Byte slot() {
            return slot;
        }

        public void invalidateSlot() {
            this.slot = null;
        }

        public ItemStack stack() {
            return stack;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (SlottedStack) obj;
            return Objects.equals(this.slot, that.slot) &&
                    Objects.equals(this.stack, that.stack);
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, stack);
        }

        @Override
        public String toString() {
            return "SlottedStack{" +
                    "slot=" + slot + ", " +
                    "stack=" + stack + '}';
        }
    }
}
