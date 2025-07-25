package lanse.lanses.challenge.modpack.challenges.mobexploder;

import com.mojang.brigadier.CommandDispatcher;
import lanse.lanses.challenge.modpack.MainControl;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class MobExploder {
    public static void onLivingEntityDeath(LivingEntity entity, ServerWorld world) {

        //If a creeper or EnderDragon dies, spawn a nuke lol
        if (entity instanceof EnderDragonEntity || entity instanceof CreeperEntity) {
            world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), 50.0F, World.ExplosionSourceType.TNT);
        } else {
            // Trigger a regular explosion with the power of an end crystal (size 6)
            world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), 6.0F, World.ExplosionSourceType.TNT);
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(CommandManager.literal("LCP_Preset_MobExploder")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    MainControl.modPreset = MainControl.Preset.MOBEXPLODER;
                    context.getSource().sendFeedback(() -> Text.literal("Challenge Mod Preset set to MobExploder!"), true);
                    return 1;
                }));
    }
}