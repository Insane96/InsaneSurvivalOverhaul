package insane96mcp.insanesurvivaloverhaul.module.combat;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.misc.Packs;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.COMBAT)
public class MiscCombat extends Feature {
    /**
     * Holds the armor_rework movement speed penalty (a negative value). Kept off the real movement_speed
     * attribute so it can be applied to it only while sprinting, see LivingEntityMixin_MiscCombat.
     */
    public static final DeferredHolder<Attribute, Attribute> SPRINT_SLOWDOWN_ATTRIBUTE = ISORegistries.ATTRIBUTES.register("sprint_slowdown", () -> new RangedAttribute("attribute.name.sprint_slowdown", 0d, -1024d, 1024d));
    public static final ResourceLocation SPRINT_SLOWDOWN_ID = InsaneSO.id("sprint_slowdown");

    @Config(description = "If enabled, tools will not take 2 damage when used to hurt entities")
    public static Boolean oneDamageForToolAttacking = true;
    @Config(description = "Rework Sweeping attack. Sweeping is no longer on swords, instead it's on hoes. Also, the sweeping attack deals full damage and the range is increased. A data pack is enabled that removes the sweeping enchantment.")
    public static Boolean sweepingOverhaul = true;
    @Config(description = "In vanilla, if you attack as soon as you just attacked you already deal 20% of the full damage. This changes that to 0%.")
    public static Boolean noDamageWhenSpamming = true;

    @Config(description = "Enables a data pack that reworks armor.")
    public static Boolean armorReworkDataPack = true;
    @Config(description = "Enables a data pack that reworks tools and weapons.")
    public static Boolean toolsAndWeaponsDataPack = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("armor_rework", "Insane's Survival Overhaul Armor Rework", () -> this.isEnabled() && !Packs.disableAllDataPacks && armorReworkDataPack);
        InsaneSO.addServerPack("tools_and_weapons_rework", "Insane's Survival Overhaul Tools and Weapons Rework", () -> this.isEnabled() && !Packs.disableAllDataPacks && toolsAndWeaponsDataPack);
        InsaneSO.addServerPack("sweeping_overhaul", "Insane's Survival Overhaul Sweeping Overhaul", () -> this.isEnabled() && !Packs.disableAllDataPacks && sweepingOverhaul);
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        if (sweepingOverhaul) {
            ItemAbilities.DEFAULT_SWORD_ACTIONS.remove(ItemAbilities.SWORD_SWEEP);
            ItemAbilities.DEFAULT_HOE_ACTIONS.add(ItemAbilities.SWORD_SWEEP);
        }
        else {
            ItemAbilities.DEFAULT_SWORD_ACTIONS.add(ItemAbilities.SWORD_SWEEP);
            ItemAbilities.DEFAULT_HOE_ACTIONS.remove(ItemAbilities.SWORD_SWEEP);
        }
    }

    @SubscribeEvent
    public void onCritDisableSweep(CriticalHitEvent event) {
        if (!this.isEnabled()
                || !sweepingOverhaul)
            return;

        event.setDisableSweep(false);
    }

    public static boolean noDamageWhenSpamming() {
        return isEnabled(MiscCombat.class) && noDamageWhenSpamming;
    }

    public static void addAttribute(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            if (!event.has(entityType, SPRINT_SLOWDOWN_ATTRIBUTE))
                event.add(entityType, SPRINT_SLOWDOWN_ATTRIBUTE);
        }
    }
}