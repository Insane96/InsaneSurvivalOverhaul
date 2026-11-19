package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.villager;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.Villagers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(GiveGiftToHero.class)
public abstract class GiveGiftToHeroMixin_Villagers {

    /**
     * Throws the gift of the hero's Hero of the Village level instead of the vanilla one (see {@link Villagers#getHeroGift}).
     * Done here because {@code throwGift} is the only method that receives the hero.
     */
    @WrapOperation(method = "throwGift", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/GiveGiftToHero;getItemToThrow(Lnet/minecraft/world/entity/npc/Villager;)Ljava/util/List;"))
    private List<ItemStack> insanesurvivaloverhaul$heroLevelGift(GiveGiftToHero behavior, Villager villager, Operation<List<ItemStack>> original, @Local(argsOnly = true) LivingEntity hero) {
        List<ItemStack> gift = Villagers.getHeroGift(villager, hero);
        return gift != null ? gift : original.call(behavior, villager);
    }
}
