package insane96mcp.iguanatweaksreborn.module.items.altimeter;


import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryObject;

@LoadFeature(module = Modules.Ids.ITEMS, description = "Check your altitude. Disables itself if Caverns and Chasms is present")
public class Altimeter extends Feature {
	public static final RegistryObject<Item> ITEM = ISORegistries.ITEMS.register("altimeter", () -> new AltimeterItem(new Item.Properties()));

	public static Boolean tooltip = true;

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && !ModList.get().isLoaded("caverns_and_chasms");
    }

    @SubscribeEvent
	public void onTooltip(ItemTooltipEvent event) {
		if (!isEnabled(Altimeter.class)
				|| !tooltip
				|| !event.getItemStack().is(ITEM.get())
				|| event.getEntity() == null)
			return;

		event.getToolTip().add(Component.translatable("hud_info.depth", event.getEntity().getBlockY()).withStyle(ChatFormatting.GRAY));
	}
}