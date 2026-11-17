package insane96mcp.insanesurvivaloverhaul.mixin.module.mobs.villager;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.insanesurvivaloverhaul.module.mobs.villager.Villagers;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ZombieVillager.class)
public class ZombieVillagerMixin_Villagers {

    /**
     * When {@link Villagers} is active, the cure item is any item in {@link Villagers#CURE_ITEMS} instead of the golden apple.
     */
    @ModifyExpressionValue(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean insanesurvivaloverhaul$cureItemFromTag(boolean original, @Local ItemStack itemStack) {
        if (!Villagers.isActive())
            return original;
        return itemStack.is(Villagers.CURE_ITEMS);
    }

    /**
     * When {@link Villagers} is active, replaces the vanilla cure duration (3600-6000 ticks) with the configured one.
     * Only the cure started by the player is changed, not the remaining time loaded from NBT.
     */
    @ModifyArg(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/ZombieVillager;startConverting(Ljava/util/UUID;I)V"), index = 1)
    private int insanesurvivaloverhaul$cureDuration(int original) {
        ZombieVillager $this = (ZombieVillager) (Object) this;
        return Villagers.getCureDuration($this.getRandom(), original);
    }

    /**
     * Zombie villagers placed by structures get infinite Fire Resistance (see {@link Villagers#onZombieVillagerStructureSpawn}).
     * Structure templates call finalizeSpawn directly, without firing FinalizeSpawnEvent.
     */
    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void insanesurvivaloverhaul$structureFireResistance(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        if (spawnType == MobSpawnType.STRUCTURE)
            Villagers.onZombieVillagerStructureSpawn((ZombieVillager) (Object) this);
    }
}
