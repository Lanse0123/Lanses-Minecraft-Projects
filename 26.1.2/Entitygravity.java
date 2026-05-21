package lanse.entitygravity;

import com.mojang.brigadier.CommandDispatcher;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Entitygravity implements ModInitializer {

	/** This is my best recreation of the original I could make. 26.1.2 is a lot different in
	 * the code base, and I need to get used to it again since ive been making my own game. **/

	private static double GRAVITATIONAL_CONSTANT = 0.75;
	private static final double GRAVITATIONAL_SPEED_LIMIT = 0.25;
	private static final double MAX_DISTANCE = 85.0;
	public static int tickCount = 0;

	public static boolean entityGravityOn = false;
	public static boolean playerGravityOn = false;
	public static boolean creativeAffected = false;

	@Override
	public void onInitialize() {
		ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
	}

	private void onServerTick(MinecraftServer server) {
		if (!entityGravityOn && !playerGravityOn) {
			return;
		}

		tickCount++;

		for (ServerLevel level : server.getAllLevels()) {
			List<Entity> entities = new ArrayList<>();
			level.getAllEntities().forEach(entities::add);

			for (Entity entityA : entities) {
				if (!entityA.isAlive()) continue;
				boolean entityAPulls = !(entityA instanceof ItemEntity);

				// In player gravity mode, only players act as gravity sources
				if (playerGravityOn) {
					if (!(entityA instanceof Player)) {
						continue;
					}
				}

				for (Entity entityB : entities) {
					if (entityA == entityB) continue;
					if (!entityB.isAlive()) continue;

					// Ignore creative players unless enabled
					if (!creativeAffected && entityB instanceof Player player && player.getAbilities().instabuild) {
						continue;
					}

					double dx = entityA.getX() - entityB.getX();
					double dy = entityA.getY() - entityB.getY();
					double dz = entityA.getZ() - entityB.getZ();

					double distanceSq = dx * dx + dy * dy + dz * dz;

					if (distanceSq > MAX_DISTANCE * MAX_DISTANCE) {
						continue;
					}

					distanceSq = Math.max(distanceSq, 1.5);

					if (entityAPulls) {

						double distance = Math.sqrt(distanceSq);

						Vec3 direction = new Vec3(dx / distance, dy / distance, dz / distance);

						// Hard speed cap because chicken jockey 10E289 is bad for minecraft
						double force = Math.min(GRAVITATIONAL_CONSTANT / distanceSq, GRAVITATIONAL_SPEED_LIMIT);
						Vec3 velocityChange = direction.scale(force);
						entityB.addDeltaMovement(velocityChange);

						// Marks movement update for clients
						entityB.hurtMarked = true;
					}
				}
			}
		}
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("EntityGravityOn").executes(context -> {
			entityGravityOn = true;
			playerGravityOn = false;
			context.getSource().sendSuccess(() -> Component.literal("Gravity Simulator Enabled!"), true);
			return 1;
		}));

		dispatcher.register(Commands.literal("EntityGravityOff").executes(context -> {
			entityGravityOn = false;
			context.getSource().sendSuccess(() -> Component.literal("Gravity Simulator Disabled."), true);
			return 1;
		}));

		dispatcher.register(Commands.literal("PlayerGravityOn").executes(context -> {
			playerGravityOn = true;
			entityGravityOn = false;
			context.getSource().sendSuccess(() -> Component.literal("Player Gravity Enabled!"), true);
			return 1;
		}));

		dispatcher.register(Commands.literal("PlayerGravityOff").executes(context -> {
			playerGravityOn = false;
			context.getSource().sendSuccess(() -> Component.literal("Player Gravity Disabled."), true);
			return 1;
		}));

		dispatcher.register(Commands.literal("EntityGravityAffectsCreative").then(Commands.argument
				("enabled", BoolArgumentType.bool()).executes(context -> {
					creativeAffected = BoolArgumentType.getBool(context, "enabled");
					context.getSource().sendSuccess(() -> Component.literal("Creative affected by gravity is now " + creativeAffected + "."), true);
					return 1;
				})));

		dispatcher.register(Commands.literal("EntityGravityDifficulty")
						.then(Commands.argument("difficulty", StringArgumentType.word()).suggests((context, builder) ->
										SharedSuggestionProvider.suggest(new String[]{"easy", "normal", "hard", "impossible"}, builder)).executes(context -> {
									String difficulty = StringArgumentType.getString(context, "difficulty").toLowerCase();

									switch (difficulty) {
										case "easy" -> GRAVITATIONAL_CONSTANT = 0.55;
										case "normal" -> GRAVITATIONAL_CONSTANT = 0.75;
										case "hard" -> GRAVITATIONAL_CONSTANT = 1.0;
										case "impossible" -> GRAVITATIONAL_CONSTANT = 2.069;
										default -> {
											context.getSource().sendFailure(Component.literal("Valid difficulties: easy, normal, hard, impossible"));
											return 0;
										}
									}

									context.getSource().sendSuccess(() -> Component.literal("Gravity difficulty set to " + difficulty + "."), true);
									return 1;
								})));
	}
}