package insane96mcp.insanesurvivaloverhaul.module.world;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.mixin.accessor.BlockBehaviourAccessor;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.setup.ISORegistries;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

@LoadFeature(module = ISOModules.WORLD)
public class Berries extends Feature {

    public static final DeferredHolder<Item, ItemNameBlockItem> SWEET_BERRY_SEEDS = ISORegistries.ITEMS.register("sweet_berry_seeds", () -> new ItemNameBlockItem(Blocks.SWEET_BERRY_BUSH, new Item.Properties()));

    @Config(description = "Berry bushes no longer deal damage when walking in them with leggings and boots")
    public static Boolean noDamageIfDressed = true;

    @SubscribeEvent
    public void onBushesDamage(LivingIncomingDamageEvent event) {
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
                || !BerriesCommon.dataPack())
            return;

        BlockState stateClicked = event.getLevel().getBlockState(event.getHitVec().getBlockPos());

        if (event.getItemStack().is(Items.SWEET_BERRIES) && ((BlockBehaviourAccessor) Blocks.SWEET_BERRY_BUSH).callCanSurvive(stateClicked, event.getLevel(), event.getPos()))
            event.setCanceled(true);
    }

}
