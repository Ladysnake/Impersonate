/*
 * Impersonate
 * Copyright (C) 2020-2024 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.ladysnake.impersonatest;

import io.github.ladysnake.impersonate.Impersonator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
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
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof ServerPlayerEntity) { // also checks server side
            Impersonator.get(user).impersonate(IMPERSONATION_KEY, ((PlayerEntity) entity).getGameProfile());
        }
        return super.useOnEntity(stack, user, entity, hand);
    }
}
