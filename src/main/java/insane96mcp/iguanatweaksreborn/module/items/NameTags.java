package insane96mcp.iguanatweaksreborn.module.items;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.ModNBTData;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.ITEMS, description = "Retrieve name tags from name tagged entities.")
public class NameTags extends Feature {
    public static final TagKey<EntityType<?>> SHOULDNT_DROP_NAMETAG = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.location("shouldnt_drop_nametag"));

    public static ResourceLocation HAS_NAME_TAG;

    public NameTags(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
        HAS_NAME_TAG = this.createDataKey("has_name_tag");
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

        if (ModNBTData.get(target, HAS_NAME_TAG, Boolean.class))
            dropNameTag(target.level(), target);
        if (event.getItemStack().hasCustomHoverName())
            ModNBTData.put(target, HAS_NAME_TAG, true);
        else {
            ModNBTData.put(target, HAS_NAME_TAG, false);
            target.setCustomName(null);
        }

    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (!this.isEnabled()
                || event.getEntity().level().isClientSide
                || event.getEntity().getType().is(SHOULDNT_DROP_NAMETAG)
                || !ModNBTData.get(event.getEntity(), HAS_NAME_TAG, Boolean.class))
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