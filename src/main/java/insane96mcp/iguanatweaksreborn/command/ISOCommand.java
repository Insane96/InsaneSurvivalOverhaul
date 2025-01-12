package insane96mcp.iguanatweaksreborn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import insane96mcp.iguanatweaksreborn.module.sleeprespawn.tiredness.TirednessHandler;
import insane96mcp.iguanatweaksreborn.module.world.weather.Foggy;
import insane96mcp.iguanatweaksreborn.module.world.weather.Weather;
import insane96mcp.iguanatweaksreborn.network.message.UnfairOneShotActivation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.command.EnumArgument;

import java.util.HashMap;
import java.util.Map;

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
                .then(Commands.literal("get_treasure_enchantments_book")
                        .executes(context -> {
                            if (!(context.getSource().getEntity() instanceof ServerPlayer player))
                                return 0;
                            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                            Map<Enchantment, Integer> enchantments = new HashMap<>();
                            for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
                                if (!enchantment.isTreasureOnly() || enchantment.isCurse())
                                    continue;
                                enchantments.put(enchantment, enchantment.getMaxLevel());
                            }
                            EnchantmentHelper.setEnchantments(enchantments, enchantedBook);
                            player.getInventory().add(enchantedBook);
                            return 1;
                        }))
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
                .then(Commands.literal("test")
                        .executes(context -> {
                            /*for (int i = 0; i < 3; i++) {
                                ITRFallingBlockEntity fallingBlock = new ITRFallingBlockEntity(context.getSource().getLevel(), context.getSource().getPosition().x, context.getSource().getPosition().y + i, context.getSource().getPosition().z, i == 1 ? Blocks.ACACIA_LOG.defaultBlockState : Blocks.ACACIA_LEAVES.defaultBlockState);
                                context.getSource().getLevel().addFreshEntity(fallingBlock);
                            }*/
                            if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                UnfairOneShotActivation.send(player);
                            }
                            return 1;
                        })));
    }

}
