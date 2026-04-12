package insane96mcp.insanesurvivaloverhaul.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Fired on NeoForge's event bus before {@link insane96mcp.insanesurvivaloverhaul.module.death.DeathPenalty}
 * processes item and experience loss on player death.
 * <p>
 * Cancel to skip all penalties entirely (e.g. player respawning at an obelisk).
 * Set an {@link #setItemDropHandler itemDropHandler} to redirect dropped items elsewhere (e.g. into a grave block).
 * Mutable fields allow adjusting penalty amounts before processing.
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
}
