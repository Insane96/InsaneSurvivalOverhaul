package insane96mcp.iguanatweaksreborn.module.items.pouch;

import insane96mcp.iguanatweaksreborn.data.criterion.ISOTriggers;
import insane96mcp.iguanatweaksreborn.data.generator.ISOItemTagsProvider;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

@LoadFeature(module = Modules.Ids.ITEMS, description = "A new item that can let you carry more stuff around.")
public class Pouch extends Feature {
    public static final TagKey<Item> POUCH_LIKE_WEIGHT = ISOItemTagsProvider.create("pouch_like_weight");

	public static final UUID CRATE_WEIGHT_UUID = UUID.fromString("4ce89c45-a011-43fa-b9a8-7f2bd0ea2fc3");

	public static final RegistryObject<PouchItem> ITEM = ISORegistries.ITEMS.register("pouch", () -> new PouchItem(new Item.Properties().stacksTo(1)));

	@Config(min = 0)
	public static Integer maxPouchWithoutSlowdown = 2;

	@Config(min = 0d, max = 1d, description = "When you have > 'Max pouch without slowdown' this is the base value for the slowdown. The slowdown is calculated as (pouch above 'Max pouch' ^ 2 * this)")
	public static Double baseSlownessPerPouchOverMax = 0.03d;

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (baseSlownessPerPouchOverMax == 0d
				|| event.phase == TickEvent.Phase.START)
			return;

		int pouchesInInventory = ContainerHelper.clearOrCountMatchingItems(event.player.getInventory(), stack -> stack.is(ITEM.get()) || stack.is(POUCH_LIKE_WEIGHT), 0, true);
		event.player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(CRATE_WEIGHT_UUID);
		if (pouchesInInventory > maxPouchWithoutSlowdown) {
			double slowness = ((pouchesInInventory - maxPouchWithoutSlowdown) * baseSlownessPerPouchOverMax) * Math.max(1, pouchesInInventory - maxPouchWithoutSlowdown);
			MCUtils.applyModifier(event.player, Attributes.MOVEMENT_SPEED, CRATE_WEIGHT_UUID, "Crate weight penalty", -slowness, AttributeModifier.Operation.MULTIPLY_BASE, false);
			 if (event.player.tickCount % 20 == 4 && event.player instanceof ServerPlayer serverPlayer)
                 ISOTriggers.OVERWEIGHT_POUCH_CARRY.trigger(serverPlayer);
		}
	}
}