package lanse.lanican.custom.item;

import lanse.lanican.custom.entity.HollowJudgementEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class HollowJudgementItem extends Item {
    public HollowJudgementItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            // Check if the item is on cooldown
            if (player.getItemCooldownManager().isCoolingDown(this)) {
                return TypedActionResult.fail(player.getStackInHand(hand));
            }

            // Spawn the HollowJudgementEntity
            HollowJudgementEntity entity = new HollowJudgementEntity((ServerWorld) world, player);
            world.spawnEntity(entity);

            // Damage the item and set cooldown
            ItemStack stack = player.getStackInHand(hand);
            stack.damage(1, player, p -> p.sendToolBreakStatus(hand)); // Notify when the item breaks
            player.getItemCooldownManager().set(this, 100); // Cooldown duration (100 ticks = 5 seconds)
        }

        return TypedActionResult.success(player.getStackInHand(hand));
    }
}