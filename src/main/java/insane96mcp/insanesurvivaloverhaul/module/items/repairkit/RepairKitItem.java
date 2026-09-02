package insane96mcp.insanesurvivaloverhaul.module.items.repairkit;

import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RepairKitItem extends Item {
    public RepairKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation material = stack.get(ISORegistries.REPAIR_KIT_MATERIAL.get());
        if (material == null)
            return super.getName(stack);

        Item materialItem = BuiltInRegistries.ITEM.get(material);
        return Component.translatable(this.getDescriptionId(stack) + ".material", Component.translatable(materialItem.getDescriptionId()));
    }
}
