package insane96mcp.iguanatweaksreborn.module.world;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.iguanatweaksreborn.module.misc.DataPacks;
import insane96mcp.iguanatweaksreborn.setup.ISORegistries;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.RegistryObject;

@Label(name = "Berries")
@LoadFeature(module = Modules.Ids.WORLD)
public class Berries extends Feature {

    public static final RegistryObject<BlockItem> SWEET_BERRY_SEEDS = ISORegistries.ITEMS.register("sweet_berry_seeds", () -> new ItemNameBlockItem(Blocks.SWEET_BERRY_BUSH, new Item.Properties()));

    @Config
    @Label(name = "No damage if dressed", description = "Berry bushes no longer deal damage when walking in them with leggings and boots")
    public static Boolean noDamageIfDressed = true;

    @Config
    @Label(name = "Data pack", description = """
		Makes sweet berries not plantable, requiring seeds, and also enables a data pack that makes the following changes:
		* Makes sweet berry bushes drop seeds
		* Makes sweet berry patches has less plants and plants will not be all grown up
		If berry good is present, a different datapack is enabled that integrates with the mod
	""")
    public static Boolean dataPack = true;

    /*@Config
    @Label(name = "Data pack Berry Good", description = """
		Same as 'Data pack' but uses Berry Good seeds
	""")
    public static Boolean dataPackBerryGood = false;*/

    public Berries(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        InsaneSurvivalOverhaul.addServerPack("berries", "Insane's Survival Overhaul Berries", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && dataPack && !ModList.get().isLoaded("berry_good"));
        InsaneSurvivalOverhaul.addServerPack("berries_berry_good", "Insane's Survival Overhaul Berries (Berry Good compat)", () -> this.isEnabled() && !DataPacks.disableAllDataPacks && dataPack && ModList.get().isLoaded("berry_good"));
    }

    @SubscribeEvent
    public void onBushesDamage(LivingAttackEvent event) {
        if (!this.isEnabled()
                || !noDamageIfDressed
                || !event.getSource().is(DamageTypes.SWEET_BERRY_BUSH)
                || event.getEntity().getItemBySlot(EquipmentSlot.LEGS).isEmpty()
                || event.getEntity().getItemBySlot(EquipmentSlot.FEET).isEmpty())
            return;

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onTryToPlant(PlayerInteractEvent.RightClickBlock event) {
        if (!this.isEnabled()
                || (!dataPack /*&& !dataPackBerryGood*/))
            return;

        BlockState stateClicked = event.getLevel().getBlockState(event.getHitVec().getBlockPos());

        if (event.getItemStack().is(Items.SWEET_BERRIES) && ((BushBlock) Blocks.SWEET_BERRY_BUSH).canSurvive(stateClicked, event.getLevel(), event.getPos()))
            event.setCanceled(true);
    }

}
