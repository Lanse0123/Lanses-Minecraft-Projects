package lanse.mobrain;

import lanse.mobrain.custom_mobs.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BossRain{

    private static final List<EntityType<?>> COMMON_MOBS = Arrays.asList(
            EntityType.ZOMBIE, EntityType.RAVAGER, EntityType.CREEPER, EntityType.SLIME,
            EntityType.EVOKER, EntityType.GUARDIAN, EntityType.ILLUSIONER, EntityType.DROWNED);

    private static final List<EntityType<?>> UNCOMMON_MOBS = Arrays.asList(
            EntityType.WARDEN, EntityType.GHAST, EntityType.SKELETON_HORSE,
            EntityType.SKELETON);

    private static final List<EntityType<?>> RARE_MOBS = Arrays.asList(
            EntityType.WITHER, EntityType.ELDER_GUARDIAN);

    private static int tickCounter = 0;
    private static int waveNumber = 1;
    private static boolean isActive = false;

    //Called each tick from MobRain to keep it updated.
    public static void tickUpdate(MinecraftServer server) {
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

    private static void spawnMobRain(MinecraftServer server) {
        Random random = new Random();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            World world = player.getEntityWorld();
            for (int i = 0; i < waveNumber; i++) {
                EntityType<?> entityType = getRandomEntityType(random);
                MobEntity mobEntity = (MobEntity) entityType.create(world);
                if (mobEntity != null) {

                    //Spawn mobs in a ring around the player from 20 - 50 blocks away.
                    double angle = random.nextDouble() * 2 * Math.PI; //Random angle
                    double radius = 20 + random.nextDouble() * 30; //Random radius between 20 and 50
                    double x = player.getX() + radius * Math.cos(angle); //I hate cos and sin
                    double y = player.getY() + 10; //(CIRCLE IN MINECEAFT OMG)
                    double z = player.getZ() + radius * Math.sin(angle); //Its a love hate thing
                    mobEntity.setPosition(x, y, z); //Actually just a hate thing
                    world.spawnEntity(mobEntity); //OH and this line spawns it in ITS THE GOAT

                    // Equip drowneds with tridents
                    if (mobEntity instanceof DrownedEntity) {
                        (mobEntity).equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
                    }

                    // Handle Skeleton Horseman
                    if (entityType == EntityType.SKELETON_HORSE) {
                        SkeletonHorseEntity skeletonHorse = SkeletonHorsemen.createSkeletonHorseman(world);
                        skeletonHorse.setPosition(x, y, z);
                        world.spawnEntity(skeletonHorse);
                    }

                    // Set size for Slime
                    if (mobEntity instanceof SlimeEntity) {
                        ((SlimeEntity) mobEntity).setSize(8, true);
                    }

                    // Handle PETER
                    if (entityType == EntityType.SKELETON) {
                        SkeletonEntity peter = PETER.createPeter((ServerWorld) world);
                        peter.setPosition(mobEntity.getX(), mobEntity.getY(), mobEntity.getZ());
                        world.spawnEntity(peter);
                        mobEntity.discard(); // Remove the original skeleton entity
                    }

                    // Make all creepers charged
                    if (mobEntity instanceof CreeperEntity creeper) {
                        creeper.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 200, 0));
                        creeper.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 30, 1));
                        creeper.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 5, 5));

                        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
                        if (lightning != null) {
                            lightning.refreshPositionAfterTeleport(creeper.getX(), creeper.getY(), creeper.getZ());
                            world.spawnEntity(lightning);
                            creeper.setHealth(creeper.getMaxHealth());
                        }
                    }

                    // Handle Warden
                    if (mobEntity instanceof WardenEntity warden) {
                        warden.setCustomName(Text.literal("ROBERT").formatted(Formatting.DARK_BLUE));
                        warden.setCustomNameVisible(true);
                        warden.setPersistent();
                    }

                    // Handle BOB, King BOB, and the baby zombie
                    if (entityType == EntityType.ZOMBIE){
                        if (random.nextInt(100) == 69){
                            ZombieEntity Kingbob = KingBOB.createKingBob(world);
                            Kingbob.setPosition(mobEntity.getX(), mobEntity.getY(), mobEntity.getZ());
                            world.spawnEntity(Kingbob);
                            mobEntity.discard(); // Remove the original zombie entity
                        } else {
                            if (random.nextInt(2) == 1) {
                                ZombieEntity bob = BOB.createBob(world);
                                bob.setPosition(mobEntity.getX(), mobEntity.getY(), mobEntity.getZ());
                                world.spawnEntity(bob);
                                mobEntity.discard();
                            } else {
                                ZombieEntity baby = GoldenBabyZombie.createGoldenBaby(world);
                                baby.setPosition(mobEntity.getX(), mobEntity.getY(), mobEntity.getZ());
                                world.spawnEntity(baby);
                                mobEntity.discard();
                            }
                        }
                    }
                }
            }
        }
    }

    private static EntityType<?> getRandomEntityType(Random random) {
        int chance = random.nextInt(1000);
        if (chance < 900) { // 90% chance for common mobs
            return COMMON_MOBS.get(random.nextInt(COMMON_MOBS.size()));
        } else if (chance < 990) { // 9% chance for uncommon mobs
            return UNCOMMON_MOBS.get(random.nextInt(UNCOMMON_MOBS.size()));
        } else { //1% chance for Rare mobs
            return RARE_MOBS.get(random.nextInt(RARE_MOBS.size()));
        }
    }

    public static void startBossRain() {
        isActive = true;
        tickCounter = 0;
        waveNumber = 1;
    }

    public static void stopBossRain() {
        isActive = false;
        tickCounter = 0;
        waveNumber = 1;
    }

    public static void setWave(int wave) {waveNumber = wave;}

    public static int getWave(){return waveNumber;}

    public static boolean getActive(){return isActive;}
}