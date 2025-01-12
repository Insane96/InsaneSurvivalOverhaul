package insane96mcp.iguanatweaksreborn.module.world.explosionoverhaul;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.network.message.ExplodeParticles;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Explosion Overhaul", description = "Various changes to explosions, like higher knockback and getting hit when behind blown up blocks.")
@LoadFeature(module = Modules.Ids.WORLD)
public class ExplosionOverhaul extends Feature {
	public static final TagKey<EntityType<?>> KNOCKBACK_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "explosion_knockback_blacklist"));
	public static final TagKey<EntityType<?>> ENTITY_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(InsaneSurvivalOverhaul.MOD_ID, "explosion_entity_blacklist"));
	public static final GameRules.Key<GameRules.BooleanValue> RULE_MOBGRIEFING = GameRules.register("iguanatweaks:explosionMobGriefing", GameRules.Category.MISC, GameRules.BooleanValue.create(true));

	@Config
	@Label(name = "Disable Explosion Randomness", description = "Vanilla Explosions use a random number that changes the explosion power. With this enabled the ray strength will be as the explosion size.")
	public static Boolean disableExplosionRandomness = true;
	@Config
	@Label(name = "Enable Poof Particles", description = "Somewhere around 1.15 Mojang (for performance issues) removed the poof particles from Explosions. Keep them disabled if you have a low end PC.\n" +
			"These particles aren't shown when explosion power is <= 1")
	public static Boolean enablePoofParticles = true;
	@Config
	@Label(name = "Disable Emitter Particles", description = "Removes the particles spawned by the explosion.")
	public static Boolean disableEmitterParticles = true;
	@Config
	@Label(name = "Knockback Scales With Size", description = "While enabled knockback is greatly increased by explosion size")
	public static Boolean knockbackScalesWithSize = true;
	@Config
	@Label(name = "Explosions at Half Entity", description = "Explosions will start from the middle of the entity instead of feets.")
	public static Boolean explosionAtHalfEntity = true;
	@Config
	@Label(name = "Explosion Affect Just Spawned Entities", description = "Explosions affect even entities spawned by the explosions, like TnTs or chests content. BE AWARE that containers content will get destroyed.")
	public static Boolean affectJustSpawnedEntities = false;
	@Config
	@Label(name = "Enable Flying Blocks", description = "EXPERIMENTAL! This will make explosion blast blocks away. Blocks that can't land will drop the block as a TNT would have destroyed it.")
	public static Boolean enableFlyingBlocks = false;
	@Config
	@Label(name = "Creeper collateral", description = "If true, creepers explosions will drop no blocks.")
	public static Boolean creeperCollateral = false;
	@Config
	@Label(name = "Explosion Damage calculation multiplier", description = "Number in the explosion damage calculation. Vanilla is 7. Higher = More damage")
	public static Double explosionDamageCalculationMultiplier = 6d;
	@Config
	@Label(name = "Limit explosion size", description = "Disabled if set to 0.")
	public static Integer limitExplosionSize = 12;
	@Config
	@Label(name = "Don't stack drops")
	public static Boolean dontStackDrops = true;

	public ExplosionOverhaul(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void explosionPoofParticles(ExplosionEvent.Detonate event) {
		if (!this.isEnabled()
				|| !enablePoofParticles)
			return;

		Explosion e = event.getExplosion();
		if (e.level instanceof ServerLevel level) {
			if (e instanceof ISOExplosion isoExplosion) {
				if (!isoExplosion.poofParticles)
					return;
				ExplodeParticles.sync(level, isoExplosion);
			}
		}
	}

	//Setting the lowest priority so other mods can change explosions params before creating the ITExplosion
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void replaceExplosionWithITRExplosion(ExplosionEvent.Start event) {
		if (!this.isEnabled()
				|| !(event.getLevel() instanceof ServerLevel level)
				|| (event.getExplosion().getExploder() != null && event.getExplosion().getExploder().getType().is(ENTITY_BLACKLIST)))
			return;

		event.setCanceled(true);
		Explosion e = event.getExplosion();
		double y = e.getPosition().y;
		if (e.source != null && explosionAtHalfEntity)
			y += e.source.getBbHeight() / 2d;
		ISOExplosion.explode(level, e.source, e.getDamageSource(), e.damageCalculator, e.getPosition().x, y, e.getPosition().z, e.radius, e.fire, e.blockInteraction, true);
	}

	public static boolean shouldTakeReducedKnockback(Entity entity) {
		if (!(entity instanceof LivingEntity))
			return true;

		return entity.getType().is(KNOCKBACK_BLACKLIST);
	}
}