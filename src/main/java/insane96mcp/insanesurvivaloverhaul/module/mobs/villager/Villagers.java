package insane96mcp.insanesurvivaloverhaul.module.mobs.villager;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import insane96mcp.insanelib.world.effect.ILMobEffect;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.data.generator.ISOItemTagsProvider;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.MOBS, description = "Villagers can only be obtained by curing zombie villagers: no breeding, no nitwits, conversion to zombie villager on every difficulty, cure with the items in the `insanesurvivaloverhaul:zombie_villager_cure_items` item tag, convalescence after the cure, prices that only rise (demand and mistreatment, no discounts), emeralds from advancements that give experience. Requires the villagers overhaul data pack (VillagersCommon).")
public class Villagers extends Feature {
    public static final TagKey<Item> CURE_ITEMS = ISOItemTagsProvider.create("zombie_villager_cure_items");
    /**
     * Given to cured villagers: no trading, no work and no restock, Slowness. Its presence is the convalescence state.
     */
    public static final DeferredHolder<MobEffect, MobEffect> CONVALESCENCE = ISORegistries.MOB_EFFECTS.register("convalescence", () -> new ILMobEffect(MobEffectCategory.HARMFUL, 0x799c65, false)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, InsaneSO.id("villagers/convalescence"), -0.15F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    @Config(description = "If true, villagers killed by zombies always turn into zombie villagers, whatever the difficulty.")
    public static Boolean alwaysConvertZombie = true;
    @Config(min = 1, description = "Ticks it takes to cure a zombie villager. Vanilla is 3600-6000.")
    public static MinMaxConfig cureDuration = new MinMaxConfig(3600, 6000);
    @Config(description = "If true, cured nitwits become unemployed villagers that can take a job.")
    public static Boolean noNitwits = true;
    @Config(min = 0, description = "Ticks of convalescence after a zombie villager is cured: the villager doesn't trade, work or restock, and is slowed. 0 disables it.")
    public static Integer convalescenceDuration = 24000;
    @Config(description = "If true, zombie villagers placed by structures (e.g. abandoned villages) get infinite Fire Resistance, so they don't burn before being found.")
    public static Boolean fireResistantStructureZombieVillagers = true;
    @Config(min = 0d, max = 1d, description = "When a trade's demand is above 0 and goes down at a restock, the decrease is multiplied by this value, so expensive trades go back to the base price more slowly. 1 is vanilla.")
    public static Double positiveDemandDecayMultiplier = 0.6d;
    @Config(description = "If true, a trade's demand can't go below -max uses, so an unused trade can always get more expensive again.")
    public static Boolean clampNegativeDemand = true;
    @Config(description = "If true, villagers get 1 trading experience as soon as they take a job, so they keep it (and their trades) even if they lose their job site.")
    public static Boolean lockTrades = true;
    @Config(min = 0d, description = "Multiplier to the experience villagers need to level up.")
    public static Double experienceToLevelUpMultiplier = 1d;
    @Config(min = 0, description = "Advancements that give experience as a reward also give 1 emerald every this much experience (rounded down). 0 disables it.")
    public static Integer advancementExperiencePerEmerald = 25;

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
        return cureDuration.getIntRandBetween(random);
    }

    /**
     * Only negative reputation raises the prices: no discounts from positive reputation (e.g. the cure) or Hero of the Village.
     * Only the negative gossip types are summed, otherwise the permanent positive gossip of the cure would cancel any mistreatment.
     * Returns false if vanilla must run instead.
     */
    public static boolean updateSpecialPrices(Villager villager, Player player) {
        if (!isActive())
            return false;
        int negativeReputation = villager.getGossips().getReputation(player.getUUID(), gossipType -> gossipType.weight < 0);
        if (negativeReputation < 0) {
            for (MerchantOffer offer : villager.getOffers())
                offer.addToSpecialPriceDiff(Math.max(0, -Mth.floor(negativeReputation * offer.getPriceMultiplier())));
        }
        return true;
    }

    /**
     * Vanilla demand update ({@code demand + uses - (maxUses - uses)}), with the decrease multiplied by {@link #positiveDemandDecayMultiplier} while demand is above 0
     * (stopping at 0 for this restock) and the negative demand clamped at {@code -maxUses}.
     */
    public static int getUpdatedDemand(int demand, int uses, int maxUses) {
        int change = uses - (maxUses - uses);
        int newDemand;
        if (change < 0 && demand > 0)
            newDemand = Math.max(0, demand - Mth.ceil(-change * positiveDemandDecayMultiplier));
        else
            newDemand = demand + change;
        if (clampNegativeDemand)
            newDemand = Math.max(newDemand, -maxUses);
        return newDemand;
    }

    /**
     * Gives 1 experience to a villager taking a job, so it keeps it.
     */
    public static void lockTrades(Villager villager, VillagerData newData) {
        if (!isActive()
                || !lockTrades
                || villager.level().isClientSide()
                || newData.getProfession() == VillagerProfession.NONE
                || newData.getProfession() == villager.getVillagerData().getProfession()
                || villager.getVillagerXp() > 0)
            return;
        villager.setVillagerXp(1);
    }

    public static int getExperienceToLevelUp(int vanillaXp) {
        if (!isActive())
            return vanillaXp;
        return (int) (vanillaXp * experienceToLevelUpMultiplier);
    }

    public static void onZombieVillagerStructureSpawn(ZombieVillager zombieVillager) {
        if (!isActive()
                || !fireResistantStructureZombieVillagers)
            return;
        zombieVillager.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, MobEffectInstance.INFINITE_DURATION, 0, true, false));
    }

    /**
     * Whether the villager is still recovering from the cure. The effect stays the state even if the overhaul is disabled meanwhile.
     */
    public static boolean isConvalescent(Villager villager) {
        return villager.hasEffect(CONVALESCENCE);
    }

    @SubscribeEvent
    public void onCuredConvalescence(LivingConversionEvent.Post event) {
        if (!isActive()
                || convalescenceDuration <= 0
                || !(event.getEntity() instanceof ZombieVillager)
                || !(event.getOutcome() instanceof Villager villager)
                || villager.level().isClientSide())
            return;

        villager.addEffect(new MobEffectInstance(CONVALESCENCE, convalescenceDuration));
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

    /**
     * Gives emeralds for the experience reward of the earned advancement, so it works for vanilla, ISO and mod advancements alike.
     */
    @SubscribeEvent
    public void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!isActive()
                || advancementExperiencePerEmerald <= 0
                || event.getEntity().level().isClientSide())
            return;

        int emeralds = event.getAdvancement().value().rewards().experience() / advancementExperiencePerEmerald;
        if (emeralds <= 0)
            return;
        ItemHandlerHelper.giveItemToPlayer(event.getEntity(), new ItemStack(Items.EMERALD, emeralds));
    }
}
