package lanse.lanican;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Objects;

public class Commands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
//        dispatcher.register(CommandManager.literal("LCP_ChallengeModOn")
//                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
//                    if (Objects.equals(MainControl.modPreset, "none")){
//                        context.getSource().sendFeedback(() -> Text.literal("The Challenge Mod Pack needs a preset to start. use LCP_Preset_ to find a preset!"), false);
//                        return 0;
//                    }
//                    MainControl.isModEnabled = true;
//                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Enabled! Current Preset is: " + MainControl.modPreset), true);
//                    return 1;
//                }));
    }
}