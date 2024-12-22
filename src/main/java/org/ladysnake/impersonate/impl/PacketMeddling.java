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
package org.ladysnake.impersonate.impl;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.impersonate.Impersonator;
import org.ladysnake.impersonate.impl.mixin.PlayerListS2CPacketEntryAccessor;

import java.util.Optional;

public final class PacketMeddling {

    public static void resolvePlayerListEntries(PlayerListS2CPacket packet, ServerPlayerEntity player) {
        boolean reveal = ImpersonateTextContent.shouldBeRevealedBy(player);
        for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
            PlayerEntity playerEntry = player.server.getPlayerManager().getPlayer(entry.profileId());
            if (playerEntry != null) {
                Impersonator impersonator = Impersonator.get(playerEntry);
                if (impersonator.isImpersonating()) {
                    // OPs get the true profile with semi-fake display name, others get a complete lie
                    PlayerListS2CPacketEntryAccessor accessibleEntry = (PlayerListS2CPacketEntryAccessor) (Object) entry;
                    if (reveal) {
                        accessibleEntry.setDisplayName(MutableText.of(ImpersonateTextContent.get(playerEntry, true)));
                    } else {
                        accessibleEntry.setProfile(impersonator.getEditedProfile());
                    }
                }
            }
        }
    }

    public static <P extends Packet<?>> P copyPacket(P packet, PacketCodec<? super RegistryByteBuf, P> codec, DynamicRegistryManager dynamicRegistryManager) {
        RegistryByteBuf buf = new RegistryByteBuf(PacketByteBufs.create(), dynamicRegistryManager);
        try {
            codec.encode(buf, packet);
            return codec.decode(buf);
        } finally {
            buf.release();
        }
    }

    public static ChatMessageS2CPacket resolveChatMessage(ChatMessageS2CPacket chatPacket, ServerPlayerEntity player) {
        CommandOutput commandOutput = player.getCommandOutput();
        @Nullable Text unsignedContent = Optional.ofNullable(chatPacket.unsignedContent()).map(t -> ((RecipientAwareText) t).impersonateResolveAll(commandOutput)).orElse(null);
        Text name = ((RecipientAwareText) chatPacket.serializedParameters().name()).impersonateResolveAll(commandOutput);
        Optional<Text> targetName = chatPacket.serializedParameters().targetName().map(text -> text instanceof RecipientAwareText t
            ? t.impersonateResolveAll(commandOutput)
            : null);


        // God, I wish we had a Record#copy method in this language
        // And yes we need to do a deep copy at the end, to avoid sharing text references
        return copyPacket(new ChatMessageS2CPacket(
            chatPacket.sender(),
            chatPacket.index(),
            chatPacket.signature(),
            chatPacket.body(),
            unsignedContent,
            chatPacket.filterMask(),
            new MessageType.Parameters(
                chatPacket.serializedParameters().type(),
                name,
                targetName
            )
        ), ChatMessageS2CPacket.CODEC, player.getRegistryManager());
    }
}
