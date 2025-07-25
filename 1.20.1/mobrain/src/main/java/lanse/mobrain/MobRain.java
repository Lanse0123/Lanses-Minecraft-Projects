package lanse.mobrain;

import lanse.mobrain.custom_mobs.BOB;
import lanse.mobrain.custom_mobs.GoldenBabyZombie;
import lanse.mobrain.custom_mobs.SkeletonHorsemen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MobRain implements ModInitializer {

	private static final List<EntityType<?>> COMMON_MOBS = Arrays.asList(
			EntityType.ZOMBIE, EntityType.SPIDER, EntityType.SKELETON,
			EntityType.DROWNED, EntityType.HUSK, EntityType.CREEPER);

	private static final List<EntityType<?>> UNCOMMON_MOBS = Arrays.asList(
			EntityType.ENDERMAN, EntityType.CAVE_SPIDER,
			EntityType.STRAY, EntityType.SLIME, EntityType.WITCH);

	private static final List<EntityType<?>> RARE_MOBS = Arrays.asList(
			EntityType.IRON_GOLEM, EntityType.ZOGLIN,
			EntityType.GUARDIAN, EntityType.VINDICATOR);

	private static final List<EntityType<?>> LEGENDARY_MOBS = Arrays.asList(
			EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.RAVAGER,
			EntityType.SKELETON_HORSE);

	private static int tickCounter = 0;
	private static int waveNumber = 1;
	private static boolean isActive = false;

	@Override
	public void onInitialize() {
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> MobRainCommands.register(dispatcher));
	}

	private void onServerTick(MinecraftServer server) {
		if (BossRain.getActive() == true){
			BossRain.tickUpdate(server);
		}

		if (!isActive) {
			return;
		}
		tickCounter++;

		if (tickCounter >= 1200) { //Every minute, spawn a new wave
			tickCounter = 0;
			spawnMobRain(server);
			waveNumber++;
		}
	}

	private void spawnMobRain(MinecraftServer server) {
		Random random = new Random();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			World world = player.getEntityWorld();
			for (int i = 0; i < waveNumber; i++) {
				EntityType<?> entityType = getRandomEntityType(random);
				MobEntity mobEntity = (MobEntity) entityType.create(world);
				if (mobEntity != null) {

					//Spawn mobs in a ring around the player from 20 - 69 blocks away.
					double angle = random.nextDouble() * 2 * Math.PI; //Random angle
					double radius = 20 + random.nextDouble() * 49; //Random radius between 20 and 69
					double x = player.getX() + radius * Math.cos(angle); //I hate cos and sin
					double y = player.getY() + 10; //(CIRCLE IN MINECEAFT OMG)
					double z = player.getZ() + radius * Math.sin(angle); //Its a love hate thing
					mobEntity.setPosition(x, y, z); //Actually just a hate thing
					world.spawnEntity(mobEntity); //OH and this line spawns it in ITS THE GOAT

					// Equip skeletons with bows
					if (mobEntity instanceof SkeletonEntity) {
						(mobEntity).equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
					}

					// Set size for Slime
					if (mobEntity instanceof SlimeEntity) {
						((SlimeEntity) mobEntity).setSize(3, true); // Set the size to 3
					}

					// Handle Skeleton Horseman
					if (entityType == EntityType.SKELETON_HORSE) {
						SkeletonHorseEntity skeletonHorse = SkeletonHorsemen.createSkeletonHorseman(world);
						skeletonHorse.setPosition(x, y, z);
						world.spawnEntity(skeletonHorse);
					}

					//Handle BOB and Golden Baby Zombie
					if (entityType == EntityType.ZOMBIE && random.nextInt(300) == 69) { //1 in 300 chance to be BOB
						ZombieEntity bob = BOB.createBob(world);
						bob.setPosition(mobEntity.getX(), mobEntity.getY(), mobEntity.getZ());
						world.spawnEntity(bob);
						mobEntity.discard(); // Remove the original zombie entity
					}
					if (entityType == EntityType.ZOMBIE && random.nextInt(300) == 42) { //1 in 300 chance to be Baby
						ZombieEntity baby = GoldenBabyZombie.createGoldenBaby(world);
						baby.setPosition(mobEntity.getX(), mobEntity.getY(), mobEntity.getZ());
						world.spawnEntity(baby);
						mobEntity.discard();
					}
				}
			}
		}
	}

	private EntityType<?> getRandomEntityType(Random random) {
		int chance = random.nextInt(1000);
		if (chance < 900) { // 90% chance for common mobs
			return COMMON_MOBS.get(random.nextInt(COMMON_MOBS.size()));
		} else if (chance < 980) { // 8% chance for uncommon mobs
			return UNCOMMON_MOBS.get(random.nextInt(UNCOMMON_MOBS.size()));
		} else if (chance < 997) { // 1.7% chance for rare mobs
			return RARE_MOBS.get(random.nextInt(RARE_MOBS.size()));
		} else { // 0.3% chance for legendary mobs
			return LEGENDARY_MOBS.get(random.nextInt(LEGENDARY_MOBS.size()));
		}
	}

	public static void startMobRain() {
		isActive = true;
		tickCounter = 0;
		waveNumber = 1;
	}

	public static void stopMobRain() {
		isActive = false;
		tickCounter = 0;
		waveNumber = 1;
	}

	public static void setWave(int wave) {
		waveNumber = wave;
	}

	public static int getWave(){return waveNumber;}
}