package insane96mcp.insanesurvivaloverhaul.module.world;

import insane96mcp.insanelib.core.feature.Feature;
import insane96mcp.insanelib.core.feature.LoadFeature;
import insane96mcp.insanelib.core.feature.config.Config;
import insane96mcp.insanesurvivaloverhaul.module.ISOModules;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@LoadFeature(module = ISOModules.WORLD)
public class Fluids extends Feature {
    @Config(name = "Water fall damage", description = "If enabled, water will deal fall damage if too shallow")
    public static Boolean waterFallDamage = true;
    @Config(name = "Water push force", description = "How strong does water push entities. Vanilla is 0.014")
    public static Double waterPushForce = 0.028d;
    @Config(name = "Water pushes when no blocks are around", description = "If true water pushes entities down with the same strength as there are no blocks around")
    public static Boolean waterPushesWhenNoBlocksAround = true;
    @Config(name = "[EXPERIMENTAL] No explosion resistance", description = "If true, water and lava will have 0 explosion resistance allowing waterlogged block to be destroyed by explosions")
    public static Boolean noExplosionResistance = true;
    @Config(name = "[EXPERIMENTAL] Floaty entities", description = "If true, entities will float in water")
    public static Boolean floatyEntities = false;

    public static boolean shouldOverrideWaterFallDamageModifier() {
        return Feature.isEnabled(Fluids.class) && waterFallDamage;
    }

    public static boolean shouldWaterPushWhenNoBlocksAround() {
        return Feature.isEnabled(Fluids.class) && waterPushesWhenNoBlocksAround;
    }

    public static boolean shouldChangeFluidsExplosionResistance() {
        return Feature.isEnabled(Fluids.class) && noExplosionResistance;
    }

    @SubscribeEvent
    public void onLivingUpdate(EntityTickEvent.Pre event) {
        if (!this.isEnabled()
                || !floatyEntities
                || event.getEntity() instanceof WaterAnimal
                || event.getEntity().getFluidTypeHeight(NeoForgeMod.WATER_TYPE.value()) < event.getEntity().getBbHeight() / 3f
                || (event.getEntity() instanceof Player player && player.getAbilities().flying))
            return;
        Vec3 deltaMovement = event.getEntity().getDeltaMovement();
        event.getEntity().setDeltaMovement(deltaMovement.x, deltaMovement.y + /*(double)(deltaMovement.y < (double)0.06F ? 5.0E-4F : 0.0F)*/0.035, deltaMovement.z);
    }
}