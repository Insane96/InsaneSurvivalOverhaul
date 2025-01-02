package insane96mcp.iguanatweaksreborn.module.mobs.equipment;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalTweaks;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.JsonFeature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.data.IdTagMatcher;
import insane96mcp.insanelib.setup.ILStrings;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@Label(name = "Equipment", description = "Changes to mobs equipment")
@LoadFeature(module = Modules.Ids.MOBS)
public class Equipment extends JsonFeature {

    public static final ArrayList<EquipmentDropChance> EQUIPMENT_DROP_CHANCES_DEFAULT = new ArrayList<>(List.of(
            new EquipmentDropChance(IdTagMatcher.newTag("minecraft:skeletons"), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(IdTagMatcher.newId("minecraft:zombie"), EquipmentSlot.OFFHAND),
            new EquipmentDropChance(IdTagMatcher.newId("minecraft:zombified_piglin"), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(IdTagMatcher.newId("minecraft:piglin"), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(IdTagMatcher.newId("minecraft:piglin_brute"), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(IdTagMatcher.newId("minecraft:drowned"), EquipmentSlot.OFFHAND),
            new EquipmentDropChance(IdTagMatcher.newId("minecraft:zombie_villager"), EquipmentSlot.OFFHAND),
            new EquipmentDropChance(IdTagMatcher.newId("minecraft:vex"), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(IdTagMatcher.newId("minecraft:pillager"), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(IdTagMatcher.newId("progressivebosses:wither_minion"), EquipmentSlot.MAINHAND)
    ));
    public static final ArrayList<EquipmentDropChance> equipmentDropChances = new ArrayList<>();
    @Config
    @Label(name = "Drop chance", description = "Set the drop chance for mobs equipment.")
    public static Double dropChance = 0.5d;
    @Config
    @Label(name = "Drop chance from Spawners", description = "Set the drop chance for mobs equipment when spawned from spawners.")
    public static Double dropChanceFromSpawners = 0.2d;
    @Config(min = 0, max = 1)
    @Label(name = "Max durability", description = "Max durability of items dropped by mobs. This also fixes https://bugs.mojang.com/browse/MC-136374. Setting to 0 will disable this feature.")
    public static Double maxDurability = 0.6d;
    @Config
    @Label(name = "Disenchant equipment", description = "All drops from mobs will be disenchanted.")
    public static Boolean disenchantEquipment = false;

    public Equipment(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        JSON_CONFIGS.add(new JsonConfig<>("custom_drop_chances.json", equipmentDropChances, EQUIPMENT_DROP_CHANCES_DEFAULT, EquipmentDropChance.LIST_TYPE));
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSurvivalTweaks.CONFIG_FOLDER;
    }

    @Override
    public void loadJsonConfigs() {
        if (!this.isEnabled())
            return;
        super.loadJsonConfigs();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMobSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob entity))
            return;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            boolean customDropChance = false;
            for (EquipmentDropChance edc : equipmentDropChances) {
                if (edc.entity.matchesEntity(entity) && edc.slot.equals(slot)) {
                    edc.apply(entity);
                    customDropChance = true;
                }
            }
            if (!customDropChance) {
                if (entity.getPersistentData().getBoolean(ILStrings.Tags.SPAWNED_FROM_SPAWNER))
                    entity.setDropChance(slot, dropChanceFromSpawners.floatValue());
                else
                    entity.setDropChance(slot, dropChance.floatValue());
            }
        }
    }

    @SubscribeEvent
    public void onLivingDrop(LivingDropsEvent event) {
        if (!this.isEnabled()
                || !disenchantEquipment)
            return;

        event.getDrops().forEach(itemEntity -> {
            if (!itemEntity.getItem().is(Items.ENCHANTED_BOOK))
                itemEntity.getItem().removeTagKey("Enchantments");
        });
    }
}
