package lanse.greaterhorns;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.world.ServerWorld;

public class GreaterHorns implements ModInitializer {

	private static boolean HornsActivateSculk = false;
	private static boolean HornsHaveCooldowns = true;
	private static boolean HornsCompatibleWithDispensers = false;
	private static boolean HornsDispensedActivateSculk = false;

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> GreaterHorns.register(dispatcher));

		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (world.isClient) {
				return TypedActionResult.pass(player.getStackInHand(hand));
			}

			ItemStack itemStack = player.getStackInHand(hand);

			if (itemStack.getItem() != Items.GOAT_HORN) {
				return TypedActionResult.pass(itemStack);
			}

			if (HornsHaveCooldowns) {
				if (player.getItemCooldownManager().isCoolingDown(Items.GOAT_HORN)) {
					return TypedActionResult.fail(itemStack);
				}
			} else {
				player.getItemCooldownManager().remove(Items.GOAT_HORN);
			}

			if (HornsActivateSculk) {
				if (!player.getItemCooldownManager().isCoolingDown(Items.GOAT_HORN)) {
					activateSculkBlocks((ServerWorld) world, player.getBlockPos(), player.getPos());

					if (HornsHaveCooldowns) {
						player.getItemCooldownManager().set(Items.GOAT_HORN, 100);
					}
				}
			}
			return TypedActionResult.success(itemStack);
		});
	}

	private static void activateSculkBlocks(ServerWorld world, BlockPos playerPos, Vec3d playerVec) {
		int radius = 128;

		// Define the area to search for sculk sensors
		int startX = playerPos.getX() - radius;
		int startY = playerPos.getY() - 15;
		int startZ = playerPos.getZ() - radius;
		int endX = playerPos.getX() + radius;
		int endY = playerPos.getY() + 15;
		int endZ = playerPos.getZ() + radius;

		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				for (int z = startZ; z <= endZ; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					if (world.getBlockState(pos).getBlock() instanceof SculkSensorBlock) {
						summonProjectile(world, pos, playerVec);
					}
				}
			}
		}
	}

	private static void summonProjectile(ServerWorld world, BlockPos pos, Vec3d playerVec) {
		SnowballEntity snowball = new SnowballEntity(world, playerVec.x, playerVec.y, playerVec.z);
		snowball.updatePosition(pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5);
		snowball.setOwner(world.getClosestPlayer(playerVec.x, playerVec.y, playerVec.z, 128, false));
		world.spawnEntity(snowball);
	}

	private static void registerDispenserBehavior() {
		if (HornsCompatibleWithDispensers) {
			DispenserBlock.registerBehavior(Items.GOAT_HORN, new ItemDispenserBehavior() {
				@Override
				protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
					ServerWorld world = pointer.getWorld();
					BlockPos pos = pointer.getPos();
					Item item = stack.getItem();

					if (item instanceof GoatHornItem && stack.hasNbt()) {
						NbtCompound nbt = stack.getNbt();

						// Check if the NBT data contains the "instrument" key
						if (nbt != null && nbt.contains("instrument")) {
							// Get the instrument from the NBT data
							Identifier instrumentId = new Identifier(nbt.getString("instrument"));
							RegistryEntry<Instrument> instrumentEntry = Registries.INSTRUMENT.getEntry(RegistryKey.of(Registries.INSTRUMENT.getKey(), instrumentId)).orElse(null);

							if (instrumentEntry != null) {
								RegistryEntry<SoundEvent> soundEventEntry = instrumentEntry.value().soundEvent();
								SoundEvent hornSound = soundEventEntry.value();

								for (ServerPlayerEntity player : world.getPlayers()) {
									if (player.getBlockPos().isWithinDistance(pos, 128)) {
										player.playSound(hornSound, SoundCategory.PLAYERS, 1.0F, 1.0F);
									}
								}
								if (HornsDispensedActivateSculk) {
									activateSculkBlocks(world, pos, Vec3d.ofCenter(pos));
								}
							}
						}
					}
					return stack;
				}
			});
		} else {
			DispenserBlock.registerBehavior(Items.GOAT_HORN, new ItemDispenserBehavior() {
				@Override
				protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
					// Default behavior (dispense the item)
					return super.dispenseSilently(pointer, stack);
				}
			});
		}
	}


	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("HornsActivateSculk")
				.requires(source -> source.hasPermissionLevel(2))
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("true")
						.executes(context -> {
							HornsActivateSculk = true;
							ServerCommandSource source = context.getSource();
							source.sendFeedback(() -> Text.of("Goat Horns activate Sculk Sensors."), true);
							return 1;
						})
				)
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("false")
						.executes(context -> {
							HornsActivateSculk = false;
							ServerCommandSource source = context.getSource();
							source.sendFeedback(() -> Text.of("Goat Horns do not activate Sculk Sensors."), true);
							return 1;
						})));

		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("HornsHaveCooldown")
				.requires(source -> source.hasPermissionLevel(2))
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("true")
						.executes(context -> {
							HornsHaveCooldowns = true;
							ServerCommandSource source = context.getSource();
							source.sendFeedback(() -> Text.of("Goat Horns have cooldowns."), true);
							return 1;
						})
				)
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("false")
						.executes(context -> {
							HornsHaveCooldowns = false;
							ServerCommandSource source = context.getSource();
							source.sendFeedback(() -> Text.of("Goat Horns do not have cooldowns."), true);
							return 1;
						})));

		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("HornsCompatibleWithDispensers")
				.requires(source -> source.hasPermissionLevel(2))
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("true")
						.executes(context -> {
							HornsCompatibleWithDispensers = true;
							registerDispenserBehavior();
							ServerCommandSource source = context.getSource();
							source.sendFeedback(() -> Text.of("Goat Horns can be played by Dispensers."), true);
							return 1;
						})
				)
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("false")
						.executes(context -> {
							HornsCompatibleWithDispensers = false;
							registerDispenserBehavior();
							ServerCommandSource source = context.getSource();
							source.sendFeedback(() -> Text.of("Goat Horns can not be played by Dispensers."), true);
							return 1;
						})));

		dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("HornsDispensedActivateSculk")
				.requires(source -> source.hasPermissionLevel(2))
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("true")
						.executes(context -> {
							HornsDispensedActivateSculk = true;
							registerDispenserBehavior();
							ServerCommandSource source = context.getSource();
							source.sendFeedback(() -> Text.of("Goat Horns played by Dispensers activate sculk."), true);
							return 1;
						})
				)
				.then(LiteralArgumentBuilder.<ServerCommandSource>literal("false")
						.executes(context -> {
							HornsDispensedActivateSculk = false;
							registerDispenserBehavior();
							ServerCommandSource source = context.getSource();
							source.sendFeedback(() -> Text.of("Goat Horns played by Dispensers do not activate sculk."), true);
							return 1;
						})));
	}
}