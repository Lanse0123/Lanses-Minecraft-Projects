package lanse.lanican.custom.item;

import lanse.lanican.custom.entity.HollowPurpleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class HollowPurpleItem extends Item {
    public HollowPurpleItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            Vec3d direction = player.getRotationVec(1.0F);
            Vec3d spawnPos = player.getEyePos();

            HollowPurpleEntity entity = new HollowPurpleEntity((ServerWorld) world, player, direction, false, true);
            entity.refreshPositionAndAngles(spawnPos.x, spawnPos.y, spawnPos.z, player.getYaw(), 0.0F);
            world.spawnEntity(entity);

            ItemStack stack = player.getStackInHand(hand);
            stack.damage(1, player, (p) -> p.sendToolBreakStatus(hand));
        }
        return TypedActionResult.success(player.getStackInHand(hand));
    }
}