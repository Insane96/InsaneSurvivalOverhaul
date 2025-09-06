package insane96mcp.iguanatweaksreborn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import insane96mcp.iguanatweaksreborn.entity.ISOFallingBlockEntity;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.TirednessHandler;
import insane96mcp.iguanatweaksreborn.module.world.weather.Foggy;
import insane96mcp.iguanatweaksreborn.module.world.weather.Weather;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.command.EnumArgument;

import java.util.concurrent.atomic.AtomicReference;

public class ISOCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("insanesurvivaloverhaul").requires(source -> source.hasPermission(2))
                .then(Commands.literal("tiredness")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.literal("set")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                                .executes(context -> TirednessHandler.setFromCommand(context.getSource(), EntityArgument.getPlayers(context, "players"), FloatArgumentType.getFloat(context, "amount")))))
                                .then(Commands.literal("reset")
                                        .executes(context -> TirednessHandler.setFromCommand(context.getSource(), EntityArgument.getPlayers(context, "players"), 0)))))
                .then(Commands.literal("foggy_weather")
                        .then(Commands.literal("get")
                                .executes(context -> {
                                    if (!(context.getSource().getEntity() instanceof ServerPlayer player))
                                        return 0;
                                    player.sendSystemMessage(Component.literal(Weather.getCurrentFoggyData(context.getSource().getServer().getLevel(Level.OVERWORLD)).toString()));
                                    return 1;
                                })
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("foggy", EnumArgument.enumArgument(Foggy.class))
                                        .executes(context -> {
                                            Weather.setFoggyWeather(context.getSource().getServer().getLevel(Level.OVERWORLD), context.getArgument("foggy", Foggy.class));
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("next")
                                .executes(context -> {
                                        Weather.nextFoggyWeather(context.getSource().getServer().getLevel(Level.OVERWORLD));
                                        return 1;
                                })
                        )
                )
                .then(Commands.literal("thunderstorm_intensity")
                        .then(Commands.literal("get")
                                .executes(context -> {
                                    if (!(context.getSource().getEntity() instanceof ServerPlayer player))
                                        return 0;
                                    player.sendSystemMessage(Component.literal(Weather.getCurrentThunderIntensityData(context.getSource().getServer().getLevel(Level.OVERWORLD)).toString()));
                                    return 1;
                                }))
                )
                .then(Commands.literal("villager_professions_list")
                        .executes(context -> {
                            if (context.getSource().getEntity() == null)
                                return 0;
                            ForgeRegistries.VILLAGER_PROFESSIONS.getValues().forEach(profession -> {
                                AtomicReference<MutableComponent> component = new AtomicReference<>(Component.literal(profession.name()).append("["));
                                ForgeRegistries.POI_TYPES.getValues().forEach(poiType -> {
                                    if (profession.heldJobSite().test(Holder.direct(poiType))) {
                                        poiType.matchingStates().forEach(state -> {
                                            component.get().append(ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString()).append(", ");
                                        });
                                    }
                                });
                                context.getSource().getEntity().sendSystemMessage(component.get().append("]"));
                            });
                            return 1;
                        })
                )
                .then(Commands.literal("test")
                        .executes(context -> {
                            ISOFallingBlockEntity fallingBlock = new ISOFallingBlockEntity(context.getSource().getLevel(), context.getSource().getPosition().x, context.getSource().getPosition().y, context.getSource().getPosition().z, Blocks.DIRT.defaultBlockState);
                            context.getSource().getLevel().addFreshEntity(fallingBlock);
                            return 1;
                        })));
    }

}
