package lanse.bossmobs.custombosses.overworld;

import com.mojang.brigadier.CommandDispatcher;
import lanse.bossmobs.AttackHandler;
import lanse.bossmobs.BossMobs;
import lanse.bossmobs.customattacks.Laser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.literal;

public class BatBoss extends BatEntity {

    public BatBoss(World world) {
        super(EntityType.BAT, world);

        this.setCustomName(Text.literal("BatBoss").formatted(Formatting.YELLOW));
        this.setCustomNameVisible(true);

        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));

        this.experiencePoints = 20;
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(40.0);
        this.setHealth(42.F);

        BossMobs.addId(this.getId());
    }
    public static void Covidify(ServerWorld world, LivingEntity boss) {

        //TODO - Add cool death animation here (AND LOOT)

        BossMobs.removeId(boss.getId());
    }

    public static void attack(ServerWorld world, LivingEntity boss) {
        //This is called each tick that the boss is loaded.

        if (!AttackHandler.checkForNearbyPlayers(world, boss, 15)) return;

        Random random = new Random();
        if (random.nextInt(75) == 25) {

            int attackIndex = random.nextInt(2); // 0 - 1

            switch (attackIndex) {
                case 0 -> AttackBlindingLaser(world, boss);
                case 1 -> AttackBite(world, boss);
            }
        }
    }
    private static void AttackBlindingLaser(ServerWorld world, LivingEntity boss) {
        Laser.BlindingLaser(world, boss, 3);
    }

    private static void AttackBite(ServerWorld world, LivingEntity boss) {

        Vec3d pos = boss.getPos();
        List<LivingEntity> nearbyEntities = world.getEntitiesByClass(LivingEntity.class, new Box(pos.subtract(5, 5, 5), pos.add(5, 5, 5)), e -> true);

        for (LivingEntity entity : nearbyEntities){
            entity.damage(new DamageSource(AttackHandler.bossDamageType), 4);
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 40, 1));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200, 1));
        }
    }

    public static void spawnReactor(Entity entity, ServerWorld world) {
        BatBoss bat = new BatBoss(world);
        bat.refreshPositionAndAngles((entity.getX()), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
        world.spawnEntity(bat);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("SummonBossBat")
                .requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        World world = player.getEntityWorld();
                        BatBoss bat = new BatBoss(world);
                        bat.refreshPositionAndAngles((player.getX() + 10), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        world.spawnEntity(bat);
                        context.getSource().sendFeedback(() -> Text.literal("Bat Boss has been summoned!"), true);
                        return 1;
                    } else {
                        context.getSource().sendError(Text.literal("Failed to summon Bat Boss. Player not found."));
                        return 0;
                    }
                }));
    }
}
