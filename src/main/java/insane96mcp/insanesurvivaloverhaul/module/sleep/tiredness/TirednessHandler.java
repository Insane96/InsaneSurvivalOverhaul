package insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness;

import insane96mcp.insanelib.core.ModNBTData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;

import static insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness.Tiredness.NBT_TAG;

public class TirednessHandler {
    public static float get(LivingEntity entity) {
        return ModNBTData.get(entity, NBT_TAG, Float.class);
    }

    public static void set(LivingEntity entity, float tiredness) {
        ModNBTData.put(entity, NBT_TAG, Math.max(tiredness, 0));
    }

    public static void add(LivingEntity entity, float tiredness) {
        set(entity, get(entity) + tiredness);
    }

    public static void subtract(LivingEntity entity, float tiredness) {
        set(entity, get(entity) - tiredness);
    }

    public static float setAndGet(LivingEntity entity, float tiredness) {
        set(entity, tiredness);
        return get(entity);
    }

    public static float addAndGet(LivingEntity entity, float tiredness) {
        return setAndGet(entity, get(entity) + tiredness);
    }

    public static float subtractAndGet(LivingEntity entity, float tiredness) {
        return setAndGet(entity, get(entity) - tiredness);
    }

    public static void reset(LivingEntity entity) {
        ModNBTData.put(entity, NBT_TAG, 0);
    }

    public static float getOnWakeUp(LivingEntity entity) {
        return 0;
    }

    public static void syncToClient(ServerPlayer player) {
        ClientboundTirednessPacket.sync(player, get(player));
    }

    public static int setFromCommand(CommandSourceStack source, Collection<ServerPlayer> players, float amount) {
        int set = 0;
        for (ServerPlayer player : players) {
            set(player, amount);
            set++;
            syncToClient(player);
        }
        return set;
    }
}
