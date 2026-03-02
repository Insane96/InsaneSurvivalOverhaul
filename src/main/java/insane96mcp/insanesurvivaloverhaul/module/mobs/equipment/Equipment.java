package insane96mcp.insanesurvivaloverhaul.module.mobs.equipment;

import insane96mcp.insanelib.core.JsonFeature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanelib.data.ObjTag;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

import java.util.ArrayList;
import java.util.List;

@LoadFeature(module = ISOModules.MOBS, description = "Changes to mobs equipment")
public class Equipment extends JsonFeature {

    public static final ArrayList<EquipmentDropChance> EQUIPMENT_DROP_CHANCES_DEFAULT = new ArrayList<>(List.of(
            new EquipmentDropChance(ObjTag.tagOf(ResourceLocation.withDefaultNamespace("skeletons"), Registries.ENTITY_TYPE), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(ObjTag.objOf(ResourceLocation.withDefaultNamespace("zombie"), Registries.ENTITY_TYPE), EquipmentSlot.OFFHAND),
            new EquipmentDropChance(ObjTag.objOf(ResourceLocation.withDefaultNamespace("zombified_piglin"), Registries.ENTITY_TYPE), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(ObjTag.objOf(ResourceLocation.withDefaultNamespace("piglin"), Registries.ENTITY_TYPE), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(ObjTag.objOf(ResourceLocation.withDefaultNamespace("piglin_brute"), Registries.ENTITY_TYPE), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(ObjTag.objOf(ResourceLocation.withDefaultNamespace("drowned"), Registries.ENTITY_TYPE), EquipmentSlot.OFFHAND),
            new EquipmentDropChance(ObjTag.objOf(ResourceLocation.withDefaultNamespace("zombie_villager"), Registries.ENTITY_TYPE), EquipmentSlot.OFFHAND),
            new EquipmentDropChance(ObjTag.objOf(ResourceLocation.withDefaultNamespace("vex"), Registries.ENTITY_TYPE), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(ObjTag.objOf(ResourceLocation.withDefaultNamespace("pillager"), Registries.ENTITY_TYPE), EquipmentSlot.MAINHAND),
            new EquipmentDropChance(ObjTag.objOf(ResourceLocation.parse("progressivebosses:wither_minion"), Registries.ENTITY_TYPE), EquipmentSlot.MAINHAND)
    ));
    public static final ArrayList<EquipmentDropChance> equipmentDropChances = new ArrayList<>();
    @Config(description = "Set the drop chance for mobs equipment.")
    public static Double dropChance = 0.5d;
    @Config(description = "Set the drop chance for mobs equipment when spawned from spawners.")
    public static Double dropChanceFromSpawners = 0.2d;
    @Config(min = 0, max = 1, description = "Max durability of items dropped by mobs. This also fixes https://bugs.mojang.com/browse/MC-136374. Setting to 0 will disable this feature.")
    public static Double maxDurability = 0.6d;
    @Config(description = "All drops from mobs will be disenchanted.")
    public static Boolean disenchantEquipment = false;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
        this.getJsonConfigs().add(new JsonConfig<>("custom_drop_chances.json", equipmentDropChances, EQUIPMENT_DROP_CHANCES_DEFAULT, EquipmentDropChance.LIST_TYPE));
    }

    @Override
    public String getModConfigFolder() {
        return InsaneSO.CONFIG_FOLDER;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMobSpawn(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof Mob entity))
            return;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            boolean customDropChance = false;
            for (EquipmentDropChance edc : equipmentDropChances) {
                if (edc.entity.matches(entity.getType()) && edc.slot.equals(slot)) {
                    edc.apply(entity);
                    customDropChance = true;
                }
            }
            if (!customDropChance) {
                if (entity.getSpawnType() == MobSpawnType.SPAWNER)
                    entity.setDropChance(slot, dropChanceFromSpawners.floatValue());
                else
                    entity.setDropChance(slot, dropChance.floatValue());
            }
        }
    }

    @SubscribeEvent
    public void onLivingDrop(LivingDropsEvent event) {
        if (!this.isEnabled()
                || !disenchantEquipment
                || event.getEntity() instanceof Player)
            return;

        event.getDrops().forEach(itemEntity -> itemEntity.getItem().remove(DataComponents.ENCHANTMENTS));
    }
}
