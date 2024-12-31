package insane96mcp.iguanatweaksreborn.module.items;

import insane96mcp.iguanatweaksreborn.data.generator.ITRItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Disabled Items", description = "Make items not able to mine / attack")
@LoadFeature(module = Modules.Ids.ITEMS)
public class DisabledItems extends Feature {

	public static final TagKey<Item> NO_DAMAGE = ITRItemTagsProvider.create("no_damage");
	public static final String NO_DAMAGE_ITEM_LANG = "iguanatweaksreborn.no_damage_item";
	public static final TagKey<Item> NO_EFFICIENCY = ITRItemTagsProvider.create("no_efficiency");
	public static final String NO_EFFICIENCY_ITEM_LANG = "iguanatweaksreborn.no_efficiency_item";

	@Config
	@Label(name = "Add tooltip", description = "If set to true items in the 'no_damage' and 'no_efficiency' item tags will get a tooltip.")
	public static Boolean addTooltip = true;

	public DisabledItems(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void processAttacking(LivingHurtEvent event) {
		if (!this.isEnabled()
				|| !(event.getSource().getDirectEntity() instanceof Player player))
			return;

		ItemStack stack = player.getMainHandItem();
		if (stack.is(NO_DAMAGE)) {
			event.setCanceled(true);
			player.displayClientMessage(Component.translatable(NO_DAMAGE_ITEM_LANG), true);
		}
	}

	@SubscribeEvent
	public void processMining(PlayerEvent.BreakSpeed event) {
		if (!this.isEnabled())
			return;

		Player player = event.getEntity();
		ItemStack stack = player.getMainHandItem();
		if (stack.is(NO_EFFICIENCY)) {
			event.setCanceled(true);
			event.getEntity().displayClientMessage(Component.translatable(NO_EFFICIENCY_ITEM_LANG), true);
		}
	}


	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onTooltip(ItemTooltipEvent event) {
		if (!this.isEnabled()
				|| !addTooltip)
			return;

		ItemStack stack = event.getItemStack();
		if (stack.is(NO_DAMAGE)) {
			event.getToolTip().add(Component.translatable(NO_DAMAGE_ITEM_LANG).withStyle(ChatFormatting.RED));
		}
		if (stack.is(NO_EFFICIENCY)) {
			event.getToolTip().add(Component.translatable(NO_EFFICIENCY_ITEM_LANG).withStyle(ChatFormatting.RED));
		}
	}
}