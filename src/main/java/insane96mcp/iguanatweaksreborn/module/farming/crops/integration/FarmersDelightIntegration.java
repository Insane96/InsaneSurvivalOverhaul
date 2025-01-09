package insane96mcp.iguanatweaksreborn.module.farming.crops.integration;

import insane96mcp.iguanatweaksreborn.module.farming.crops.Crops;
import insane96mcp.stamina.stamina.StaminaFeature;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModList;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModEffects;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.utility.TextUtils;

import javax.annotation.Nullable;

public class FarmersDelightIntegration {
    public static Block getOnion() {
        return ModBlocks.ONION_CROP.get();
    }
    public static Block getRice() {
        return ModBlocks.RICE_CROP.get();
    }

    public static boolean preventPlanting(ItemStack stack) {
        return stack.is(ModItems.ONION.get()) || stack.is(ModItems.RICE.get());
    }

    public static void tryAlertRice(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(Crops.RICE_SEEDS.get()))
            return;
        Player player = event.getEntity();
        BlockState targetState = event.getLevel().getBlockState(event.getPos());
        if (player != null && event.getLevel().getFluidState(event.getPos().relative(event.getHitVec().getDirection())).isEmpty() && event.getHitVec().getDirection() == Direction.UP && (targetState.is(BlockTags.DIRT) || targetState.getBlock() instanceof FarmBlock)) {
            player.displayClientMessage(TextUtils.getTranslation("block.rice.invalid_placement"), true);
        }
    }

    public static boolean preventRichSoilFarmland(BlockState state) {
        return state.is(ModBlocks.RICH_SOIL.get());
    }

    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect() != ModEffects.NOURISHMENT.get())
            return;

        if (ModList.get().isLoaded("stamina"))
            event.getEntity().addEffect(new MobEffectInstance(StaminaFeature.VIGOUR_EFFECT.get(), (event.getEffectInstance().getDuration() + 1) * 2, event.getEffectInstance().getAmplifier(), event.getEffectInstance().isAmbient(), event.getEffectInstance().isVisible(), event.getEffectInstance().showIcon()));
        else
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, (event.getEffectInstance().getDuration() + 1), event.getEffectInstance().getAmplifier(), event.getEffectInstance().isAmbient(), event.getEffectInstance().isVisible(), event.getEffectInstance().showIcon()));
        event.setResult(Event.Result.DENY);
    }

    @Nullable
    public static ItemStack tryPickBlock(Block block) {
        if (block == ModBlocks.RICE_CROP.get() || block == ModBlocks.RICE_CROP_PANICLES.get())
            return new ItemStack(Crops.RICE_SEEDS.get());
        else if (block == ModBlocks.ONION_CROP.get())
            return new ItemStack(Crops.ROOTED_ONION.get());
        return null;
    }
}
