package insane96mcp.insanesurvivaloverhaul.module.movement;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.MOVEMENT)
public final class Sprinting extends Feature {
	public static final ResourceLocation SPRINT_PENALTY_ID = InsaneSO.id("hungry_sprint_penalty");

	/**
	 * Holds the total sprint-only movement speed penalty (a negative value), fed by both this feature's
	 * hunger penalty and armor_rework's armor penalty. Kept off the real movement_speed attribute so it
	 * can be applied to it only while sprinting, see LivingEntityMixin_Sprinting.
	 */
	public static final DeferredHolder<Attribute, Attribute> SPRINT_SLOWDOWN_ATTRIBUTE = ISORegistries.ATTRIBUTES.register("sprint_slowdown", () -> new RangedAttribute("attribute.name.sprint_slowdown", 0d, -1024d, 1024d));
	public static final ResourceLocation SPRINT_SLOWDOWN_ID = InsaneSO.id("sprint_slowdown");

	@Config(min = 0, max = 20, description = "Player can only sprint when have at least this much hunger. Vanilla is 7")
	public static Integer minHunger = 1;
	@Config(min = 0, max = 20, description = "Apply a sprint speed penalty when below this hunger")
	public static Integer speedPenaltyBelowHunger = 7;
	@Config(min = 0, description = "How much less movement speed per hunger below 'Speed Penalty below hunger' sprinting players have. Vanilla gives 0.3 bonus movement speed when sprinting")
	public static Double speedReductionEachHunger = 0.025;

	public static void addAttribute(EntityAttributeModificationEvent event) {
		for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
			if (!event.has(entityType, SPRINT_SLOWDOWN_ATTRIBUTE))
				event.add(entityType, SPRINT_SLOWDOWN_ATTRIBUTE);
		}
	}

	@SubscribeEvent
	public void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (!this.isEnabled()
				|| player.level().isClientSide)
			return;

		MCUtils.removeModifier(player, SPRINT_SLOWDOWN_ATTRIBUTE, SPRINT_PENALTY_ID);
		if (speedPenaltyBelowHunger == 0)
			return;
		double penalty = (speedPenaltyBelowHunger - player.getFoodData().getFoodLevel()) * speedReductionEachHunger;
		if (penalty <= 0d)
			return;
		MCUtils.applyModifier(player, SPRINT_SLOWDOWN_ATTRIBUTE, SPRINT_PENALTY_ID, -penalty, AttributeModifier.Operation.ADD_VALUE, false);
	}
}