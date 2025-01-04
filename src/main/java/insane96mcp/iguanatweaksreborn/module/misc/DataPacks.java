package insane96mcp.iguanatweaksreborn.module.misc;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.farming.crops.integration.FarmersDelightIntegration;
import insane96mcp.iguanatweaksreborn.setup.IntegratedPack;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import vectorwing.farmersdelight.common.Configuration;

@Label(name = "Data Packs & Integration", description = "Various data packs that can be enabled/disabled")
@LoadFeature(module = Modules.Ids.MISC)
public class DataPacks extends Feature {

    @Config
    @Label(name = "Disable ALL data packs", description = "If true, no integrated data pack will be loaded")
    public static Boolean disableAllDataPacks = false;

    //Furnace requires copper, you get nuggets by smelting raw copper on campfire
    @Config
    @Label(name = "Hardcore Torches", description = """
            Changes vanilla torch recipes.
            * Torches can be only made on Campfires early on in the game
            * With shears you can make 3 torches out of coal
            * With Fire Charges you can make them later in the game.""")
    public static Boolean hardcoreTorches = true;

    @Config
    @Label(name = "Cheaper Chains", description = "Changes vanilla chains recipe. Makes chains easily craftable with nuggets only.")
    public static Boolean cheaperChains = true;

    @Config
    @Label(name = "Misc tweaks", description = """
            Minor changes:
            * Cakes now drop when broken and not eaten
            * Dark Prismarine is made easier
            * Clay balls can be crafted from blocks
            * Leads and Sticky Pistons can also be made with Honey
            * Iron Doors can be blasted back to Iron Ingots
            * Dispensers can be made from droppers
            * Levers and glass can now be broken faster with pickaxes, cactus with hoes""")
    public static Boolean miscTweaks = true;

    @Config
    @Label(name = "Actual Redstone Components", description = "Makes redstone components require redstone in their recipe (e.g. Levers, pressure plates, etc). Also makes Corail's Woodcutter no longer able to make buttons and pressure plates.")
    public static Boolean actualRedstoneComponents = true;

    @Config
    @Label(name = "Copper Furnace", description = "If true a data pack will be enabled that makes furnaces require copper. Copper ingots can be obtained from raw copper on campfires.")
    public static Boolean copperFurnace = true;

    @Config
    @Label(name = "Disable Long Noses Structures", description = "If true a data pack will be enabled that disables villages and pillagers outpost generation.")
    public static Boolean disableLongNoses = true;

    @Config
    @Label(name = "Fishing Loot Changes", description = "If true a data pack will be enabled that changes fishing Loot.")
    public static Boolean fishingLootChanges = true;
    @Config
    @Label(name = "Increase End Cities", description = "If true, a data pack will be enabled that makes End Cities will be more common.")
    public static Boolean increaseEndCities = true;

    @Config
    @Label(name = "Better Structure Loot", description = "If true a data pack will be enabled that overhauls structure loot. Disables itself if iguanatweaksexpanded is present")
    public static Boolean betterStructureLoot = true;
    @Config
    @Label(name = "Less loot closer to spawn", description = "If true a data pack will be enabled that reduces loot from structures closer to spawn")
    public static Boolean lessLootCloserToSpawn = true;
    @Config
    @Label(name = "Mob loot changes", description = "Changes mobs loot and makes mobs drop reduced loot if not killed by a player")
    public static Boolean mobLootChanges = true;
    @Config
    @Label(name = "Advancements overhaul", description = """
            Overhauls vanilla advancements:
            * Merges them in a big giant advancement tree
            * Heavily increases experience reward
            * Removes some advancements (like villagers ones)""")
    public static Boolean advancements = true;
    @Config
    @Label(name = "Supplementaries integration", description = "Integrates the mod with Supplementaries. Check here for changes: https://github.com/Insane96/IguanaTweaksReborn/wiki/Farmer%27s-Delight-integration")
    public static Boolean supplementaries = true;
    @Config
    @Label(name = "Farmer's Delight integration", description = "Integrates the mod with Farmer's delight. Some config options are changed along with a data pack installed. Check here for changes: https://github.com/Insane96/IguanaTweaksReborn/wiki/Farmer%27s-Delight-integration")
    public static Boolean farmersDelight = true;

