package insane96mcp.insanesurvivaloverhaul.module.misc.beacon;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ISOBeaconMenu extends AbstractContainerMenu {
    private final ContainerData beaconData;
    private final ContainerLevelAccess access;

    public ISOBeaconMenu(int containerId, Inventory playerInventory) {
        this(containerId, new SimpleContainerData(ISOBeaconBlockEntity.DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public ISOBeaconMenu(int containerId, ContainerData beaconData, ContainerLevelAccess access) {
        super(BeaconFeature.BEACON_MENU_TYPE.get(), containerId);
        checkContainerDataCount(beaconData, ISOBeaconBlockEntity.DATA_COUNT);
        this.beaconData = beaconData;
        this.access = access;
        this.addDataSlots(beaconData);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, BeaconFeature.BEACON.block().get());
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
        this.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Nullable
    public Holder<MobEffect> getEffect() {
        return BeaconMenu.decodeEffect(this.beaconData.get(ISOBeaconBlockEntity.DATA_EFFECT));
    }

    public int getAmplifier() {
        return this.beaconData.get(ISOBeaconBlockEntity.DATA_AMPLIFIER);
    }

    public int getLayers() {
        return this.beaconData.get(ISOBeaconBlockEntity.DATA_LAYERS);
    }

    public int getRange() {
        return this.beaconData.get(ISOBeaconBlockEntity.DATA_RANGE);
    }

    /**
     * Chiamato dal packet lato server: scarta le selezioni che la piramide attuale non consente.
     */
    public void updateEffect(Holder<MobEffect> effect, int amplifier) {
        BeaconEffect beaconEffect = BeaconFeature.getBeaconEffect(effect);
        int layers = this.getLayers();
        if (beaconEffect == null
                || !beaconEffect.isAvailableAt(layers)
                || amplifier != beaconEffect.clampAmplifier(amplifier, layers))
            return;

        this.beaconData.set(ISOBeaconBlockEntity.DATA_EFFECT, BeaconMenu.encodeEffect(effect));
        this.beaconData.set(ISOBeaconBlockEntity.DATA_AMPLIFIER, amplifier);
        this.access.execute(Level::blockEntityChanged);
    }
}
