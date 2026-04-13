package insane96mcp.insanesurvivaloverhaul.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Fired on NeoForge's event bus before {@link insane96mcp.insanesurvivaloverhaul.module.death.DeathPenalty}
 * processes item and experience loss on player death.
 * <p>
 * <b>Cancel</b> to skip all penalties entirely (e.g. player respawning at an obelisk).
 * <p>
 * <b>Mutable penalty fields</b> ({@code lostItemsPercentage}, {@code lostDurabilityPercentage},
 * {@code destroyItems}, {@code lostXpPercentage}, {@code destroyXp}) can be adjusted before processing runs.
 * <p>
 * <b>{@link #setItemDropHandler itemDropHandler}</b> — replaces the default {@code player.drop} behaviour.
 * Grave mods can set this to store items in a grave block instead.
 * <p>
 * <b>{@link #addItemList addItemList}</b> — registers extra slot lists (backpacks, Curios, etc.) to be
 * processed with the same penalties as the vanilla inventory slots.
 * <p>
 * <b>{@link #setItemFilter itemFilter}</b> — predicate applied to every stack before any penalty; return
 * {@code false} to leave that stack untouched.
 */
public class DeathPenaltyEvent extends Event implements ICancellableEvent {

    private final ServerPlayer player;
    private int lostItemsPercentage;
    private int lostDurabilityPercentage;
    private boolean destroyItems;
    private int lostXpPercentage;
    private boolean destroyXp;
    @Nullable
    private Consumer<ItemStack> itemDropHandler;
    private final List<List<ItemStack>> additionalItemLists = new ArrayList<>();
    private Predicate<ItemStack> itemFilter = stack -> true;

    public DeathPenaltyEvent(ServerPlayer player, int lostItemsPercentage, int lostDurabilityPercentage, boolean destroyItems, int lostXpPercentage, boolean destroyXp) {
        this.player = player;
        this.lostItemsPercentage = lostItemsPercentage;
        this.lostDurabilityPercentage = lostDurabilityPercentage;
        this.destroyItems = destroyItems;
        this.lostXpPercentage = lostXpPercentage;
        this.destroyXp = destroyXp;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public int getLostItemsPercentage() {
        return lostItemsPercentage;
    }

    public void setLostItemsPercentage(int lostItemsPercentage) {
        this.lostItemsPercentage = lostItemsPercentage;
    }

    public int getLostDurabilityPercentage() {
        return lostDurabilityPercentage;
    }

    public void setLostDurabilityPercentage(int lostDurabilityPercentage) {
        this.lostDurabilityPercentage = lostDurabilityPercentage;
    }

    public boolean isDestroyItems() {
        return destroyItems;
    }

    public void setDestroyItems(boolean destroyItems) {
        this.destroyItems = destroyItems;
    }

    public int getLostXpPercentage() {
        return lostXpPercentage;
    }

    public void setLostXpPercentage(int lostXpPercentage) {
        this.lostXpPercentage = lostXpPercentage;
    }

    public boolean isDestroyXp() {
        return destroyXp;
    }

    public void setDestroyXp(boolean destroyXp) {
        this.destroyXp = destroyXp;
    }

    /**
     * Returns the handler used to process items that would normally be dropped on death, or {@code null}
     * to use the default behaviour ({@code player.drop}).
     * Grave mods can set this to store items in a grave block instead.
     */
    @Nullable
    public Consumer<ItemStack> getItemDropHandler() {
        return itemDropHandler;
    }

    public void setItemDropHandler(@Nullable Consumer<ItemStack> itemDropHandler) {
        this.itemDropHandler = itemDropHandler;
    }

    /**
     * Registers an additional list of item slots to be processed by the death penalty
     * (e.g. a backpack mod's inventory or Curios slots).
     */
    public void addItemList(List<ItemStack> items) {
        this.additionalItemLists.add(items);
    }

    public List<List<ItemStack>> getAdditionalItemLists() {
        return additionalItemLists;
    }

    /**
     * Sets a predicate that controls which items are affected by the penalty.
     * Return {@code false} to skip an item entirely. Defaults to always {@code true}.
     */
    public void setItemFilter(Predicate<ItemStack> itemFilter) {
        this.itemFilter = itemFilter;
    }

    public Predicate<ItemStack> getItemFilter() {
        return itemFilter;
    }
}
