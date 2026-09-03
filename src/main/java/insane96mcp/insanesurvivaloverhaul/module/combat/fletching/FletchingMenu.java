package insane96mcp.insanesurvivaloverhaul.module.combat.fletching;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class FletchingMenu extends AbstractContainerMenu {
    public static final int INGREDIENT_SLOT = 0;
    public static final int CATALYST_1_SLOT = 1;
    public static final int CATALYST_2_SLOT = 2;
    public static final int RESULT_SLOT = 3;
    private static final int INV_SLOT_START = 4;
    private static final int INV_SLOT_END = 31;
    private static final int USE_ROW_SLOT_START = 31;
    private static final int USE_ROW_SLOT_END = 40;

    private final ContainerLevelAccess access;
    private final Player player;
    private final Container craftContainer = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            FletchingMenu.this.slotsChanged(this);
        }
    };
    private final Container resultContainer = new SimpleContainer(1);
    private final RecipeManager.CachedCheck<FletchingRecipeInput, FletchingRecipe> quickCheck =
            RecipeManager.createCheck(FletchingFeature.FLETCHING_RECIPE_TYPE.get());

    public FletchingMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public FletchingMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(FletchingFeature.FLETCHING_MENU_TYPE.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        this.addSlot(new Slot(this.craftContainer, INGREDIENT_SLOT, 56, 44));
        this.addSlot(new Slot(this.craftContainer, CATALYST_1_SLOT, 47, 26));
        this.addSlot(new Slot(this.craftContainer, CATALYST_2_SLOT, 65, 26));
        this.addSlot(new Slot(this.resultContainer, 0, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player takingPlayer, ItemStack stack) {
                FletchingMenu.this.onTakeResult(takingPlayer);
                super.onTake(takingPlayer, stack);
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    private FletchingRecipeInput createRecipeInput() {
        return new FletchingRecipeInput(
                this.craftContainer.getItem(INGREDIENT_SLOT),
                this.craftContainer.getItem(CATALYST_1_SLOT),
                this.craftContainer.getItem(CATALYST_2_SLOT));
    }

    /**
     * Consumes the ingredient/catalyst stacks the matched recipe declares, mirroring what {@code assemble()}
     * produced into the result slot.
     */
    private void onTakeResult(Player takingPlayer) {
        this.quickCheck.getRecipeFor(this.createRecipeInput(), takingPlayer.level()).ifPresent(recipeHolder -> {
            FletchingRecipe recipe = recipeHolder.value();
            this.craftContainer.removeItem(INGREDIENT_SLOT, recipe.getIngredient().count());
            this.craftContainer.removeItem(CATALYST_1_SLOT, recipe.getCatalyst1().count());
            recipe.getCatalyst2().ifPresent(catalyst2 -> this.craftContainer.removeItem(CATALYST_2_SLOT, catalyst2.count()));
        });
    }

    @Override
    public void slotsChanged(Container container) {
        this.access.execute((level, pos) -> this.updateResult((ServerPlayer) this.player, level));
    }

    private void updateResult(ServerPlayer serverPlayer, Level level) {
        FletchingRecipeInput input = this.createRecipeInput();
        ItemStack result = this.quickCheck.getRecipeFor(input, level)
                .map(recipeHolder -> recipeHolder.value().assemble(input, level.registryAccess()))
                .filter(stack -> stack.isItemEnabled(level.enabledFeatures()))
                .orElse(ItemStack.EMPTY);
        this.resultContainer.setItem(0, result);
        this.setRemoteSlot(RESULT_SLOT, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), RESULT_SLOT, result));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, Blocks.FLETCHING_TABLE);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.craftContainer));
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != this.resultContainer && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();
            if (index == RESULT_SLOT) {
                if (!this.moveItemStackTo(slotStack, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, result);
            } else if (index >= INV_SLOT_START) {
                if (!this.moveItemStackTo(slotStack, INGREDIENT_SLOT, RESULT_SLOT, false)) {
                    if (index < INV_SLOT_END) {
                        if (!this.moveItemStackTo(slotStack, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(slotStack, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(slotStack, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }
        return result;
    }
}
