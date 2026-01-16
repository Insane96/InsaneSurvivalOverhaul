package insane96mcp.iguanatweaksreborn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn.Respawn;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.respawn.RespawnPointsScreenMessage;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.TirednessHandler;
import insane96mcp.iguanatweaksreborn.module.world.weather.Foggy;
import insane96mcp.iguanatweaksreborn.module.world.weather.Weather;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
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
                .then(Commands.literal("count_ores_in_chunk")
                        .executes(context -> {
                            if (!(context.getSource().getEntity() instanceof ServerPlayer player))
                                return 0;

                            Level level = player.level();
                            ChunkPos chunkPos = new ChunkPos(player.blockPosition());
                            ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);

                            java.util.Map<net.minecraft.world.level.block.Block, Integer> oreCounts = new java.util.HashMap<>();
                            int minY = level.getMinBuildHeight();
                            int maxY = level.getMaxBuildHeight();

                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    for (int y = minY; y < maxY; y++) {
                                        BlockPos pos = new BlockPos(chunkPos.getMinBlockX() + x, y, chunkPos.getMinBlockZ() + z);
                                        BlockState state = chunk.getBlockState(pos);
                                        if (ForgeRegistries.BLOCKS.tags().getTag(net.minecraft.tags.BlockTags.create(ResourceLocation.fromNamespaceAndPath("forge", "ores"))).contains(state.getBlock())) {
                                            oreCounts.put(state.getBlock(), oreCounts.getOrDefault(state.getBlock(), 0) + 1);
                                        }
                                    }
                                }
                            }

                            double centerX = chunkPos.getMinBlockX() + 8.0;
                            double centerZ = chunkPos.getMinBlockZ() + 8.0;
                            double centerY = player.getY() + 2.0;

                            StringBuilder oreList = new StringBuilder("Ores in chunk:\n");
                            int totalOres = 0;

                            // Sort entries by block ResourceLocation
                            java.util.List<java.util.Map.Entry<net.minecraft.world.level.block.Block, Integer>> sortedEntries =
                                    new java.util.ArrayList<>(oreCounts.entrySet());
                            sortedEntries.sort(java.util.Comparator.comparing(entry ->
                                    ForgeRegistries.BLOCKS.getKey(entry.getKey()).toString()));

                            for (java.util.Map.Entry<net.minecraft.world.level.block.Block, Integer> entry : sortedEntries) {
                                oreList.append(ForgeRegistries.BLOCKS.getKey(entry.getKey())).append(": ").append(entry.getValue()).append("\n");
                                totalOres += entry.getValue();
                            }

                            Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
                            textDisplay.setPos(centerX, centerY, centerZ);
                            textDisplay.setText(Component.literal(oreList.toString()));
                            level.addFreshEntity(textDisplay);

                            player.sendSystemMessage(Component.literal("Found " + totalOres + " ore blocks in chunk:\n" + oreList.toString()));

                            return 1;
                        })
                )
                .then(Commands.literal("test")
                        .executes(context -> {
                            if (!(context.getSource().getEntity() instanceof ServerPlayer player))
                                return 0;
                            Respawn.clearRespawnPoints(player);
                            for (int i = 0; i < 3; i++) {
                                Respawn.addRespawnPoint(player, Component.literal("respawn " + i), BlockPos.containing(player.blockPosition().getX() + i, player.blockPosition().getY(), player.blockPosition().getZ() + i));
                            }
                            RespawnPointsScreenMessage.send(player);
                            Respawn.clearRespawnPoints(player);
                            return 1;
                        })));
    }

}
