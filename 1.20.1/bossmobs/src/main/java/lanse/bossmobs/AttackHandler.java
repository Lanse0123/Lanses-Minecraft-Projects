package lanse.bossmobs;

import lanse.bossmobs.customattacks.FishTorpedo;
import lanse.bossmobs.customattacks.HomingArrow;
import lanse.bossmobs.custombosses.end.EndCrystalBoss;
import lanse.bossmobs.custombosses.end.EndermiteBoss;
import lanse.bossmobs.custombosses.nether.BlazeBoss;
import lanse.bossmobs.custombosses.overworld.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class AttackHandler {

    public static boolean bossBarsEnabled = false;
    public static RegistryEntry<DamageType> bossDamageType;

    public static void HandleAttack(int id, ServerWorld world, MinecraftServer server) {

        //Damage initializer. Only runs once.
        if (!BossMobs.bossDamageDefined) {
            bossDamageType = world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).getEntry(DamageTypes.MAGIC).orElseThrow();
            BossMobs.bossDamageDefined = true;
        }

        Entity entity = world.getEntityById(id);
        if (entity != null) {

            EntityType<?> entityType = entity.getType();

            //Long list of every attack call goes down here...
            if (entityType == EntityType.CREEPER){
                Bomby.attack( world, (LivingEntity) entity, server); return;
            }
            if (entityType == EntityType.ELDER_GUARDIAN){
                ElderGuardianBoss.attack(world, (LivingEntity) entity, server); return;
            }
            if (entityType == EntityType.GUARDIAN){
                GuardianBoss.attack(world, (LivingEntity) entity, server); return;
            }
            if (entityType == EntityType.SQUID){
                SquidBoss.attack( world, (LivingEntity) entity); return;
            }
            if (entityType == EntityType.GLOW_SQUID){
                GlowSquidBoss.attack(world, (LivingEntity) entity); return;
            }
            if (entityType == EntityType.WITCH){
                WitchBoss.attack(world, (LivingEntity) entity); return;
            }
            if (entityType == EntityType.SPIDER){
                SpiderBoss.attack(world, (LivingEntity) entity); return;
            }
            if (entityType == EntityType.CAVE_SPIDER){
                CaveSpiderBoss.attack(world, (LivingEntity) entity); return;
            }
            if (entityType == EntityType.BLAZE){
                BlazeBoss.attack(world, (LivingEntity) entity); return;
            }
            if (entityType == EntityType.SILVERFISH){
                SilverfishBoss.attack(world, (LivingEntity) entity); return;
            }
            if (entityType == EntityType.SNOW_GOLEM){
                SnowGolemBoss.attack(world, (LivingEntity) entity); return;
            }
            if (entityType == EntityType.ENDERMITE){
                EndermiteBoss.attack(world, (LivingEntity) entity); return;
            }

            ////////////////////// ALPHA RELEASE ABOVE //////////////////////////////////////////

            if (entityType == EntityType.PUFFERFISH){
                PufferfishBoss.attack(world, (LivingEntity) entity); return;
            }
            if (entityType == EntityType.BOAT){
                BoatBoss.attack(world, entity); return;
            }
            if (entityType == EntityType.BAT){
                BatBoss.attack(world, (LivingEntity) entity); return;
            }
            if (entityType == EntityType.END_CRYSTAL){
                EndCrystalBoss.attack(world, entity); return;
            }
            if (entityType == EntityType.PHANTOM){
                PhantomBoss.attack(world, (LivingEntity) entity); return;
            }
            if (entityType == EntityType.EVOKER){
                EvokerBoss.attack(world, (LivingEntity) entity); return;
            }
        }
    }
    public static void HandleMinion(int id, ServerWorld world) {

        Entity entity = world.getEntityById(id);
        if (entity != null) {
            EntityType<?> entityType = entity.getType();

            //Not as long but still long list of all Entity types to handle for Minions.
            if (entityType == EntityType.COD) {
                FishTorpedo.tick(world, (LivingEntity) entity);
            }
            if (entityType == EntityType.ARROW) {
                HomingArrow.tick(world, entity);
            }
        }
    }
    public static boolean checkForNearbyPlayers(ServerWorld world, Entity boss, int r){

        //r = radius
        Vec3d pos = boss.getPos();
        List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(PlayerEntity.class, new Box(pos.subtract(r, r, r), pos.add(r, r, r)), e -> true);

        return !nearbyPlayers.isEmpty();
    }
}