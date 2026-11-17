package insane96mcp.insanesurvivaloverhaul.module.mobs.villager;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

@LoadFeature(module = ISOModules.MOBS, description = "Villagers can only be obtained by curing zombie villagers: no breeding, no nitwits, conversion to zombie villager on every difficulty, cure with the items in the `insanesurvivaloverhaul:zombie_villager_cure_items` item tag. Requires the villagers overhaul data pack (VillagersCommon).")
public class Villagers extends Feature {
    public static final TagKey<Item> CURE_ITEMS = ISOItemTagsProvider.create("zombie_villager_cure_items");

    @Config(description = "If true, villagers killed by zombies always turn into zombie villagers, whatever the difficulty.")
    public static Boolean alwaysConvertZombie = true;
    @Config(min = 1, description = "Minimum ticks it takes to cure a zombie villager. Vanilla is 3600.")
    public static Integer cureDurationMin = 3600;
    @Config(min = 1, description = "Maximum ticks it takes to cure a zombie villager. Vanilla is 6000.")
    public static Integer cureDurationMax = 6000;
    @Config(description = "If true, cured nitwits become unemployed villagers that can take a job.")
    public static Boolean noNitwits = true;
    @Config(description = "If true, zombie villagers placed by structures (e.g. abandoned villages) get infinite Fire Resistance, so they don't burn before being found.")
    public static Boolean fireResistantStructureZombieVillagers = true;

    /**
     * The feature does nothing when the villagers overhaul data pack is disabled.
     */
    public static boolean isActive() {
        return isEnabled(Villagers.class) && VillagersCommon.isOverhaulEnabled();
    }

    public static boolean canVillagersBreed() {
        return !isActive();
    }

    public static boolean shouldAlwaysConvertZombie() {
        return isActive() && alwaysConvertZombie;
    }

    public static int getCureDuration(RandomSource random, int vanillaDuration) {
        if (!isActive())
            return vanillaDuration;
        int min = Math.min(cureDurationMin, cureDurationMax);
        int max = Math.max(cureDurationMin, cureDurationMax);
        return min + random.nextInt(max - min + 1);
    }

    public static void onZombieVillagerStructureSpawn(ZombieVillager zombieVillager) {
        if (!isActive()
                || !fireResistantStructureZombieVillagers)
            return;
        zombieVillager.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0, true, false));
    }

    @SubscribeEvent
    public void onCured(LivingConversionEvent.Post event) {
        if (!isActive()
                || !noNitwits
                || !(event.getEntity() instanceof ZombieVillager)
                || !(event.getOutcome() instanceof Villager villager)
                || !(villager.level() instanceof ServerLevel level)
                || villager.getVillagerData().getProfession() != VillagerProfession.NITWIT)
            return;

        villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.NONE));
        //The brain was built with the nitwit profile, which never looks for a job site
        villager.refreshBrain(level);
    }
}
