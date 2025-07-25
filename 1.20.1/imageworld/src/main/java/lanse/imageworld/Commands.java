package lanse.imageworld;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import lanse.imageworld.automata.Automata3D;
import lanse.imageworld.automata.LavaCaster;
import lanse.imageworld.imagecalculator.ColorMapGenerator;
import lanse.imageworld.imagecalculator.CoordinateCalculator;
import lanse.imageworld.imagecalculator.ImageCalculator;
import lanse.imageworld.imagecalculator.ImageConverter;
import lanse.imageworld.imagecalculator.worldpresets.FullColorMesaPreset;
import lanse.imageworld.storage.Database;
import lanse.imageworld.storage.RegionStorage;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.nio.file.Path;
import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

public class Commands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ImageWorld").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("start")
                        .then(CommandManager.literal("folder")
                                .then(CommandManager.argument("folderPath", StringArgumentType.string())
                                        .then(CommandManager.argument("videoName", StringArgumentType.string())
                                                .then(CommandManager.argument("width", IntegerArgumentType.integer())
                                                        .then(CommandManager.argument("height", IntegerArgumentType.integer()).executes(context -> {
                                                            ImageConverter.inputFolder = StringArgumentType.getString(context, "folderPath");
                                                            ImageConverter.inputFile = "UNKNOWN";
                                                            ImageCalculator.videoName = StringArgumentType.getString(context, "videoName");
                                                            ImageConverter.targetWidth = IntegerArgumentType.getInteger(context, "width");
                                                            ImageConverter.targetHeight = IntegerArgumentType.getInteger(context, "height");
                                                            ImageConverter.isUsingLargeImage = false;
                                                            ServerCommandSource source = context.getSource();
                                                            ImageWorld.originalServer = source.getServer();
                                                            ImageWorld.originalPlayer = source.getPlayer();
                                                            ImageWorld.isModEnabled = true;
                                                            ColorMapGenerator.isUsingColorMap = false;
                                                            ImageWorld.singleStopper = false;
                                                            ImageCalculator.currentFrameComplete = true;
                                                            ImageCalculator.currentFrameIndex--;
                                                            source.sendFeedback(() -> Text.of("Starting video frame conversion from folder. DONT MOVE UNTIL ITS DONE. IF YOU USED THE FILE VERSION BEFORE THIS, THIS MIGHT NOT WORK, SO RESTART MINECRAFT IF ITS A PROBLEM."), false);
                                                            return 1;
                                                        }))))))
                        .then(CommandManager.literal("file")
                                .then(CommandManager.argument("filePath", StringArgumentType.string())
                                        .then(CommandManager.argument("videoName", StringArgumentType.string())
                                                .then(CommandManager.argument("width", IntegerArgumentType.integer())
                                                        .then(CommandManager.argument("height", IntegerArgumentType.integer()).executes(context -> {
                                                            ImageConverter.inputFile = StringArgumentType.getString(context, "filePath");
                                                            ImageConverter.inputFolder = "UNKNOWN";
                                                            CoordinateCalculator.coordinateMode = CoordinateCalculator.CoordinateMode.SINGLE_AT_LOCATION;
                                                            ImageCalculator.videoName = StringArgumentType.getString(context, "videoName");
                                                            ImageConverter.targetWidth = IntegerArgumentType.getInteger(context, "width");
                                                            ImageConverter.targetHeight = IntegerArgumentType.getInteger(context, "height");
                                                            ImageConverter.isUsingLargeImage = ImageConverter.targetWidth + ImageConverter.targetHeight > 900;
                                                            ServerCommandSource source = context.getSource();
                                                            ImageWorld.originalServer = source.getServer();
                                                            ImageWorld.originalPlayer = source.getPlayer();
                                                            ImageWorld.isModEnabled = true;
                                                            ColorMapGenerator.isUsingColorMap = false;
                                                            ImageWorld.singleStopper = false;
                                                            ImageCalculator.currentFrameComplete = true;
                                                            ImageCalculator.currentFrameIndex--;
                                                            source.sendFeedback(() -> Text.of("Starting video frame conversion from file."), false);
                                                            return 1;
                                                        }))))))
                        .then(CommandManager.literal("colorMap")
                                .then(CommandManager.argument("scale", IntegerArgumentType.integer()).executes(context -> {
                                    ImageConverter.inputFile = "UNKNOWN";
                                    ImageConverter.inputFolder = "UNKNOWN";
                                    ColorMapGenerator.biomeScale = IntegerArgumentType.getInteger(context, "scale");
                                    ColorMapGenerator.isUsingColorMap = true;
                                    CoordinateCalculator.coordinateMode = CoordinateCalculator.CoordinateMode.SINGLE_AT_LOCATION;
                                    ImageConverter.isUsingLargeImage = true;
                                    ServerCommandSource source = context.getSource();
                                    ImageWorld.originalServer = source.getServer();
                                    ImageWorld.originalPlayer = source.getPlayer();
                                    ImageWorld.isModEnabled = true;
                                    ImageWorld.singleStopper = false;
                                    ImageCalculator.currentFrameComplete = true;
                                    source.sendFeedback(() -> Text.of("Starting terrain generation from ColorMap."), false);
                                    return 1;
                                }))))

                .then(CommandManager.literal("off").executes(context -> {
                    ImageWorld.isModEnabled = false;
                    Database dataSaver = Database.getOrCreate(context.getSource().getServer());
                    Path configFolder = ImageWorld.getConfigFolder();
                    dataSaver.saveToJson(configFolder);
                    context.getSource().sendFeedback(() -> Text.literal("ImageWorld Paused / Stopped!"), true);
                    return 1;
                }))

                .then(CommandManager.literal("resume").executes(context -> {
                    ImageWorld.isModEnabled = true;
                    ImageWorld.originalPlayer = context.getSource().getPlayer();
                    ImageWorld.singleStopper = false;
                    context.getSource().sendFeedback(() -> Text.literal("ImageWorld Resumed."), true);
                    return 1;
                }))

                .then(CommandManager.literal("reset")
                        .then(CommandManager.literal("chunks").executes(context -> {
                            ChunkProcessor.clearProcessedChunks();
                            context.getSource().sendFeedback(() -> Text.literal("ImageWorld processing queue and updated chunks reset!"), true);
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
                                            context.getSource().sendFeedback(() -> Text.literal("ImageWorld settings have returned to default!"), true);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("permasave")
                                .then(CommandManager.argument("confirm", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"confirm"}, builder)).executes(context -> {
                                            String confirmation = StringArgumentType.getString(context, "confirm");
                                            if (!confirmation.equals("confirm")) {
                                                context.getSource().sendFeedback(() -> Text.literal("Type '/reset permasave confirm' to proceed."), false);
                                                return 0;
                                            }
                                            RegionStorage.resetPermaSave(context.getSource().getServer());
                                            context.getSource().sendFeedback(() -> Text.literal("Permasave File Reset!"), true);
                                            return 1;
                                        }))))

                .then(CommandManager.literal("Set")
                        .then(CommandManager.literal("mode")
                                .then(CommandManager.literal("permaSave")
                                        .then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            ImageWorld.permaSave = enabled;
                                            Database.changed = true;
                                            context.getSource().sendFeedback(() -> Text.literal("PermaSave set to: " + enabled), true);
                                            return 1;
                                        })))
                                .then(CommandManager.literal("useNaturalMesaTop")
                                        .then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            FullColorMesaPreset.naturalMesaTop = enabled;
                                            Database.changed = true;
                                            context.getSource().sendFeedback(() -> Text.literal("Using Natural Mesa Top set to: " + enabled), true);
                                            return 1;
                                        })))
                                .then(CommandManager.literal("clampWorldHeight")
                                        .then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            WorldEditor.clampHeight = enabled;
                                            Database.changed = true;
                                            context.getSource().sendFeedback(() -> Text.literal("Clamp World Height set to: " + enabled), true);
                                            return 1;
                                        })))
                                .then(CommandManager.literal("skyblock")
                                        .then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                            Automata3D.skyblockMode = enabled;
                                            Database.changed = true;
                                            context.getSource().sendFeedback(() -> Text.literal("Skyblock mode set to: " + enabled), true);
                                            return 1;
                                        }))))
                        .then(CommandManager.literal("originalPlayer").executes(context -> {
                            ImageWorld.originalPlayer = context.getSource().getPlayer();
                            context.getSource().sendFeedback(() -> Text.literal("Original player set to: " + ImageWorld.originalPlayer.getName().getString()), true);
                            return 1;
                        }))
                        .then(CommandManager.literal("colorPalette")
                                .then(CommandManager.argument("colorpalette", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                                "black_and_white", "full_color", "full_color_mesa", "anarchy"
                                        }, builder)).executes(context -> {
                                            String setting = StringArgumentType.getString(context, "colorpalette").toLowerCase();
                                            Database.changed = true;
                                            switch (setting) {
                                                case "black_and_white" -> {
                                                    WorldEditor.colorPalette = WorldEditor.ColorPalette.BLACK_AND_WHITE;
                                                    context.getSource().sendError(Text.of("Color Palette set to Black and White."));
                                                }
                                                case "full_color" -> {
                                                    WorldEditor.colorPalette = WorldEditor.ColorPalette.FULL_COLOR;
                                                    context.getSource().sendError(Text.of("Color Palette set to full Rainbow."));
                                                }
                                                case "full_color_mesa" -> {
                                                    WorldEditor.colorPalette = WorldEditor.ColorPalette.FULL_COLOR_MESA;
                                                    context.getSource().sendError(Text.of("Color Palette set to full Rainbow Mesa."));
                                                }
                                                case "anarchy" -> {
                                                    WorldEditor.colorPalette = WorldEditor.ColorPalette.ANARCHY;
                                                    context.getSource().sendError(Text.of("Color Palette set to Anarchy."));
                                                }
                                                default -> context.getSource().sendError(Text.of("Unknown color palette."));
                                            }
                                            return 1;
                                        })))
                        .then(CommandManager.literal("coordinateMode")
                                .then(CommandManager.argument("coordinatemode", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                                "line", "box_spiral", "single", "single_at_location"
                                        }, builder)).executes(context -> {
                                            String setting = StringArgumentType.getString(context, "coordinatemode").toLowerCase();
                                            Database.changed = true;
                                            switch (setting) {
                                                case "line" -> {
                                                    CoordinateCalculator.coordinateMode = CoordinateCalculator.CoordinateMode.LINE;
                                                    context.getSource().sendError(Text.of("Coordinate mode set to Line."));
                                                }
                                                case "box_spiral" -> {
                                                    CoordinateCalculator.coordinateMode = CoordinateCalculator.CoordinateMode.BOX_SPIRAL;
                                                    context.getSource().sendError(Text.of("Coordinate mode set to full Box Spiral."));
                                                }
                                                case "single" -> {
                                                    CoordinateCalculator.coordinateMode = CoordinateCalculator.CoordinateMode.SINGLE;
                                                    context.getSource().sendError(Text.of("Coordinate mode set to full Single."));
                                                }
                                                case "single_at_location" -> {
                                                    CoordinateCalculator.coordinateMode = CoordinateCalculator.CoordinateMode.SINGLE_AT_LOCATION;
                                                    context.getSource().sendError(Text.of("Coordinate mode set to full Single directly at your location."));
                                                }
                                                default -> context.getSource().sendError(Text.of("Unknown coordinate mode."));
                                            }
                                            return 1;
                                        })))
                        .then(CommandManager.literal("setting")
                                .then(CommandManager.argument("setting", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
                                                "main_column_loading_speed", "frame_index", "pause_timer_seconds"
                                        }, builder)).then(CommandManager.argument("value", IntegerArgumentType.integer()).executes(context -> {
                                            String setting = StringArgumentType.getString(context, "setting").toLowerCase();
                                            int value = IntegerArgumentType.getInteger(context, "value");
                                            Database.changed = true;
                                            switch (setting) {
                                                case "main_column_loading_speed" -> {
                                                    if (value >= 1 && value <= 2500) {
                                                        ImageWorld.maxColumnsPerTick = value;
                                                        LavaCaster.maxSpeed = value * 100;
                                                        context.getSource().sendFeedback(() -> Text.of("Column Loading Speed set to: " + value), true);
                                                        if (value > 1000){
                                                            context.getSource().sendFeedback(() -> Text.of("BE CAREFUL WITH VALUES ABOVE 1,000 FOR THIS!!! THE GAME MIGHT CRASH ON SOME SETTINGS OR DEVICES!!!"), true);
                                                        }
                                                    } else {
                                                        context.getSource().sendError(Text.of("Please specify a value between 1 and 2,500."));
                                                    }
                                                }
                                                case "frame_index" -> {
                                                    ImageCalculator.currentFrameIndex = value;
                                                    context.getSource().sendFeedback(() -> Text.of("Frame index set to: " + value + ". Make sure this is a frame of the video."), true);
                                                }
                                                case "pause_timer_seconds" -> {
                                                    ImageWorld.pauseTimerLength = value * 20;
                                                    context.getSource().sendFeedback(() -> Text.of("Pause timer set to " + value + " seconds / " + ImageWorld.pauseTimerLength + " ticks."), true);
                                                }
                                                default -> context.getSource().sendError(Text.of("Unknown setting."));
                                            }
                                            return 1;
                                        })))))
                .then(CommandManager.literal("Get").then(CommandManager.literal("Settings").executes(context -> {
                    ServerCommandSource source = context.getSource();
                    Database.getSettings(source);
                    return 1;
                })))
                .then(CommandManager.literal("Find")
                        .then(CommandManager.literal("AdvancedEnableAndResetPlayerWorldLoader")
                                .requires(source -> source.hasPermissionLevel(2)).then(CommandManager.argument("player", EntityArgumentType.player())
                                        .then(CommandManager.argument("enable", BoolArgumentType.bool()).executes(context -> {
                                            CarpetPlayerWorldLoader.trackedPlayer = EntityArgumentType.getPlayer(context, "player");
                                            CarpetPlayerWorldLoader.playerWorldLoaderEnabled = BoolArgumentType.getBool(context, "enable");
                                            CarpetPlayerWorldLoader.trackedPlayerMovementDistance = 20;
                                            context.getSource().sendFeedback(() -> Text.literal("ImageWorld Player World Loader " + (CarpetPlayerWorldLoader.playerWorldLoaderEnabled ? "enabled" : "disabled") + " for " + CarpetPlayerWorldLoader.trackedPlayer.getName() + "! Use a carpet bot in creative flying forward and let it run for a while. It will load everything near you."), true);
                                            return 1;
                                        })))))
                .then(CommandManager.literal("Debug")
                        .then(CommandManager.literal("chunkTask").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.of("ProcessingQueue size: " + ImageWorld.processingQueue.size()), false);
                            context.getSource().sendFeedback(() -> Text.of("PixelArray size: " + ImageCalculator.currentFrameData.length), false);
                            return 1;
                        }))
                        .then(CommandManager.literal("lavaCaster").executes(context -> {
                            context.getSource().sendFeedback(() -> Text.of("LavaQueue size: " + LavaCaster.lavaQueue.size()), false);
                            context.getSource().sendFeedback(() -> Text.of("InitializerQueue size: " + LavaCaster.initializerQueue.size()), false);
                            context.getSource().sendFeedback(() -> Text.of("seen size: " + LavaCaster.seen.size()), false);
                            return 1;
                        })))
                .then(CommandManager.literal("LavaCast")
                        .then(CommandManager.argument("rule", IntegerArgumentType.integer()).executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            int rule = IntegerArgumentType.getInteger(context, "rule");
                            if (rule < 0 || rule > 512){
                                context.getSource().sendFeedback(() -> Text.of("rule must be an integer between 0 and 512."), false);
                                return 0;
                            }
                            assert player != null;
                            LavaCaster.initializeLavaCast(player.getServerWorld(), player.getBlockPos(), LavaCaster.LavaCastType.LAVACAST, rule, 0);
                            context.getSource().sendFeedback(() -> Text.of("spawned lavacast with rule: " + rule), false);
                            return 1;
                        })))
                .then(literal("Automata3D")
                        .then(literal("create")
                                .then(argument("maxTicks", IntegerArgumentType.integer(1))
                                        .executes(ctx -> {
                                            // maxTicks only, symmetrical = "random", type = NORMAL
                                            return Automata3D.executeAutomatonCreate(ctx, "random", Automata3D.AutomataType.NORMAL);
                                        })
                                        .then(argument("symmetrical", StringArgumentType.word())
                                                .suggests((ctx, builder) ->
                                                        CommandSource.suggestMatching(List.of("true", "false", "random"), builder)).executes(ctx -> {
                                                    String sym = StringArgumentType.getString(ctx, "symmetrical");
                                                    return Automata3D.executeAutomatonCreate(ctx, sym, Automata3D.AutomataType.NORMAL);
                                                })
                                                .then(argument("type", StringArgumentType.word()).suggests((ctx, builder) -> CommandSource.suggestMatching(
                                                        Arrays.stream(Automata3D.AutomataType.values())
                                                                .map(Enum::name)
                                                                .map(String::toLowerCase)
                                                                .toList(), builder)).executes(ctx -> {
                                                    String sym = StringArgumentType.getString(ctx, "symmetrical");
                                                    String typeStr = StringArgumentType.getString(ctx, "type").toUpperCase();
                                                    Automata3D.AutomataType type = Automata3D.AutomataType.valueOf(typeStr); // Will throw if invalid, that's fine
                                                    return Automata3D.executeAutomatonCreate(ctx, sym, type);
                                                })))))
                        .then(CommandManager.literal("Set").requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.literal("colorPalette").then(CommandManager.argument("palette", StringArgumentType.string())
                                        .suggests((context, builder) -> CommandSource.suggestMatching(Automata3D.COLOR_PALLETS, builder)).executes(context -> {
                                            String palette = StringArgumentType.getString(context, "palette").toLowerCase();
                                            Automata3D.setColorPalette(palette);
                                            context.getSource().sendFeedback(() -> Text.literal("Color palette set to " + palette + "."), true);
                                            Database.changed = true;
                                            return 1;
                                        })))
                                .then(CommandManager.literal("biomeScale")
                                        .then(CommandManager.argument("scale", IntegerArgumentType.integer()).executes(context -> {
                                            int scale = IntegerArgumentType.getInteger(context, "scale");
                                            ColorMapGenerator.biomeScale = scale;
                                            Objects.requireNonNull(context.getSource().getPlayer()).sendMessage(Text.of("Biome Scale set to " + scale));
                                            return 1;
                                        }))))
                        .then(CommandManager.literal("stop").executes(ctx -> {
                            ServerCommandSource source = ctx.getSource();
                            Automata3D.stopSimulation = true;
                            source.sendFeedback(() -> Text.literal("Stopping automaton for this world."), false);
                            return 1;
                        }))
                        .then(CommandManager.literal("debug")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(ctx -> {
                                    ServerCommandSource source = ctx.getSource();
                                    boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
                                    source.sendFeedback(() -> Text.literal("Debug mode: " + (enabled ? "ON" : "OFF")), false);

                                    ServerWorld world = source.getWorld();
                                    Set<BlockPos> aliveSet = Automata3D.worldState.getOrDefault(world, Collections.emptySet());
                                    source.sendFeedback(() -> Text.literal("Alive block count in current automaton: " + aliveSet.size()), false);

                                    source.sendFeedback(() -> Text.literal("Decaying block count: " + Automata3D.decayTimers.size()), false);

                                    source.sendFeedback(() -> Text.literal("Queue size: " + Automata3D.automataQueue.size()), false);
                                    Automata3D.debug = enabled;
                                    return 1;
                                })))));
    }
}