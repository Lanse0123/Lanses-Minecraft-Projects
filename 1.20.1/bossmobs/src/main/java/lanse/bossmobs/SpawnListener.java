package lanse.bossmobs;

import lanse.bossmobs.customattacks.HomingFireball;
import lanse.bossmobs.customattacks.SmallFireball;
import lanse.bossmobs.custombosses.end.EndCrystalBoss;
import lanse.bossmobs.custombosses.end.EndermiteBoss;
import lanse.bossmobs.custombosses.nether.BlazeBoss;
import lanse.bossmobs.custombosses.nether.GhastBoss;
import lanse.bossmobs.custombosses.nether.MagmaTitan;
import lanse.bossmobs.custombosses.overworld.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Random;

public class SpawnListener {
    public SpawnListener(){
        ServerEntityEvents.ENTITY_LOAD.register(this::onEntitySpawn);
    }

    public static boolean updated = false;

    private void onEntitySpawn(Entity entity, ServerWorld world) {

        if (BossMobs.bossMobsIsOn){
            if (BossMobs.hasId(entity.getId())) {
                return;
            }

            tryProjectiles(entity, world);
            if (updated){
                return;
            }
        }

        if (BossMobs.AllMobsAreBosses) {

            if (entity == null || entity instanceof ItemEntity) {
                return;
            }

            Random random = new Random();
            if (random.nextInt(10) < 8) {
                entity.discard();
                return;
            }

            EntityType<?> entityType = entity.getType();

            ///////////////////////// Yet another long list of each boss conversion type... ////////////////////////////////

            if (entityType == EntityType.CREEPER) {
                Bomby.spawnReactor(entity, world);

            } else if (entityType == EntityType.ZOMBIE) {
                if (random.nextInt(100) == 25) {
                    NostalgiaBosses.spawnReactorKINGBOB(entity, world); //1 in 100 chance for King BOB per zombie.
                } else {
                    NostalgiaBosses.spawnReactorBOB(entity, world);
                }

            } else if (entityType == EntityType.SKELETON) {
                NostalgiaBosses.spawnReactorPETER(entity, world);

            } else if (entityType == EntityType.GHAST) {
                GhastBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.SLIME) {
                SlimeTitan.spawnReactor(entity, world);

            } else if (entityType == EntityType.MAGMA_CUBE) {
                MagmaTitan.spawnReactor(entity, world);

            } else if (entityType == EntityType.GUARDIAN) {
                GuardianBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.ELDER_GUARDIAN) {
                ElderGuardianBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.SQUID) {
                SquidBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.GLOW_SQUID) {
                GlowSquidBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.WITCH) {
                WitchBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.SPIDER) {
                SpiderBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.CAVE_SPIDER) {
                CaveSpiderBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.BLAZE) {
                BlazeBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.SILVERFISH) {
                SilverfishBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.SNOW_GOLEM) {
                SnowGolemBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.ENDERMITE) {
                EndermiteBoss.spawnReactor(entity, world);

                /////////////////////// ALPHA RELEASE ABOVE ////////////////////////////////////////////////////////

            } else if (entityType == EntityType.PUFFERFISH) {
                PufferfishBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.BOAT) {
                BoatBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.BAT) {
                BatBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.END_CRYSTAL) {
                EndCrystalBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.PHANTOM) {
                PhantomBoss.spawnReactor(entity, world);

            } else if (entityType == EntityType.EVOKER) {
                EvokerBoss.spawnReactor(entity, world);
            }


            entity.discard();
        }

        /** BOSS IDEAS!!!
         *-
         * PASSIVE MOBS:
         * All passive mobs get 5x more hp, and thorns 1.
         * All passive mobs set attacker to player, so they run away.
         * All passive mobs get speed 1.
         * All Villagers and wandering traders have more expensive trades
         * All Iron Golems attack players
         *-
         * Horse / Donkey / Mule: (FRIENDLY)
         * EarthQuake Stomp (when you jump) (slight damage + slight pushaway)
         * Kick / Bite
         *-
         * Llamas:
         * Super Powerful Spit
         *-
         * Cats:
         * Anti Air Gun towards Phantoms and PhantomBosses, and the ender dragon.
         * Scratch (if close)
         * Bite (if close)
         *-
         * Ocelots:
         * Anti Air Gun towards phantoms, phantom bosses, ender dragon, and elytras.
         * Powerful Scratch (if close)
         * Powerful Bite (if close)
         *-
         * Bees:
         * More Powerful Sting
         * Poison Laser
         *-
         * Enderman:
         * Much angrier
         * Teleport player to a random nearby spot
         * EntityGravity Pull towards enderman
         *-
         * Shulker:
         * Summon Enderman Boss
         * Recursive Bullets
         * Pushaway Blast
         *-
         * Zombie Pigman:
         * Automatically Agro towards nearest player within 120 blocks.
         * Has Speed
         * SlowPlayer (give nearby player hunger, slowness, and weakness)
         * Weak Pushaway
         *-
         * Stray:
         * Give Frozen ticks per hit (normal ability)
         * Anti Air gun to Elytra
         * Ice Beam
         * SlowPlayer (give nearby player hunger, slowness, and weakness)
         * Arrow Fountain
         * OP Bow
         * Frost Blast (works just like Ice Beam)
         *-
         * All drowneds have tridents (copy from BossRain), as well as some custom attacks:
         * Golden Armor (With trim)
         * Bubble Beam
         * Hydro Blast (Does AOE damage to nearby entities)
         * Channel Lightning (Strike nearby players with lightning)
         * DrownEnemy (EntityGravity.PushDown (POWER = 5))
         * SlowPlayer (Give nearby player hunger, slowness, and weakness)
         * Fish torpedo
         *-
         * Husks: (custom attacks)
         * Sand Firework
         * Sand Attack (blindness, slowness, and weakness)
         * Summon Vex Minions
         * SlowPlayer (Give nearby player hunger, slowness, and weakness)
         * Sand Circle
         * Sand Bomb
         * Crocodile line to player (like vanilla evoker)
         *-
         * EnderDragon: Set player velocity towards the dragon, so it always gets you near (or have this as an attack)
         * Recursive Fireballs with lingering fountains of harming and shockwaves on collision
         * Lingering Harming rain
         * Dive at Player
         * Summon End Crystal bosses
         * Summon Phantom Minions
         * Harming Laser
         *-
         * Wither:
         * Nearby players get wither effect (passive ability)
         * All black wither skulls are recursive (multiply)
         * All blue skulls are giant explosions like ghast
         * Summon Minions
         * Wither blast
         * Wither skull blast
         * Wither Laser
         * Meteor Shower (doesn't stop other attacks)(spawn skulls 100 blocks above all nearby players, give them random X and Z speed, and a constant down speed. 0 Drag.)
         *-
         * Wither Skeleton:
         * Nearby players get wither effect (passive ability)
         * Wither Laser
         * Blinding Laser
         * Undead EarthQuake
         * Black / Blue Wither Skull Projectile (Not recursive)
         *-
         * Illusioner: (1 in 15 chance of replacing an evoker)
         * Like a witch + evoker but more powerful in every way.
         *-
         * Warden: How can I turn Minecraft's scariest boss into something scarier
         * Sonic Explosion (Blast)
         * Has Speed
         * Has Lower HP, but good armor
         * Gives players Slowness and Nausea (Passive ability)
         */
    }

    private void tryProjectiles(Entity entity, ServerWorld world){

        EntityType<?> entityType = entity.getType();

        //Hey this is a shorter long list! MAYAHEEEEEEEE MAYAHAAAAAAAA MAYAHOOOOOO MAYA HAHA AYA EEEEEEE POYAOAAAAAAA EAIOU B
        if (entityType == EntityType.FIREBALL){
            HomingFireball.spawnReactor(entity, world);
        }
        if (entityType == EntityType.SMALL_FIREBALL) {
            SmallFireball.spawnReactor(entity, world);
        }
    }
}
