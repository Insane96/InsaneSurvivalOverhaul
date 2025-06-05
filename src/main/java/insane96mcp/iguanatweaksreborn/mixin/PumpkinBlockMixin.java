package insane96mcp.iguanatweaksreborn.mixin;

import insane96mcp.iguanatweaksreborn.InsaneSO;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(PumpkinBlock.class)
public abstract class PumpkinBlockMixin {
    private static final ResourceLocation PUMPKIN_SHEAR_LOOT_TABLE = new ResourceLocation(InsaneSO.MOD_ID, "pumpkin_shear");

    @Redirect(method = "use", at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/ItemLike;I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack onShearsDrops(ItemLike item, int count, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        LootTable lootTable = ((ServerLevel) level).getServer().getLootData().getLootTable(PUMPKIN_SHEAR_LOOT_TABLE);
        LootParams lootParams = (new LootParams.Builder((ServerLevel) level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withParameter(LootContextParams.TOOL, player.getItemInHand(hand)).withOptionalParameter(LootContextParams.THIS_ENTITY, player).withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
        List<ItemStack> list = lootTable.getRandomItems(lootParams);
        return list.get(level.getRandom().nextInt(list.size()));
    }
}