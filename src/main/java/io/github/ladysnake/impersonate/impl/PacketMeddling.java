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
package io.github.ladysnake.impersonate.impl;

import io.github.ladysnake.impersonate.Impersonate;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.mixin.PlayerListS2CPacketEntryAccessor;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public final class PacketMeddling {

    public static void resolvePlayerListEntries(PlayerListS2CPacket packet, ServerPlayerEntity player) {
        boolean reveal = ImpersonateTextContent.shouldBeRevealedBy(player);
        for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
            PlayerEntity playerEntry = player.server.getPlayerManager().getPlayer(entry.profileId());
            if (playerEntry != null) {
                Impersonator impersonator = Impersonate.IMPERSONATION.get(playerEntry);
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

    public static <P extends Packet<?>> P copyPacket(P packet, Function<PacketByteBuf, P> factory) {
        PacketByteBuf buf = PacketByteBufs.create();
        try {
            packet.write(buf);
            return factory.apply(buf);
        } finally {
            buf.release();
        }
    }

    public static ChatMessageS2CPacket resolveChatMessage(ChatMessageS2CPacket chatPacket, ServerPlayerEntity player) {
        @Nullable Text unsignedContent = Optional.ofNullable(chatPacket.unsignedContent()).map(t -> ((RecipientAwareText) t).impersonateResolveAll(player)).orElse(null);
        Text name = ((RecipientAwareText) chatPacket.serializedParameters().name()).impersonateResolveAll(player);
        @Nullable Text targetName = chatPacket.serializedParameters().targetName() instanceof RecipientAwareText t
            ? t.impersonateResolveAll(player)
            : null;

        // God, I wish we had a Record#copy method in this language
        // And yes we need to do a deep copy at the end, to avoid sharing text references
        return copyPacket(new ChatMessageS2CPacket(
            chatPacket.sender(),
            chatPacket.index(),
            chatPacket.signature(),
            chatPacket.body(),
            unsignedContent,
            chatPacket.filterMask(),
            new MessageType.Serialized(
                chatPacket.serializedParameters().typeId(),
                name,
                targetName
            )
        ), ChatMessageS2CPacket::new);
    }
}
