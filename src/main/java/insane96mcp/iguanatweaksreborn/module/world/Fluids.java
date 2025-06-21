package insane96mcp.iguanatweaksreborn.module.world;

import insane96mcp.iguanatweaksreborn.module.Modules;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@LoadFeature(module = Modules.Ids.WORLD)
public class Fluids extends Feature {
    @Config(name = "Water fall damage", description = "If enabled, water will deal fall damage if too shallow")
    public static Boolean waterFallDamage = true;
    @Config(name = "Water push force", description = "How strong does water push entities. Vanilla is 0.014")
    public static Double waterPushForce = 0.03d;
    @Config(name = "Water pushes when no blocks are around", description = "If true water pushes entities down with the same strength as there are no blocks around")
    public static Boolean waterPushesWhenNoBlocksAround = true;
    @Config(name = "[EXPERIMENTAL] No explosion resistance", description = "If true, water and lava will have 0 explosion resistance allowing waterlogged block to be destroyed by explosions")
    public static Boolean noExplosionResistance = true;
    @Config(name = "[EXPERIMENTAL] Floaty entities", description = "If true, entities will float in water")
    public static Boolean floatyEntities = false;

    public Fluids(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

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
    public void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (!this.isEnabled()
                || !floatyEntities
                || event.getEntity().getFluidTypeHeight(ForgeMod.WATER_TYPE.get()) < event.getEntity().getBbHeight() / 3f
                /*|| event.getEntity().getMobType() == MobType.WATER*/
                || (event.getEntity() instanceof Player player && player.getAbilities().flying))
            return;
        Vec3 deltaMovement = event.getEntity().getDeltaMovement();
        event.getEntity().setDeltaMovement(deltaMovement.x, deltaMovement.y + /*(double)(deltaMovement.y < (double)0.06F ? 5.0E-4F : 0.0F)*/0.035, deltaMovement.z);
    }
}