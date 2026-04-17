package insane96mcp.insanesurvivaloverhaul.module.items.pouch;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class PouchContainer implements Container {
    private final ItemStack stack;
    private final NonNullList<ItemStack> items;

    public PouchContainer(ItemStack stack) {
        this.stack = stack;
        this.items = NonNullList.withSize(9, ItemStack.EMPTY);
        load();
    }

    private void load() {
        items.clear();
        stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(items);
    }

    private void save() {
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    @Override
    public int getContainerSize() { return items.size(); }

    @Override
    public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }

    @Override
    public ItemStack getItem(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(items, slot, count);
        save();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot >= 0 && slot < this.items.size()) {
            ItemStack itemstack = this.items.get(slot);
            if (itemstack.isEmpty())
                return ItemStack.EMPTY;

            this.items.set(slot, ItemStack.EMPTY);
            save();
            return itemstack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        items.set(slot, itemStack);
        save();
    }

    @Override
    public void setChanged() { save(); }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public void clearContent() {
        items.clear();
        save();
    }

}
