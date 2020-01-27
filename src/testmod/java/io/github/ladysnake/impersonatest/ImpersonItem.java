package io.github.ladysnake.impersonatest;

import io.github.ladysnake.impersonate.Impersonator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ImpersonItem extends Item {
    public static final Identifier IMPERSONATION_KEY = Impersonatest.id("impersonitem");

    public ImpersonItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (Impersonator.get(user).stopImpersonation(IMPERSONATION_KEY) != null) {
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return super.use(world, user, hand);
    }

    @Override
    public boolean useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof ServerPlayerEntity) { // also checks server side
            Impersonator.get(user).impersonate(IMPERSONATION_KEY, ((PlayerEntity) entity).getGameProfile());
        }
        return super.useOnEntity(stack, user, entity, hand);
    }
}
