package insane96mcp.iguanatweaksreborn.module.misc;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.farming.crops.integration.FarmersDelightIntegration;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import vectorwing.farmersdelight.common.Configuration;

@LoadFeature(module = Modules.Ids.MISC, name = "Data / Resource Packs & Integration", description = "Various packs that can be enabled/disabled")
public class Packs extends Feature {

    @Config(description = "If true, no integrated data pack will be loaded")
    public static Boolean disableAllDataPacks = false;

    @Config(description = """
            Changes vanilla torch recipes.
            * Torches can be only made on Campfires early on in the game
            * With shears you can make 3 torches out of coal
            * With Fire Charges you can make them later in the game.""")
    public static Boolean hardcoreTorches = true;

    @Config(description = "Changes vanilla chains recipe. Makes chains easily craftable with nuggets only.")
    public static Boolean cheaperChains = true;

    @Config(description = """
            Minor changes:
            * Cakes now drop when broken and not eaten
            * Dark Prismarine is made easier
            * Clay balls can be crafted from blocks
            * Leads and Sticky Pistons can also be made with Honey
            * Iron Doors can be blasted back to Iron Ingots
            * Dispensers can be made from droppers
            * Levers and glass can now be broken faster with pickaxes, cactus with hoes""")
    public static Boolean miscTweaks = true;

    @Config(description = "Makes redstone components require redstone in their recipe (e.g. Levers, pressure plates, etc).")
    public static Boolean actualRedstoneComponents = true;

    @Config(description = "If true a data pack will be enabled that makes furnaces require copper. Copper ingots can be obtained from raw copper on campfires.")
    public static Boolean copperFurnace = true;

    @Config(description = "If true a data pack will be enabled that disables villages and pillagers outpost generation.")
    public static Boolean disableLongNosesStructures = true;

    @Config(description = "If true a data pack will be enabled that changes fishing Loot.")
    public static Boolean fishingLootChanges = true;
    @Config(description = "If true, a data pack will be enabled that makes End Cities more common.")
    public static Boolean increaseEndCities = true;

    @Config(description = "If true a data pack will be enabled that overhauls structure loot. Disables itself if iguanatweaksexpanded is present")
    public static Boolean betterStructureLoot = true;
    @Config(description = "If true a data pack will be enabled that reduces loot from structures closer to spawn")
    public static Boolean lessLootCloserToSpawn = true;
    @Config(description = "Changes mobs loot and makes mobs drop reduced loot if not killed by a player")
    public static Boolean mobLootChanges = true;
    @Config(description = """
            Overhauls vanilla advancements:
            * Heavily increases experience reward
            * Removes some advancements (like villagers ones)""")
    public static Boolean advancements = true;
    @Config(description = "Integrates the mod with Supplementaries. Check here for changes: https://github.com/Insane96/IguanaTweaksReborn/wiki/Supplementaries-integration")
    public static Boolean supplementaries = false;
    @Config(description = "Integrates the mod with Farmer's delight. Some config options are changed along with a data pack installed. Check here for changes: https://github.com/Insane96/IguanaTweaksReborn/wiki/Farmer%27s-Delight-integration")
    public static Boolean farmersDelight = false;
    @Config(description = "If true, the nourishment effect from Farmers' Delight is replaced with Speed or Vigour if Stamina mod is present")
    public static Boolean replaceFDNourishmentEffect = false;
    @Config(description = "Integrates the mod with Environmental. Changes animals loot to match the livestock changes")
    public static Boolean environmental = false;
    @Config(description = "Integrates the mod with Quark. Changes animals loot to match the livestock changes.")
    public static Boolean quark = false;
    @Config(description = "Integrates the mod with Autumnity. Changes animals loot to match the livestock changes.")
    public static Boolean autumnity = false;
    @Config(description = "Integrates the mod with Caverns & Chasms. Slightly alters ore generation and adds item stats.")
    public static Boolean cavernsAndChasms = false;
    @Config(description = "Integrates the mod with Tide. Lowers chance for fishes to drop fish.")
    public static Boolean tide = false;
    @Config(description = "Integrates the mod with Crate. Alters ore generation.")
    public static Boolean create = false;
    @Config(description = "Integrates the mod with Tinker's Construct. Disables vanilla items. Changes some stats and adds modifiers")
    public static Boolean tinkersConstruct = false;

    @Config(description = "When you add a new mod the game automatically sets the data pack of the mod at the bottom of all the data packs, making the data packs loaded from this mod not work. If this is set to true the enabled and disabled Data Packs of the world are reset and reloaded. WARNING: you'll lose disabled data packs!")
    public static Boolean forceReloadWorldDataPacks = false;

