package lanse.copperworld;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import lanse.copperworld.storage.Database;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.nio.file.Path;

public class Commands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("CopperWorld").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("on").executes(context -> {
                    CopperWorld.originalServer = context.getSource().getServer();
                    CopperWorld.originalPlayer = context.getSource().getPlayer();
                    CopperWorld.isModEnabled = true;
                    context.getSource().sendFeedback(() -> Text.literal("CopperWorld Enabled!"), true);
                    return 1;
                }))

                .then(CommandManager.literal("off").executes(context -> {
                    CopperWorld.isModEnabled = false;

                    Database dataSaver = Database.getOrCreate(context.getSource().getServer());
                    Path configFolder = CopperWorld.getConfigFolder();
                    dataSaver.saveToJson(configFolder);
                    context.getSource().sendFeedback(() -> Text.literal("CopperWorld Disabled!"), true);
                    return 1;
                }))

                .then(CommandManager.literal("saveAll").executes(context -> {
                    Database dataSaver = Database.getOrCreate(context.getSource().getServer());
                    Path configFolder = CopperWorld.getConfigFolder();
                    dataSaver.saveToJson(configFolder);
                    context.getSource().sendFeedback(() -> Text.literal("CopperWorld Settings and Region File Saved!"), true);
                    return 1;
                }))

                .then(CommandManager.literal("reset")
                        .then(CommandManager.literal("chunks").executes(context -> {
                            ChunkProcessor.clearProcessedChunks();
                            context.getSource().sendFeedback(() -> Text.literal("CopperWorld processing queue and updated chunks reset!"), true);
                            return 1;
                        }))
                        .then(CommandManager.literal("settings")
                                .then(CommandManager.argument("confirm", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"confirm"}, builder)).executes(context -> {
                                            String confirmation = StringArgumentType.getString(context, "confirm");
                                            if (!confirmation.equals("confirm")) {
                                                context.getSource().sendFeedback(() -> Text.literal("Type '/reset settings confirm' to proceed."), false);
                                                return 0;
                                            }
                                            Database.returnToDefaultValues();
                                            context.getSource().sendFeedback(() -> Text.literal("CopperWorld settings have returned to default!"), true);
                                            return 1;
                                        }))))

                .then(CommandManager.literal("Set")
                        .then(CommandManager.literal("setting")
                                .then(CommandManager.argument("setting", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                                "main_column_loading_speed", "main_render_distance"
                                        }, builder)).then(CommandManager.argument("value", IntegerArgumentType.integer()).executes(context -> {
                                            String setting = StringArgumentType.getString(context, "setting").toLowerCase();
                                            int value = IntegerArgumentType.getInteger(context, "value");
                                            Database.changed = true;
                                            switch (setting) {
                                                case "main_column_loading_speed" -> {
                                                    if (value >= 1 && value <= 3500) {
                                                        CopperWorld.maxColumnsPerTick = value;
                                                        context.getSource().sendFeedback(() -> Text.of("Column Loading Speed set to: " + value), true);
                                                        if (value > 1000){
                                                            context.getSource().sendFeedback(() -> Text.of("BE CAREFUL WITH VALUES ABOVE 1,000 FOR THIS!!! THE GAME MIGHT CRASH ON SOME SETTINGS OR DEVICES!!!"), true);
                                                        }
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 1 and 3,500."));
                                                    }
                                                }
                                                case "main_render_distance" -> {
                                                    if (value >= 2 && value <= 10000000) {
                                                        ChunkProcessor.MAX_RENDER_DIST = value;
                                                        context.getSource().sendFeedback(() -> Text.of("Render Transform Distance set to: " + value), true);
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 2 and 10,000,000."));
                                                    }
                                                }
                                                default -> context.getSource().sendError(Text.of("Unknown setting."));
                                            }
                                            return 1;
                                        }))))));
    }
}