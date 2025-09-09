package insane96mcp.iguanatweaksreborn.module.misc.beaconconduit;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;

public class ISOBeaconMenu extends AbstractContainerMenu {
    private static final int PAYMENT_SLOT = 0;
    private static final int SLOT_COUNT = 1;
    private static final int INV_SLOT_START = 1;
    private static final int INV_SLOT_END = 28;
    private static final int USE_ROW_SLOT_START = 28;
    private static final int USE_ROW_SLOT_END = 37;
    private final PaymentSlot paymentSlot;
    private final Container container;
    private final ContainerData beaconData;
    private final ContainerLevelAccess access;

    public ISOBeaconMenu(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(ISOBeaconBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public ISOBeaconMenu(int pContainerId, Inventory pPlayerInventory, Container pContainer, ContainerData containerData, ContainerLevelAccess pAccess) {
        super(BeaconConduit.BEACON_MENU_TYPE.get(), pContainerId);
        checkContainerDataCount(containerData, ISOBeaconBlockEntity.DATA_COUNT);
        checkContainerSize(pContainer, ISOBeaconBlockEntity.SLOT_COUNT);
        this.container = pContainer;
        this.beaconData = containerData;
        this.access = pAccess;
        this.addDataSlots(containerData);
        this.paymentSlot = new PaymentSlot(pContainer, 0, 21, 110);
        this.addSlot(this.paymentSlot);
        int i = 36;
        int j = 137;

        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(pPlayerInventory, l + k * 9 + 9, 36 + l * 18, 137 + k * 18));
            }
        }

        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(pPlayerInventory, i1, 36 + i1 * 18, 195));
        }

    }

    /**
     * Called when the container is closed.
     */
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        if (!pPlayer.level().isClientSide) {
            ItemStack itemstack = this.paymentSlot.remove(this.paymentSlot.getMaxStackSize());
            if (!itemstack.isEmpty()) {
                pPlayer.drop(itemstack, false);
            }

        }
    }

    /**
     * Determines whether supplied player can use this container
     */
    public boolean stillValid(Player pPlayer) {
        return this.container.stillValid(pPlayer);
    }

    public void setData(int pId, int pData) {
        super.setData(pId, pData);
        this.broadcastChanges();
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (pIndex == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            }
            else if (this.moveItemStackTo(itemstack1, 0, 1, false)) { //Forge Fix Shift Clicking in beacons with stacks larger then 1.
                return ItemStack.EMPTY;
            }
            else if (pIndex >= 1 && pIndex < 28) {
                if (!this.moveItemStackTo(itemstack1, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (pIndex >= 28 && pIndex < 37) {
                if (!this.moveItemStackTo(itemstack1, 1, 28, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(itemstack1, 1, 37, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, itemstack1);
        }

        return itemstack;
    }

    @Nullable
    public MobEffect getEffect() {
        return MobEffect.byId(this.beaconData.get(ISOBeaconBlockEntity.DATA_EFFECT));
    }

    public int getAmplifier() {
        return this.beaconData.get(ISOBeaconBlockEntity.DATA_AMPLIFIER);
    }

    public int getTimeLeft() {
        return this.beaconData.get(ISOBeaconBlockEntity.DATA_TIME_LEFT);
    }

    public int getLayers() {
        return this.beaconData.get(ISOBeaconBlockEntity.DATA_LAYERS);
    }

    public int getRange() {
        return this.beaconData.get(ISOBeaconBlockEntity.DATA_RANGE);
    }

    public void updateEffect(Optional<MobEffect> mobEffect, int amplifier) {
        this.beaconData.set(ISOBeaconBlockEntity.DATA_EFFECT, mobEffect.map(MobEffect::getId).orElse(-1));
        this.beaconData.set(ISOBeaconBlockEntity.DATA_AMPLIFIER, amplifier);
        this.access.execute(Level::blockEntityChanged);
    }

    public int getMaxAmplifier(MobEffect effect) {
        for (BeaconConduit.BeaconEffect instance : BeaconConduit.effects) {
            if (instance.getEffect().equals(effect))
                return instance.getMaxAmplifier();
        }
        return -1;
    }

    class PaymentSlot extends Slot {
        public PaymentSlot(Container pContainer, int pContainerIndex, int pXPosition, int pYPosition) {
            super(pContainer, pContainerIndex, pXPosition, pYPosition);
        }

        public boolean mayPlace(ItemStack pStack) {
            return pStack.is(ItemTags.BEACON_PAYMENT_ITEMS) && BeaconConduit.beacon$requiresPayment;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            ISOBeaconMenu.this.slotsChanged(this.container);
        }
    }
}