package lanse.bossmobs;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.customattacks.ArmorHandler;
import lanse.bossmobs.custombosses.end.EndCrystalBoss;
import lanse.bossmobs.custombosses.end.EndermiteBoss;
import lanse.bossmobs.custombosses.nether.BlazeBoss;
import lanse.bossmobs.custombosses.nether.GhastBoss;
import lanse.bossmobs.custombosses.nether.MagmaTitan;
import lanse.bossmobs.custombosses.overworld.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

public class BossMobsCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("BossModeBossBarsOn(INCOMPLETE)")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        AttackHandler.bossBarsEnabled = true;
                        context.getSource().sendFeedback(() -> Text.literal("Bosses now have Boss Bars!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Player not found."));
                        return 0;
                    }}));

        dispatcher.register(literal("BossModeBossBarsOff(INCOMPLETE)")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        AttackHandler.bossBarsEnabled = false;
                        context.getSource().sendFeedback(() -> Text.literal("Bosses will not have Boss Bars!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Player not found."));
                        return 0;
                    }}));

        dispatcher.register(literal("BossModeAllNewMobsAreBosses(INCOMPLETE)")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        BossMobs.AllMobsAreBosses = true;
                        context.getSource().sendFeedback(() -> Text.literal("All new mobs spawning are bosses."), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Player not found."));
                        return 0;
                    }}));

        dispatcher.register(literal("BossModeAllNewMobsAreNotBosses(INCOMPLETE)")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        BossMobs.AllMobsAreBosses = false;
                        context.getSource().sendFeedback(() -> Text.literal("All new mobs spawning are not bosses."), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Player not found."));
                        return 0;
                    }}));

        dispatcher.register(literal("BossModeIsOn")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        BossMobs.bossMobsIsOn = true;
                        context.getSource().sendFeedback(() -> Text.literal("Boss Mobs is on."), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Player not found."));
                        return 0;
                    }}));

        dispatcher.register(literal("BossModeIsOff")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        BossMobs.bossMobsIsOn = false;
                        context.getSource().sendFeedback(() -> Text.literal("Boss Mobs is off."), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Player not found."));
                        return 0;
                    }}));

        dispatcher.register(literal("SummonGodSword")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        ServerWorld world = player.getServerWorld();
                        ArmorHandler.summonGodSword(player.getPos(), world);
                        context.getSource().sendFeedback(() -> Text.literal("God Sword has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon God Sword. Player not found."));
                        return 0;
                    }}));

        dispatcher.register(literal("SummonMaxNetheriteSet")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        ServerWorld world = player.getServerWorld();
                        ArmorHandler.summonMaxNetheriteSet(player.getPos(), world);
                        context.getSource().sendFeedback(() -> Text.literal("Max Netherite Set has been summoned!"), false);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon God Sword. Player not found."));
                        return 0;
                    }}));


        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Summon boss command registration list is below here.

        NostalgiaBosses.register(dispatcher); //These guys are from boss rain from mob rain (from Recreations of old lucky blocks)
        Bomby.register(dispatcher); //Creeper and charged Creeper (name from popularmmos). The first boss I made here.
        GhastBoss.register(dispatcher); //First fully custom with no inspiration. Just a hellish boss.

        SlimeTitan.register(dispatcher); //This guy is also from boss rain, but slightly stronger.
        MagmaTitan.register(dispatcher); //I wonder why this one wasn't from boss rain...

        ElderGuardianBoss.register(dispatcher); //Imagine actually getting mining fatigue... Or experience it from this lol
        GuardianBoss.register(dispatcher); //P O I N T Y    F I S H
        SquidBoss.register(dispatcher); //SQUIDWARD - I made this while waiting to get inside a dorm while waiting for marching band to start.
        GlowSquidBoss.register(dispatcher); //HOLY SQUIDWARD! - ITS GAME DAY!! Family weekend with da footballlllllllllllllllllll

        WitchBoss.register(dispatcher); //Thank goodness I remade potion rain. I copy pasted it lul

        SpiderBoss.register(dispatcher); //2AM Coding WOOOOH!!!!!!! I made this right after the Laser Class
        CaveSpiderBoss.register(dispatcher); //EEEEEEEEEEEEEEEEEEEEEEEE

        BlazeBoss.register(dispatcher); //H E L L

        SilverfishBoss.register(dispatcher); //THE FISH GOT OUT OF THE WATER!!!

        SnowGolemBoss.register(dispatcher); //FROSTY THE SNOWMAN!! ITS REAL!!!!!!!!!!!

        EndermiteBoss.register(dispatcher); //OOOOOOHHHHH ########### A RAT!!!

        //////////// ALPHA RELEASE ABOVE //////////////////////////////////////////////////

        PufferfishBoss.register(dispatcher); //OOOOOOHHHHH! (ty lucky blocks)
        BoatBoss.register(dispatcher); //YARRRRRRRRR ME WANT DA BOOTY I HAVE TORPEDO NOW DIE PESKY VIKING, PIRATES ARE BETTERRRR

        BatBoss.register(dispatcher); //Covid bossfight fr
        EndCrystalBoss.register(dispatcher); //Disco laser from hell? No wait from outer space, mb

        PhantomBoss.register(dispatcher); //Sleep deprivation from discrete structures? I swear why do I always use whatever is on my
        // mind, its like I am drunk coding or something

        EvokerBoss.register(dispatcher); //Procrastinating on 3 separate projects with this one lol also omg this has so many attacks
    }
}