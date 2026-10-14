package insane96mcp.insanesurvivaloverhaul.module.items;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.Module;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.InsaneSO;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import insane96mcp.insanesurvivaloverhaul.module.death.PlayerKillerBounty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@LoadFeature(module = ISOModules.ITEMS, description = "Retrieve name tags from name tagged entities. Name tagged entities show a death message when they die.")
public class NameTags extends Feature {
    public static final TagKey<EntityType<?>> NO_NAME_TAG_DROP = TagKey.create(Registries.ENTITY_TYPE, InsaneSO.id("no_name_tag_drop"));

    public static ResourceLocation HAS_NAME_TAG;

    @Config(name = "Death Message", description = "If true, entities named with a name tag show a death message in chat when they die. Requires the showDeathMessages game rule.")
    public static Boolean deathMessage = true;

    @Override
    public void init(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super.init(module, enabledByDefault, canBeDisabled);
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
        if (event.getItemStack().get(DataComponents.CUSTOM_NAME) != null)
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
                || !ModNBTData.get(event.getEntity(), HAS_NAME_TAG, Boolean.class))
            return;

        if (deathMessage)
            sendDeathMessage(event.getEntity());
        if (!event.getEntity().getType().is(NO_NAME_TAG_DROP))
            dropNameTag(event.getEntity().level(), event.getEntity());
    }

    public static void sendDeathMessage(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)
                || !level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES))
            return;

        if (Feature.isEnabled(PlayerKillerBounty.class) && ModNBTData.contains(entity, PlayerKillerBounty.KILLED_PLAYERS))
            return;

        ServerPlayer owner = entity instanceof TamableAnimal tamable && tamable.getOwner() instanceof ServerPlayer player ? player : null;
        Component message = entity.getCombatTracker().getDeathMessage();
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player != owner)
                player.sendSystemMessage(message);
        }
    }

    public static void dropNameTag(Level level, LivingEntity entity) {
        ItemStack stack = new ItemStack(Items.NAME_TAG);
        stack.set(DataComponents.CUSTOM_NAME, entity.getCustomName());
        ItemEntity item = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }
}