    @Config
    @Label(name = "Assets override", description = "Change some vanilla textures such as arrows to flint, spawners to echo and also some lang entries")
    public static Boolean assetsOverride = true;

    @Config
    @Label(name = "Force Reload world Data Packs", description = "When you add a new mod the game automatically sets the data pack of the mod at the bottom of all the data packs, making the data packs loaded from this mod not work. If this is set to true the enabled and disabled Data Packs of the world are reset and reloaded. WARNING: you'll lose disabled data packs!")
    public static Boolean forceReloadWorldDataPacks = false;

    public DataPacks(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "hardcore_torches", Component.literal("Insane's Survival Overhaul Hardcore Torches"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && hardcoreTorches));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "cheaper_chains", Component.literal("Insane's Survival Overhaul Cheaper Chains"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && cheaperChains));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "misc_tweaks", Component.literal("Insane's Survival Overhaul Misc Tweaks"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && miscTweaks));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "actual_redstone_components", Component.literal("Insane's Survival Overhaul Actual Redstone components"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && actualRedstoneComponents));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "copper_furnace", Component.literal("Insane's Survival Overhaul Copper Furnace"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && copperFurnace));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "disable_long_noses", Component.literal("Insane's Survival Overhaul Disable Long Noses"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && disableLongNoses));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "fishing_loot_changes", Component.literal("Insane's Survival Overhaul Fishing Loot Changes"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && fishingLootChanges));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "increased_end_cities", Component.literal("Insane's Survival Overhaul Increased End Cities"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && increaseEndCities));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "better_loot", Component.literal("Insane's Survival Overhaul Better Loot"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && betterStructureLoot && !ModList.get().isLoaded("iguanatweaksexpanded")));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "hardcore_loot", Component.literal("Insane's Survival Overhaul Less Loot Closer to Spawn"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && lessLootCloserToSpawn));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "mob_loot_changes", Component.literal("Insane's Survival Overhaul Mob Loot Changes"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && mobLootChanges));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "advancements", Component.literal("Insane's Survival Overhaul Advancements"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && advancements));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "supplementaries_integration", Component.literal("Insane's Survival Overhaul Supplementaries Integration"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && supplementaries && ModList.get().isLoaded("supplementaries")));
        IntegratedPack.addPack(new IntegratedPack(PackType.SERVER_DATA, "farmers_delight_integration", Component.literal("Insane's Survival Overhaul Farmer's Delight Integration"), () -> this.isEnabled() && !DataPacks.disableAllDataPacks && farmersDelight && ModList.get().isLoaded("farmersdelight")));

        IntegratedPack.addPack(new IntegratedPack(PackType.CLIENT_RESOURCES, "assets_override", Component.literal("Insane's Survival Overhaul Assets Override"), () -> assetsOverride));
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
                || !DataPacks.farmersDelight
                || !event.getItemStack().canPerformAction(ToolActions.HOE_TILL))
            return;

        if (FarmersDelightIntegration.preventRichSoilFarmland(event.getLevel().getBlockState(event.getHitVec().getBlockPos())))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!ModList.get().isLoaded("farmersdelight")
                || !this.isEnabled()
                || !DataPacks.farmersDelight)
            return;

        FarmersDelightIntegration.onEffectApplicable(event);
    }

    public static CompoundTag forceReloadWorldDataPacks(CompoundTag levelTag) {
        if (!forceReloadWorldDataPacks)
            return levelTag;

        Feature.get(DataPacks.class).getConfigOption("Force Reload world Data Packs").set(false);
        CompoundTag dataTag = levelTag.getCompound("Data");
        dataTag.remove("DataPacks");
        return levelTag;
    }
}
