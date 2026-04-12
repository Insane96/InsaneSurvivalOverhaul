package insane96mcp.insanesurvivaloverhaul.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import insane96mcp.insanesurvivaloverhaul.module.sleep.tiredness.TirednessHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.*;

public class ISOCommand {
    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("insanesurvivaloverhaul").requires(source -> source.hasPermission(2))
                .then(Commands.literal("tiredness")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.literal("set")
                                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                                .executes(context -> TirednessHandler.setFromCommand(context.getSource(), EntityArgument.getPlayers(context, "players"), FloatArgumentType.getFloat(context, "amount")))))
                                .then(Commands.literal("reset")
                                        .executes(context -> TirednessHandler.setFromCommand(context.getSource(), EntityArgument.getPlayers(context, "players"), 0)))))
                .then(Commands.literal("food")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.literal("get")
                                        .then(Commands.literal("nutrition")
                                                .executes(context -> {
                                                    for (ServerPlayer player : EntityArgument.getPlayers(context, "players"))
                                                        context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " nutrition: " + player.getFoodData().getFoodLevel()), false);
                                                    return 1;
                                                }))
                                        .then(Commands.literal("saturation")
                                                .executes(context -> {
                                                    for (ServerPlayer player : EntityArgument.getPlayers(context, "players"))
                                                        context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " saturation: " + player.getFoodData().getSaturationLevel()), false);
                                                    return 1;
                                                }))
                                        .then(Commands.literal("exhaustion")
                                                .executes(context -> {
                                                    for (ServerPlayer player : EntityArgument.getPlayers(context, "players"))
                                                        context.getSource().sendSuccess(() -> Component.literal(player.getName().getString() + " exhaustion: " + player.getFoodData().getExhaustionLevel()), false);
                                                    return 1;
                                                })))
                                .then(Commands.literal("set")
                                        .then(Commands.literal("nutrition")
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 20))
                                                        .executes(context -> {
                                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                                            int set = 0;
                                                            for (ServerPlayer player : EntityArgument.getPlayers(context, "players")) {
                                                                player.getFoodData().setFoodLevel(amount);
                                                                set++;
                                                            }
                                                            return set;
                                                        })))
                                        .then(Commands.literal("saturation")
                                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0, 20))
                                                        .executes(context -> {
                                                            float amount = FloatArgumentType.getFloat(context, "amount");
                                                            int set = 0;
                                                            for (ServerPlayer player : EntityArgument.getPlayers(context, "players")) {
                                                                player.getFoodData().setSaturation(amount);
                                                                set++;
                                                            }
                                                            return set;
                                                        })))
                                        .then(Commands.literal("exhaustion")
                                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                                                        .executes(context -> {
                                                            float amount = FloatArgumentType.getFloat(context, "amount");
                                                            int set = 0;
                                                            for (ServerPlayer player : EntityArgument.getPlayers(context, "players")) {
                                                                player.getFoodData().setExhaustion(amount);
                                                                set++;
                                                            }
                                                            return set;
                                                        }))))))
                .then(Commands.literal("count_ores_in_chunk")
                        .executes(context -> {
                            if (!(context.getSource().getEntity() instanceof ServerPlayer player))
                                return 0;

                            Level level = player.level();
                            ChunkPos chunkPos = new ChunkPos(player.blockPosition());
                            ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);

                            Map<Block, Integer> oreCounts = new HashMap<>();
                            int minY = level.getMinBuildHeight();
                            int maxY = level.getMaxBuildHeight();

                            for (int x = 0; x < 16; x++) {
                                for (int z = 0; z < 16; z++) {
                                    for (int y = minY; y < maxY; y++) {
                                        BlockPos pos = new BlockPos(chunkPos.getMinBlockX() + x, y, chunkPos.getMinBlockZ() + z);
                                        BlockState state = chunk.getBlockState(pos);
                                        if (state.is(Tags.Blocks.ORES)) {
                                            oreCounts.merge(state.getBlock(), 1, Integer::sum);
                                        }
                                    }
                                }
                            }

                            StringBuilder oreList = new StringBuilder("Ores in chunk:\n");
                            int totalOres = 0;

                            List<Map.Entry<Block, Integer>> sortedEntries = new ArrayList<>(oreCounts.entrySet());
                            sortedEntries.sort(Comparator.comparing(entry ->
                                    BuiltInRegistries.BLOCK.getKey(entry.getKey()).toString()));

                            for (Map.Entry<Block, Integer> entry : sortedEntries) {
                                oreList.append(BuiltInRegistries.BLOCK.getKey(entry.getKey())).append(": ").append(entry.getValue()).append("\n");
                                totalOres += entry.getValue();
                            }

                            double centerX = chunkPos.getMinBlockX() + 8.0;
                            double centerZ = chunkPos.getMinBlockZ() + 8.0;
                            double centerY = player.getY() + 2.0;

                            Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
                            textDisplay.setPos(centerX, centerY, centerZ);
                            ((insane96mcp.insanesurvivaloverhaul.mixin.accessor.TextDisplayAccessor) textDisplay).insanesurvivaloverhaul$setText(Component.literal(oreList.toString()));
                            level.addFreshEntity(textDisplay);

                            player.sendSystemMessage(Component.literal("Found " + totalOres + " ore blocks in chunk:\n" + oreList));

                            return 1;
                        })
                )
                .then(Commands.literal("test")
                        .executes(context -> {
                            FallingBlockEntity.fall(context.getSource().getLevel(), BlockPos.containing(context.getSource().getPosition()), Blocks.DIRT.defaultBlockState());
                            return 1;
                        })));
    }
}
