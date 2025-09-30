package insane96mcp.iguanatweaksreborn.module.items.pouch;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PouchMenu extends AbstractContainerMenu {
    public static final int CONTAINER_SIZE = 9;
    private final Container container;

    public PouchMenu(int containerId, Inventory inventory, Container container) {
        super(MenuType.GENERIC_3x3, containerId);
        checkContainerSize(container, CONTAINER_SIZE);
        this.container = container;
        container.startOpen(inventory.player);

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(container, j + i * 3, 62 + j * 18, 17 + i * 18) {
                    @Override
                    public boolean mayPlace(ItemStack pStack) {
                        return pStack.getItem().canFitInsideContainerItems();
                    }
                });
            }
        }

        for (int i1 = 0; i1 < 3; ++i1) {
            for (int k1 = 0; k1 < 9; ++k1) {
                this.addSlot(new Slot(inventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 84 + i1 * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return !(stack.getItem() instanceof PouchItem);
                    }

                    @Override
                    public boolean mayPickup(Player pPlayer) {
                        return !(this.getItem().getItem() instanceof PouchItem);
                    }
                });
            }
        }

        for (int j1 = 0; j1 < 9; ++j1) {
            this.addSlot(new Slot(inventory, j1, 8 + j1 * 18, 142) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !(stack.getItem() instanceof PouchItem);
                }

                @Override
                public boolean mayPickup(Player pPlayer) {
                    return !(this.getItem().getItem() instanceof PouchItem);
                }
            });
        }

    }

    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    public ItemStack quickMoveStack(Player player, int slotId) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            newStack = stack.copy();
            if (slotId < this.container.getContainerSize()) {
                if (!this.moveItemStackTo(stack, this.container.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(stack, 0, this.container.getContainerSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty())
                slot.setByPlayer(ItemStack.EMPTY);
            else
                slot.setChanged();
        }

        return newStack;
    }
}
