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
package org.ladysnake.impersonate.impl.mixin;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.ladysnake.impersonate.Impersonator;
import org.ladysnake.impersonate.impl.PacketMeddling;
import org.ladysnake.impersonate.impl.RecipientAwareText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin {
    @Shadow
    @Final
    protected MinecraftServer server;

    @ModifyArg(method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V"))
    private Packet<?> resolveFakeTextsInPackets(Packet<?> packet) {
        if (((Object) this) instanceof ServerPlayNetworkHandler self) {
            ServerPlayerEntity player = self.player;
            if (packet instanceof ChatMessageS2CPacket chatPacket) {
                if (this.existsImpersonator()) {
                    return PacketMeddling.resolveChatMessage(chatPacket, player);
                }
            } else if (packet instanceof GameMessageS2CPacket gamePacket) {
                if (this.existsImpersonator()) {
                    Text resolvedText = ((RecipientAwareText) gamePacket.content()).impersonateResolveAll(player);
                    return new GameMessageS2CPacket(resolvedText, gamePacket.overlay());
                }
            } else if (packet instanceof PlayerListS2CPacket listPacket) {
                if (this.existsImpersonator()) {
                    PlayerListS2CPacket copy = PacketMeddling.copyPacket(listPacket, PlayerListS2CPacket::new);
                    PacketMeddling.resolvePlayerListEntries(copy, player);
                    return copy;
                }
            }

        }
        return packet;
    }

    @Unique
    private boolean existsImpersonator() {
        for (ServerPlayerEntity player : this.server.getPlayerManager().getPlayerList()) {
            if (Impersonator.get(player).isImpersonating()) {
                return true;
            }
        }
        return false;
    }
}
