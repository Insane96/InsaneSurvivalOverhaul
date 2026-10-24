package insane96mcp.insanesurvivaloverhaul.module.items.pouch;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.criterion.OverweightPouchCarryTrigger;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

@LoadFeature(module = ISOModules.ITEMS, description = "A new item that can let you carry more stuff around.", enabledByDefault = false)
public class Pouch extends Feature {
    public static final TagKey<Item> POUCH_LIKE_WEIGHT = ISOItemTagsProvider.create("pouch_like_weight");

	public static final ResourceLocation POUCH_WEIGHT_PENALTY_ID = InsaneSO.id("pouch_weight_penalty");

	public static final DeferredHolder<Item, PouchItem> ITEM = ISORegistries.ITEMS.register("pouch", () -> new PouchItem(new Item.Properties().stacksTo(1)));
	public static final Supplier<OverweightPouchCarryTrigger> OVERWEIGHT_POUCH_CARRY =
			ISORegistries.registerTrigger("overweight_pouch_carry", OverweightPouchCarryTrigger::new);

	@Config(min = 0)
	public static Integer maxPouchWithoutSlowdown = 2;

	@Config(min = 0d, max = 1d, description = "When you have > 'Max pouch without slowdown' this is the base value for the slowdown. The slowdown is calculated as (pouch above 'Max pouch' ^ 2 * this)")
	public static Double baseSlownessPerPouchOverMax = 0.03d;

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (!this.isEnabled()
				|| baseSlownessPerPouchOverMax == 0d) {
			movementSpeed.removeModifier(POUCH_WEIGHT_PENALTY_ID);
			return;
		}

		int pouchesInInventory = ContainerHelper.clearOrCountMatchingItems(player.getInventory(), stack -> stack.is(ITEM.get()) || stack.is(POUCH_LIKE_WEIGHT), 0, true);
		double slowness = 0d;
		if (pouchesInInventory > maxPouchWithoutSlowdown)
			slowness = ((pouchesInInventory - maxPouchWithoutSlowdown) * baseSlownessPerPouchOverMax) * Math.max(1, pouchesInInventory - maxPouchWithoutSlowdown);
		//Only touch the modifier when the slowness changes
		AttributeModifier currentModifier = movementSpeed.getModifier(POUCH_WEIGHT_PENALTY_ID);
		double currentSlowness = currentModifier == null ? 0d : -currentModifier.amount();
		if (currentSlowness != slowness) {
			movementSpeed.removeModifier(POUCH_WEIGHT_PENALTY_ID);
			if (slowness > 0d)
				MCUtils.applyModifier(player, Attributes.MOVEMENT_SPEED, POUCH_WEIGHT_PENALTY_ID, -slowness, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, false);
		}
		if (slowness > 0d && player.tickCount % 20 == 4 && player instanceof ServerPlayer serverPlayer)
			OVERWEIGHT_POUCH_CARRY.get().trigger(serverPlayer);
	}
}