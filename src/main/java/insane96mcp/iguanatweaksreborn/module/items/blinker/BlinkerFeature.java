package insane96mcp.iguanatweaksreborn.module.items.blinker;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

@LoadFeature(module = Modules.Ids.ITEMS)
public class BlinkerFeature extends Feature {
    public static final RegistryObject<Item> ITEM = ISORegistries.ITEMS.register("blinker", () -> new BlinkerItem(new Item.Properties().stacksTo(1).durability(512)));

    @SubscribeEvent
    public void onAnvilRepair(AnvilUpdateEvent event) {
        if (!this.isEnabled()
                || !event.getLeft().is(ITEM.get())
                || !event.getRight().is(Items.ENDER_PEARL))
            return;

        ItemStack leftCopy = event.getLeft().copy();
        int maxPearls = Mth.ceil(leftCopy.getDamageValue() / 32f);
        int pearls = Math.min(event.getRight().getCount(), maxPearls);
        leftCopy.setDamageValue(leftCopy.getDamageValue() - (32 * pearls));
        event.setOutput(leftCopy);
        event.setCost(0);
        event.setMaterialCost(pearls);
    }
}