package insane96mcp.iguanatweaksreborn.module.items.repairkit;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class RepairKitItem extends Item {
    public RepairKitItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.getTag() == null)
            return super.getName(stack);
        return Component.translatable(this.getDescriptionId(stack), ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(stack.getTag().getString("repair_material"))).getName(stack).getString());
    }
}
