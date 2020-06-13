/*
 * Impersonate
 * Copyright (C) 2020 Ladysnake
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
package io.github.ladysnake.impersonate.impl.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.ImpersonateText;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerListS2CPacket.class)
public abstract class PlayerListS2CPacketMixin {
    @Redirect(
        method = {
            "<init>(Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket$Action;[Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            "<init>(Lnet/minecraft/network/packet/s2c/play/PlayerListS2CPacket$Action;Ljava/lang/Iterable;)V"
        },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getGameProfile()Lcom/mojang/authlib/GameProfile;")
    )
    private GameProfile preventEasyReveal(ServerPlayerEntity serverPlayerEntity) {
        GameProfile impersonatedProfile = Impersonator.get(serverPlayerEntity).getImpersonatedProfile();
        if (impersonatedProfile != null && !ImpersonateText.shouldBeRevealedBy(serverPlayerEntity)) {
            return impersonatedProfile;
        }
        return serverPlayerEntity.getGameProfile();
    }
}
