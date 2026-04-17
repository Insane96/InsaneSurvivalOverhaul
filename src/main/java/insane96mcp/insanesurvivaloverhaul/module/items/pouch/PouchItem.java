package insane96mcp.insanesurvivaloverhaul.module.items.pouch;

import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class PouchItem extends Item {
    public PouchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean canFitInsideContainerItems(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {

            openMenu(player, player.getItemInHand(hand));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(new PouchTooltip(getContentsList(stack)));
    }

    public static NonNullList<ItemStack> getContentsList(ItemStack stack) {
        NonNullList<ItemStack> list = NonNullList.withSize(PouchMenu.CONTAINER_SIZE, ItemStack.EMPTY);
        stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(list);
        return list;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack pouchStack, Slot slot, ClickAction action, Player player) {
        if (pouchStack.getCount() != 1 || action != ClickAction.SECONDARY || !slot.allowModification(player))
            return false;

        ItemStack slotStack = slot.getItem();
        PouchContainer pouch = new PouchContainer(pouchStack);
        return insertIntoPouch(pouch, slotStack, player, false);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack pouchStack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (pouchStack.getCount() != 1 || action != ClickAction.SECONDARY || !slot.allowModification(player))
            return false;

        PouchContainer pouch = new PouchContainer(pouchStack);
        if (otherStack.isEmpty()) {
            openMenu(player, pouchStack);
            return true;
        }
        return insertIntoPouch(pouch, otherStack, player, true);
    }

    private boolean insertIntoPouch(PouchContainer pouch, ItemStack stack, Player player, boolean openMenuIfEmpty) {
        if (stack.isEmpty() || !stack.getItem().canFitInsideContainerItems())
            return false;

        for (int i = 0; i < pouch.getContainerSize(); i++) {
            ItemStack inside = pouch.getItem(i);
            if (!inside.isEmpty() && ItemStack.isSameItemSameComponents(inside, stack)) {
                int space = inside.getMaxStackSize() - inside.getCount();
                if (space > 0) {
                    int toMove = Math.min(space, stack.getCount());
                    inside.grow(toMove);
                    stack.shrink(toMove);
                    pouch.setChanged();
                    playInsertSound(player);
                    if (stack.isEmpty())
                        return true;
                }
            }
        }

        for (int i = 0; i < pouch.getContainerSize(); i++) {
            ItemStack inside = pouch.getItem(i);
            if (inside.isEmpty()) {
                int toMove = Math.min(stack.getCount(), stack.getMaxStackSize());
                pouch.setItem(i, stack.split(toMove));
                playInsertSound(player);
                if (stack.isEmpty())
                    return true;
            }
        }

        return !stack.isEmpty();
    }

    private void playInsertSound(Entity pEntity) {
        pEntity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + pEntity.level().getRandom().nextFloat() * 0.4F);
    }

    public static void openMenu(Player player, ItemStack pouchStack) {
        player.containerMenu.resumeRemoteUpdates();
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new PouchMenu(id, inv, new PouchContainer(pouchStack)),
                Component.translatable(InsaneSO.lang("pouch"))
        ));
        player.containerMenu.suppressRemoteUpdates();
    }
}
