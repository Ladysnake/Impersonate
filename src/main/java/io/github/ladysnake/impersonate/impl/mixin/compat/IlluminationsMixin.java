/*
 * Impersonate
 * Copyright (C) 2020-2021 Ladysnake
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
package io.github.ladysnake.impersonate.impl.mixin.compat;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonator;
import ladysnake.illuminations.client.Illuminations;
import ladysnake.illuminations.client.data.PlayerCosmeticData;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(Illuminations.class)
public abstract class IlluminationsMixin {
    @Shadow
    private static Map<UUID, PlayerCosmeticData> PLAYER_COSMETICS;

    @Inject(method = "getCosmeticData", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getUuid()Ljava/util/UUID;"), cancellable = true, require = 0)
    private static void spoofUuid(PlayerEntity player, CallbackInfoReturnable<@Nullable PlayerCosmeticData> cir) {
        GameProfile impersonatedProfile = Impersonator.get(player).getImpersonatedProfile();
        if (impersonatedProfile != null) {
            if (player.isPartVisible(PlayerModelPart.CAPE)) {
                // if impersonate:showCapes is false, capes will never be visible, and cosmetics should not be either
                // if it is true, impersonators have to choose between both cape and cosmetics, or neither
                // not perfect but synchronizing the gamerule would be quite annoying for little benefit
                cir.setReturnValue(PLAYER_COSMETICS.get(impersonatedProfile.getId()));
            } else {
                cir.setReturnValue(null);
            }
        }
    }
}
