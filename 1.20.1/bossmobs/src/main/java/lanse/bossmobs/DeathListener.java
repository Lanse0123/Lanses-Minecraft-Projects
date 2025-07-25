package lanse.bossmobs;

import lanse.bossmobs.customattacks.FishTorpedo;
import lanse.bossmobs.custombosses.end.EndCrystalBoss;
import lanse.bossmobs.custombosses.end.EndermiteBoss;
import lanse.bossmobs.custombosses.nether.BlazeBoss;
import lanse.bossmobs.custombosses.nether.GhastBoss;
import lanse.bossmobs.custombosses.nether.MagmaTitan;
import lanse.bossmobs.custombosses.overworld.*;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;

public class DeathListener {

    public DeathListener() {
        ServerLivingEntityEvents.AFTER_DEATH.register(this::onEntityDeath);
    }

    private void onEntityDeath(Entity entity, DamageSource damageSource) {
        if (BossMobs.bossMobsIsOn) {

            if (entity != null && BossMobs.hasId(entity.getId())) {

                EntityType<?> entityType = entity.getType();
                LivingEntity livingEntity = (LivingEntity) entity;

                // Long list of death effects below.
                if (entityType == EntityType.CREEPER) {
                    Bomby.obliterate((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.GHAST){
                    GhastBoss.annihilate((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.SLIME){
                    SlimeTitan.systematicallyDestroy((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.MAGMA_CUBE){
                    MagmaTitan.terminate((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.GUARDIAN){
                    GuardianBoss.Eradicate((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.ELDER_GUARDIAN){
                    ElderGuardianBoss.Exterminate((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.SQUID){
                    SquidBoss.Vaporize((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.GLOW_SQUID){
                    GlowSquidBoss.Decapitate((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.WITCH){
                    WitchBoss.BurnAtTheStake((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.SPIDER){
                    SpiderBoss.Decimate((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.CAVE_SPIDER){
                    CaveSpiderBoss.Neutralize((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.BLAZE){
                    BlazeBoss.Extinguish((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.SILVERFISH){
                    SilverfishBoss.Eliminate((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.SNOW_GOLEM){
                    SnowGolemBoss.Melt((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.ENDERMITE){
                    EndermiteBoss.Devastate((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }

                /////////////////////// ALPHA RELEASE ABOVE ////////////////////////////////////////////////////////

                if (entityType == EntityType.PUFFERFISH){
                    PufferfishBoss.Deoxygenate((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.BOAT){
                    BoatBoss.Deconstruct((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.BAT){
                    BatBoss.Covidify((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.END_CRYSTAL){
                    EndCrystalBoss.Kaboom((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.PHANTOM){
                    PhantomBoss.Realitify((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }
                if (entityType == EntityType.EVOKER){
                    EvokerBoss.TotemOfDying((ServerWorld) livingEntity.getWorld(), livingEntity); return;
                }

                /** Other Cool death words
                 * Exile
                 * Disintegrate
                 * Eviscerate
                 * Desolate
                 * Despoil
                 */
            }

            if (entity != null && BossMobs.hasMinionID(entity.getId())) {

                EntityType<?> entityType = entity.getType();
                LivingEntity livingEntity = (LivingEntity) entity;

                if (entityType == EntityType.COD){
                    FishTorpedo.detonate((ServerWorld) livingEntity.getWorld(), livingEntity, 5); return;
                }

                BossMobs.removeMinionID(livingEntity.getId());
            }
        }
    }
}