    public Packs(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        InsaneSO.addServerPack("hardcore_torches", "Insane's Survival Overhaul Hardcore Torches", () -> this.isEnabled() && !Packs.disableAllDataPacks && hardcoreTorches);
        InsaneSO.addServerPack("cheaper_chains", "Insane's Survival Overhaul Cheaper Chains", () -> this.isEnabled() && !Packs.disableAllDataPacks && cheaperChains);
        InsaneSO.addServerPack("misc_tweaks", "Insane's Survival Overhaul Misc Tweaks", () -> this.isEnabled() && !Packs.disableAllDataPacks && miscTweaks);
        InsaneSO.addServerPack("actual_redstone_components", "Insane's Survival Overhaul Actual Redstone components", () -> this.isEnabled() && !Packs.disableAllDataPacks && actualRedstoneComponents);
        InsaneSO.addServerPack("copper_furnace", "Insane's Survival Overhaul Copper Furnace", () -> this.isEnabled() && !Packs.disableAllDataPacks && copperFurnace);
        InsaneSO.addServerPack("disable_long_noses", "Insane's Survival Overhaul Disable Long Noses", () -> this.isEnabled() && !Packs.disableAllDataPacks && disableLongNosesStructures);
        InsaneSO.addServerPack("fishing_loot_changes", "Insane's Survival Overhaul Fishing Loot Changes", () -> this.isEnabled() && !Packs.disableAllDataPacks && fishingLootChanges);
        InsaneSO.addServerPack("increased_end_cities", "Insane's Survival Overhaul Increased End Cities", () -> this.isEnabled() && !Packs.disableAllDataPacks && increaseEndCities);
        InsaneSO.addServerPack("better_loot", "Insane's Survival Overhaul Better Loot", () -> this.isEnabled() && !Packs.disableAllDataPacks && betterStructureLoot && !ModList.get().isLoaded("iguanatweaksexpanded"));
        InsaneSO.addServerPack("hardcore_loot", "Insane's Survival Overhaul Less Loot Closer to Spawn", () -> this.isEnabled() && !Packs.disableAllDataPacks && lessLootCloserToSpawn);
        InsaneSO.addServerPack("mob_loot_changes", "Insane's Survival Overhaul Mob Loot Changes", () -> this.isEnabled() && !Packs.disableAllDataPacks && mobLootChanges);
        InsaneSO.addServerPack("advancements", "Insane's Survival Overhaul Advancements", () -> this.isEnabled() && !Packs.disableAllDataPacks && advancements);
        InsaneSO.addServerPack("supplementaries_integration", "Insane's Survival Overhaul Supplementaries Integration", () -> this.isEnabled() && !Packs.disableAllDataPacks && supplementaries && ModList.get().isLoaded("supplementaries"));
        InsaneSO.addServerPack("environmental_integration", "Insane's Survival Overhaul Environmental Integration", () -> this.isEnabled() && !Packs.disableAllDataPacks && environmental && ModList.get().isLoaded("environmental"));
        InsaneSO.addServerPack("quark_integration", "Insane's Survival Overhaul Quark Integration", () -> this.isEnabled() && !Packs.disableAllDataPacks && quark && ModList.get().isLoaded("quark"));
        InsaneSO.addServerPack("autumnity_integration", "Insane's Survival Overhaul Autumnity Integration", () -> this.isEnabled() && !Packs.disableAllDataPacks && autumnity && ModList.get().isLoaded("autumnity"));
        InsaneSO.addServerPack("farmers_delight_integration", "Insane's Survival Overhaul Farmer's Delight Integration", () -> this.isEnabled() && !Packs.disableAllDataPacks && farmersDelight && ModList.get().isLoaded("farmersdelight"));
        InsaneSO.addServerPack(1, "caverns_and_chasms_integration", "Insane's Survival Overhaul Caverns & Chasms Integration", () -> this.isEnabled() && !Packs.disableAllDataPacks && cavernsAndChasms && ModList.get().isLoaded("caverns_and_chasms"));
        InsaneSO.addServerPack(1, "tide_integration", "Insane's Survival Overhaul Tide Integration", () -> this.isEnabled() && !Packs.disableAllDataPacks && tide && ModList.get().isLoaded("tide"));
        InsaneSO.addServerPack(1, "create_integration", "Insane's Survival Overhaul Create Integration", () -> this.isEnabled() && !Packs.disableAllDataPacks && create && ModList.get().isLoaded("create"));
        InsaneSO.addServerPack(1, "tconstruct_integration", "Insane's Survival Overhaul Tinkers' Construct Integration", () -> this.isEnabled() && !Packs.disableAllDataPacks && tinkersConstruct);

        InsaneSO.addClientPack("assets_override", "ISO Assets Override", () -> true);
        InsaneSO.addClientPack("programmer_art", "ISO Programmer Art", () -> true);
    }

    @Override
    public void readConfig(ModConfigEvent event) {
        super.readConfig(event);
        if (this.isEnabled() && ModList.get().isLoaded("farmersdelight") && farmersDelight) {
            Configuration.ENABLE_STACKABLE_SOUP_ITEMS.set(false);
            Configuration.CHANCE_WILD_BEETROOTS.set(Integer.MAX_VALUE);
            Configuration.CHANCE_WILD_CARROTS.set(Integer.MAX_VALUE);
            Configuration.CHANCE_WILD_POTATOES.set(Integer.MAX_VALUE);
        }
    }

    @SubscribeEvent
    public void onTryRichSoilFarmland(PlayerInteractEvent.RightClickBlock event) {
        if (!ModList.get().isLoaded("farmersdelight")
                || !this.isEnabled()
                || !farmersDelight
                || !event.getItemStack().canPerformAction(ToolActions.HOE_TILL))
            return;

        if (FarmersDelightIntegration.preventRichSoilFarmland(event.getLevel().getBlockState(event.getHitVec().getBlockPos())))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!ModList.get().isLoaded("farmersdelight")
                || !this.isEnabled()
                || !replaceFDNourishmentEffect)
            return;

        FarmersDelightIntegration.onEffectApplicable(event);
    }

    public static CompoundTag forceReloadWorldDataPacks(CompoundTag levelTag) {
        if (!forceReloadWorldDataPacks)
            return levelTag;

        Feature.get(Packs.class).getConfigOption("Force Reload world Data Packs").set(false);
        CompoundTag dataTag = levelTag.getCompound("Data");
        dataTag.remove("DataPacks");
        return levelTag;
    }
}
