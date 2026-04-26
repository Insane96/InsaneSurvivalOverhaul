package insane96mcp.insanesurvivaloverhaul.module.mobs;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@LoadFeature(module = ISOModules.MOBS)
public class MiscMobs extends Feature {
    public static final TagKey<EntityType<?>> PASSIVE_REGEN = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.id("passive_regen"));

    //TODO JsonFeature
    @Config(min = 0, description = "1 in X chance each tick for mobs in the `insanesurvivaloverhaul:passive_regen` entity type tag to regain 1 health. Set to 0 to disable")
    public static Integer passiveRegenChance = 200;

    @Config(description = "Instead of dealing magic and melee damage, guardians now only deal magic damage equal to their attack_damage attribute")
    public static Boolean guardianMagicDamageOnly = true;

    @Config(description = "Iron golems no longer deal randomized damage, but fixed attack_damage attribute damage instead")
    public static Boolean fixedIronGolemDamage = true;

    @SubscribeEvent
    public void onLivingTick(EntityTickEvent.Pre event) {
        if (!this.isEnabled()
                || passiveRegenChance == 0
                || !event.getEntity().getType().is(PASSIVE_REGEN)
                || !(event.getEntity() instanceof LivingEntity livingEntity))
            return;

        if (livingEntity.getRandom().nextInt(passiveRegenChance) == 0)
            livingEntity.heal(1f);
    }
}
