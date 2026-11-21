package insane96mcp.insanesurvivaloverhaul.module.animals.equineattributes;

import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.MinMaxConfig;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.LlamaAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = ISOModules.ANIMALS, description = "Horses, donkeys, mules, llamas and undead horses have fixed attributes (no more genetics). Configurable in equine_attributes.json, where values can also be random ranges.")
public class EquineAttributes extends JsonFeature {
    /** Set once the attributes have been rolled for an entity. Persistent data survives dimension changes, so random ranges aren't rolled again */
    public static final String APPLIED_TAG = InsaneSO.MOD_ID + ":equine_attributes_applied";

    public static final ArrayList<EquineStats> EQUINE_STATS_DEFAULT = new ArrayList<>(List.of(
            EquineStats.of("minecraft:horse", 20, 0.3375, 0.7, 1),
            EquineStats.of("minecraft:donkey", 20, 0.225, 0.5, 1),
            EquineStats.of("minecraft:mule", 20, 0.225, 0.5, 1),
            EquineStats.of("minecraft:skeleton_horse", 20, 0.4, 0.7, 1),
            EquineStats.of("minecraft:zombie_horse", 30, 0.3, 0.5, 1),
            EquineStats.of("minecraft:llama", 20, 0.175, 0.5, 1).withStrength(3, 0.5f),
            EquineStats.of("minecraft:trader_llama", 20, 0.175, 0.5, 1).withStrength(3, 0.5f)
    ));
    public static final ArrayList<EquineStats> equineStats = new ArrayList<>();

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        this.getJsonConfigs().add(new JsonConfig<>("equine_attributes.json", equineStats, EQUINE_STATS_DEFAULT, EquineStats.LIST_TYPE));
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSO.CONFIG_FOLDER;
    }

    /**
     * Returns the first entry matching the entity type, or null if none matches
     */
    @Nullable
    public static EquineStats getStats(EntityType<?> entityType) {
        for (EquineStats stats : equineStats) {
            if (stats.entity.matches(entityType))
                return stats;
        }
        return null;
    }

    /**
     * Applied when the entity joins the level, so it covers natural spawns, spawn eggs, breeding (after vanilla offspring genetics) and horses already in the world.
     * The first time the values are rolled; afterward they are rolled again only if out of the configured range (e.g. after a config change).
     */
    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide
                || !(event.getEntity() instanceof AbstractHorse horse))
            return;

        EquineStats stats = getStats(horse.getType());
        if (stats == null)
            return;

        boolean roll = !horse.getPersistentData().getBoolean(APPLIED_TAG);
        applyAttribute(horse, Attributes.MAX_HEALTH, stats.health, roll);
        applyAttribute(horse, Attributes.MOVEMENT_SPEED, stats.movementSpeed, roll);
        applyAttribute(horse, Attributes.JUMP_STRENGTH, stats.jumpStrength, roll);
        applyAttribute(horse, Attributes.STEP_HEIGHT, stats.stepHeight, roll);
        //New entities are created with the base max health (53 for horses), so this also makes them spawn at full health
        if (horse.getHealth() > horse.getMaxHealth())
            horse.setHealth(horse.getMaxHealth());
        horse.getPersistentData().putBoolean(APPLIED_TAG, true);

        //Strength of new llamas is set on spawn and breeding (see LlamaMixin_EquineAttributes), here only out of range values are fixed
        if (horse instanceof Llama llama && stats.strength != null) {
            int maxStrength = stats.extraStrengthChance != null && stats.extraStrengthChance > 0f ? 5 : (int) stats.strength.max;
            if (llama.getStrength() < stats.strength.min || llama.getStrength() > maxStrength)
                ((LlamaAccessor) llama).invokeSetStrength(stats.strength.getIntRandBetween(llama.getRandom()));
        }
    }

    private static void applyAttribute(AbstractHorse horse, Holder<Attribute> attribute, @Nullable MinMaxConfig range, boolean roll) {
        if (range == null)
            return;
        AttributeInstance instance = horse.getAttribute(attribute);
        if (instance == null)
            return;
        double baseValue = instance.getBaseValue();
        if (!roll && baseValue >= range.min && baseValue <= range.max)
            return;
        instance.setBaseValue(range.getRandBetween(horse.getRandom()));
    }

    /**
     * Strength of a bred llama: the strongest parent's strength, with a chance of +1 (vanilla clamps it to 5).
     * Returns the vanilla strength if no entry or strength is configured for the baby.
     */
    public static int getBredLlamaStrength(Llama baby, Llama parentA, Llama parentB, int vanillaStrength) {
        EquineStats stats = getStats(baby.getType());
        if (stats == null || stats.strength == null)
            return vanillaStrength;
        int strength = Math.max(parentA.getStrength(), parentB.getStrength());
        if (stats.extraStrengthChance != null && parentA.getRandom().nextFloat() < stats.extraStrengthChance)
            strength++;
        return strength;
    }
}
