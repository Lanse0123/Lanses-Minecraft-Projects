package lanse.lanses.challenge.modpack;

import com.mojang.brigadier.CommandDispatcher;
import lanse.lanses.challenge.modpack.challenges.blizzard.Blizzard;
import lanse.lanses.challenge.modpack.challenges.elytrarace.ElytraRace;
import lanse.lanses.challenge.modpack.challenges.floorislava.FloorIsLava;
import lanse.lanses.challenge.modpack.challenges.lavastorm.LavaStorm;
import lanse.lanses.challenge.modpack.challenges.lightningworld.LightningWorld;
import lanse.lanses.challenge.modpack.challenges.midastouch.MidasTouch;
import lanse.lanses.challenge.modpack.challenges.mobdoubler.MobDoubler;
import lanse.lanses.challenge.modpack.challenges.mobexploder.MobExploder;
import lanse.lanses.challenge.modpack.challenges.nuclearstorm.NuclearStorm;
import lanse.lanses.challenge.modpack.challenges.potionrain.PotionRain;
import lanse.lanses.challenge.modpack.challenges.wallspike.WallSpike;
import lanse.lanses.challenge.modpack.challenges.worldcorruptor.WorldCorrupter;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class Commands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("LCP_ChallengeModOn")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    if (MainControl.modPreset == MainControl.Preset.ALL){
                        context.getSource().sendFeedback(() -> Text.literal("The Challenge Mod Pack needs a preset to start. use LCP_Preset_ to find a preset!"), false);
                        return 0;
                    }
                    MainControl.isModEnabled = true;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Enabled! Current Preset is: " + MainControl.modPreset), true);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("LCP_ChallengeModOff")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    MainControl.isModEnabled = false;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Disabled!"), true);
                    return 1;
                }));

        dispatcher.register(CommandManager.literal("LCP_Preset_All")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    MainControl.modPreset = MainControl.Preset.ALL;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Preset set to All!"), true);
                    return 1;
                }));


        /////////////////////////////////////////////////////////////////////////////////////////////

        //TODO - somehow merge all of these into 1 command, and have them all be subcommands.

        WallSpike.register(dispatcher);
        NuclearStorm.register(dispatcher);
        LavaStorm.register(dispatcher);
        Blizzard.register(dispatcher);
        FloorIsLava.register(dispatcher);
        LightningWorld.register(dispatcher);
        WorldCorrupter.register(dispatcher);
        MobExploder.register(dispatcher);
        PotionRain.register(dispatcher);
        MidasTouch.register(dispatcher);
        MobDoubler.register(dispatcher);
        ElytraRace.register(dispatcher);
    }
}
