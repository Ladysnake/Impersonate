/*
 * Impersonate
 * Copyright (C) 2020-2026 Ladysnake
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

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.ladysnake.impersonate.Impersonate;

public final class ReloadSkinPacket implements CustomPayload {
    public static final CustomPayload.Id<ReloadSkinPacket> ID = new CustomPayload.Id<>(Impersonate.id("impersonation"));
    public static final ReloadSkinPacket INSTANCE = new ReloadSkinPacket();
    public static final PacketCodec<ByteBuf, ReloadSkinPacket> CODEC = PacketCodec.unit(INSTANCE);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private ReloadSkinPacket() {}
}
