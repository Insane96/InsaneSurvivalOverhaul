package insane96mcp.iguanatweaksreborn.module.items;

import insane96mcp.iguanatweaksreborn.InsaneSurvivalOverhaul;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Name tags", description = "Retrieve name tags from name tagged entities.")
@LoadFeature(module = Modules.Ids.ITEMS)
public class NameTags extends Feature {

    public NameTags(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (!this.isEnabled()
                || !event.getItemStack().is(Items.NAME_TAG)
                || !(event.getTarget() instanceof LivingEntity target)
                || target instanceof Player
                || event.getEntity().level().isClientSide
                || target.isDeadOrDying())
            return;

        if (target.getPersistentData().contains(InsaneSurvivalOverhaul.RESOURCE_PREFIX + "has_name_tag"))
            dropNameTag(target.level(), target);
        if (event.getItemStack().hasCustomHoverName())
            target.getPersistentData().putBoolean(InsaneSurvivalOverhaul.RESOURCE_PREFIX + "has_name_tag", true);
        else {
            target.getPersistentData().remove(InsaneSurvivalOverhaul.RESOURCE_PREFIX + "has_name_tag");
            target.setCustomName(null);
        }

    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide
                || !event.getEntity().getPersistentData().contains(InsaneSurvivalOverhaul.RESOURCE_PREFIX + "has_name_tag"))
            return;

        dropNameTag(event.getEntity().level(), event.getEntity());
    }

    public static void dropNameTag(Level level, LivingEntity entity) {
        ItemStack stack = new ItemStack(Items.NAME_TAG);
        stack.setHoverName(entity.getCustomName());
        ItemEntity item = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }
}