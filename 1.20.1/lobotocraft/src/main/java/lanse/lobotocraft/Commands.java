package lanse.lobotocraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import lanse.lobotocraft.terraincalculator.TerrainGenerator;
import lanse.lobotocraft.terraincalculator.TerrainPresets;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class Commands {
    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(CommandManager.literal("DementiacraftOn")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {

                    Lobotocraft.originalServer = context.getSource().getServer();
                    Lobotocraft.isModEnabled = true;
                    context.getSource().sendFeedback(() -> Text.literal("Lobotocraft Enabled!"), true);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("DementiacraftOff")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    Lobotocraft.isModEnabled = false;
                    context.getSource().sendFeedback(() -> Text.literal("Lobotocraft Disabled!"), true);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("DementiacraftSet")
                .then(CommandManager.literal("mode").then(CommandManager.argument("feature", StringArgumentType.string())
                                .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                        "Lobotomy", "Dementia"
                                }, builder)).executes(context -> {
                                    String feature = StringArgumentType.getString(context, "feature").toLowerCase();

                                    switch (feature) {
                                        case "lobotomy" -> {
                                            Lobotocraft.currentMode = Lobotocraft.Mode.LOBOTOMY;
                                            context.getSource().sendFeedback(() -> Text.literal("Mode set to: Lobotomy."), true);
                                        }
                                        case "dementia" -> {
                                            Lobotocraft.currentMode = Lobotocraft.Mode.DEMENTIA;
                                            context.getSource().sendFeedback(() -> Text.literal("Mode set to: Dementia / Oasis AI in Minecraft."), true);
                                        }
                                        default -> {
                                            context.getSource().sendFeedback(() -> Text.literal("Invalid mode. Available modes: Lobotomy, Dementia."), false);
                                            return 0;
                                        }
                                    }
                                    return 1;
                                })))
                .then(CommandManager.literal("terrainPreset")
                        .then(CommandManager.argument("preset", StringArgumentType.string()).suggests((context, builder) -> {
                                    //Suggest all numbers from that range + "randomized".
                                    for (int i = 1; i <= TerrainPresets.terrainCount; i++) builder.suggest(String.valueOf(i));
                                    builder.suggest("randomized");
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String preset = StringArgumentType.getString(context, "preset");

                                    if (preset.equalsIgnoreCase("randomized")) {
                                        TerrainPresets.preset = -1;
                                        context.getSource().sendFeedback(() -> Text.literal("Terrain preset set to: Randomized."), true);
                                    } else {
                                        try {
                                            int presetNumber = Integer.parseInt(preset);
                                            if (presetNumber < 1 || presetNumber > TerrainPresets.terrainCount) {  // Customize range here
                                                context.getSource().sendFeedback(() -> Text.literal("Invalid preset. Use a number between 1 and " + TerrainPresets.terrainCount + " or 'randomized'."), false);
                                                return 0;
                                            }
                                            TerrainGenerator.randomizer = false;
                                            TerrainPresets.preset = presetNumber;
                                            context.getSource().sendFeedback(() -> Text.literal("Terrain preset set to: " + presetNumber + "."), true);
                                        } catch (NumberFormatException e) {
                                            context.getSource().sendFeedback(() -> Text.literal("Invalid preset. Use a number or 'randomized'."), false);
                                            return 0;
                                        }
                                    }
                                    return 1;
                                }))));

        dispatcher.register(CommandManager.literal("DementiacraftGet")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("Settings").executes(context -> {
                    ServerCommandSource source = context.getSource();
                    TerrainGenerator.getSettings(source);
                    return 1;
                })));
    }
}
