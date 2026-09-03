package insane96mcp.insanesurvivaloverhaul.mixin.module.combat.fletching;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingFeature;
import insane96mcp.insanesurvivaloverhaul.module.combat.fletching.FletchingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FletchingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FletchingTableBlock.class)
public abstract class FletchingTableBlockMixin_Fletching {
    private static final Component CONTAINER_TITLE = Component.translatable("block.minecraft.fletching_table");

    /**
     * Vanilla's fletching table does nothing on right click ({@code useWithoutItem} always returns PASS,
     * inherited unused from {@code CraftingTableBlock}); this opens the mod's {@link FletchingMenu} instead,
     * so the vanilla block/model/loot table/villager job site can be reused as-is.
     */
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void insanesurvivaloverhaul$openFletchingMenu(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (!Feature.isEnabled(FletchingFeature.class))
            return;

        if (level.isClientSide) {
            cir.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, p) -> new FletchingMenu(containerId, inventory, ContainerLevelAccess.create(level, pos)),
                CONTAINER_TITLE));
        cir.setReturnValue(InteractionResult.CONSUME);
    }
}
