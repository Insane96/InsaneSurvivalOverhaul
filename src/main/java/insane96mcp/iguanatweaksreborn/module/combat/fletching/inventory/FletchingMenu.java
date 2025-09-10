package insane96mcp.iguanatweaksreborn.module.combat.fletching.inventory;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.Fletching;
import insane96mcp.iguanatweaksreborn.module.combat.fletching.crafting.FletchingRecipe;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class FletchingMenu extends RecipeBookMenu<CraftingContainer> {
    public static final int INGREDIENT_SLOT = 0;
    public static final int CATALYST_1_SLOT = INGREDIENT_SLOT + 1;
    public static final int CATALYST_2_SLOT = CATALYST_1_SLOT + 1;
    public static final int RESULT_SLOT = CATALYST_2_SLOT + 1;
    public static final int SLOT_COUNT = RESULT_SLOT + 1;
    private static final int INV_SLOT_START = RESULT_SLOT + 1;
    private static final int INV_SLOT_END = INV_SLOT_START + 27;
    private static final int USE_ROW_SLOT_START = INV_SLOT_END;
    private static final int USE_ROW_SLOT_END = USE_ROW_SLOT_START + 9;
    private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 1);
    private final ResultContainer resultSlots = new ResultContainer();
    protected final Level level;
    private final ContainerLevelAccess access;
    private final Player player;
    final RecipeType<? extends FletchingRecipe> recipeType;
    private final RecipeBookType recipeBookType;

    public FletchingMenu(int pContainerId, Inventory pPlayerInventory) {
        this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
    }

    public FletchingMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
        super(Fletching.FLETCHING_MENU_TYPE.get(), pContainerId);
        this.access = pAccess;
        this.player = pPlayerInventory.player;
        this.recipeType = Fletching.FLETCHING_RECIPE_TYPE.get();
        this.recipeBookType = InsaneSO.FLETCHING_RECIPE_BOOK_TYPE;
        this.level = pPlayerInventory.player.level();

        this.addSlot(new Slot(this.craftSlots, INGREDIENT_SLOT, 56, 44));
        this.addSlot(new Slot(this.craftSlots, CATALYST_1_SLOT, 47, 26));
        this.addSlot(new Slot(this.craftSlots, CATALYST_2_SLOT, 65, 26));
        this.addSlot(new FletchingResultSlot(pPlayerInventory.player, this.craftSlots, this.resultSlots, RESULT_SLOT, 124, 35));

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
        }
    }

    public void fillCraftSlotsStackedContents(StackedContents pItemHelper) {
        this.craftSlots.fillStackedContents(pItemHelper);
    }

    public void clearCraftingContent() {
        this.craftSlots.clearContent();
        this.resultSlots.clearContent();
    }

    public boolean recipeMatches(Recipe<? super CraftingContainer> pRecipe) {
        return pRecipe.matches(this.craftSlots, this.player.level());
    }

    @Override
    public void handlePlacement(boolean pPlaceAll, Recipe<?> pRecipe, ServerPlayer pPlayer) {
        new FletchingPlaceRecipe(this).recipeClicked(pPlayer, (Recipe<CraftingContainer>) pRecipe, pPlaceAll);
    }

    /**
     * Called when the container is closed.
     */
    public void removed(Player pPlayer) {
        super.removed(pPlayer);
        this.access.execute((p_39371_, p_39372_) -> this.clearContainer(pPlayer, this.craftSlots));
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pSlot) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pSlot);
        if (slot.hasItem()) {
            ItemStack itemStackInSlot = slot.getItem();
            itemstack = itemStackInSlot.copy();
            if (pSlot == RESULT_SLOT) {
                if (!this.moveItemStackTo(itemStackInSlot, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemStackInSlot, itemstack);
            }
            //If inventory slots
            else if (pSlot > RESULT_SLOT) {
                if (this.isIngredient(itemStackInSlot)) {
                    if (!this.moveItemStackTo(itemStackInSlot, INGREDIENT_SLOT, CATALYST_1_SLOT, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (this.isCatalyst1(itemStackInSlot)) {
                    if (!this.moveItemStackTo(itemStackInSlot, CATALYST_1_SLOT, CATALYST_2_SLOT, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (this.isCatalyst2(itemStackInSlot)) {
                    if (!this.moveItemStackTo(itemStackInSlot, CATALYST_2_SLOT, RESULT_SLOT, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (pSlot < INV_SLOT_END) {
                    if (!this.moveItemStackTo(itemStackInSlot, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                else if (pSlot < USE_ROW_SLOT_END && !this.moveItemStackTo(itemStackInSlot, INV_SLOT_START, INV_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.moveItemStackTo(itemStackInSlot, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            else {
                slot.setChanged();
            }

            if (itemStackInSlot.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, itemStackInSlot);
        }

        return itemstack;
    }

    protected static void slotChangedCraftingGrid(AbstractContainerMenu pMenu, Level pLevel, Player pPlayer, CraftingContainer pContainer, ResultContainer pResult) {
        if (pLevel.isClientSide)
            return;

        ServerPlayer serverplayer = (ServerPlayer)pPlayer;
        ItemStack resultStack = ItemStack.EMPTY;
        Optional<FletchingRecipe> oFletchingRecipe = pLevel.getServer().getRecipeManager().getRecipeFor(Fletching.FLETCHING_RECIPE_TYPE.get(), pContainer, pLevel);
        if (oFletchingRecipe.isPresent()) {
            FletchingRecipe fletchingRecipe = oFletchingRecipe.get();
            if (pResult.setRecipeUsed(pLevel, serverplayer, fletchingRecipe)) {
                ItemStack itemstack1 = fletchingRecipe.assemble(pContainer, pLevel.registryAccess());
                if (itemstack1.isItemEnabled(pLevel.enabledFeatures())) {
                    resultStack = itemstack1;
                }
            }
        }

        pResult.setItem(RESULT_SLOT, resultStack);
        pMenu.setRemoteSlot(RESULT_SLOT, resultStack);
        serverplayer.connection.send(new ClientboundContainerSetSlotPacket(pMenu.containerId, pMenu.incrementStateId(), RESULT_SLOT, resultStack));
    }

    @Override
    public void slotsChanged(Container pContainer) {
        this.access.execute((p_39386_, p_39387_) -> slotChangedCraftingGrid(this, p_39386_, this.player, this.craftSlots, this.resultSlots));
    }

    /**
     * Determines whether supplied player can use this container
     */
    public boolean stillValid(Player pPlayer) {
        return stillValid(this.access, pPlayer, Fletching.FLETCHING_TABLE.block().get());
    }

    public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
        return pSlot.container != this.resultSlots && super.canTakeItemForPickAll(pStack, pSlot);
    }

    public int getResultSlotIndex() {
        return RESULT_SLOT;
    }

    public int getGridWidth() {
        return this.craftSlots.getWidth();
    }

    public int getGridHeight() {
        return this.craftSlots.getHeight();
    }

    public int getSize() {
        return SLOT_COUNT;
    }

    public RecipeBookType getRecipeBookType() {
        return this.recipeBookType;
    }

    public boolean shouldMoveToInventory(int pSlotIndex) {
        return pSlotIndex != this.getResultSlotIndex();
    }

    protected boolean isIngredient(ItemStack pStack) {
        return this.level.getRecipeManager().getAllRecipesFor((RecipeType<FletchingRecipe>)this.recipeType).stream().anyMatch(recipe -> recipe.getBaseIngredient().is(pStack.getItem()));
    }

    protected boolean isCatalyst1(ItemStack pStack) {
        return this.level.getRecipeManager().getAllRecipesFor((RecipeType<FletchingRecipe>)this.recipeType).stream().anyMatch(recipe -> recipe.getCatalyst1().is(pStack.getItem()));
    }

    protected boolean isCatalyst2(ItemStack pStack) {
        return this.level.getRecipeManager().getAllRecipesFor((RecipeType<FletchingRecipe>)this.recipeType).stream().anyMatch(recipe -> recipe.getCatalyst2() != null &&  recipe.getCatalyst2().is(pStack.getItem()));
    }
}
