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
package io.github.ladysnake.impersonate.impl;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonate;
import io.github.ladysnake.impersonate.Impersonator;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerImpersonator implements Impersonator, EntitySyncedComponent {
    @NotNull
    private PlayerEntity player;
    @Nullable
    private GameProfile impersonatedProfile;
    @Nullable
    private GameProfile editedProfile;

    public PlayerImpersonator(@NotNull PlayerEntity player) {
        this.player = player;
    }

    @NotNull
    @Override
    public PlayerEntity getEntity() {
        return this.player;
    }

    @NotNull
    @Override
    public ComponentType<?> getComponentType() {
        return Impersonate.IMPERSONATION;
    }

    @Override
    public void impersonate(@NotNull GameProfile profile) {
        if (this.getImpersonatedProfile() != profile) {
            if (this.isImpersonating()) {
                this.stopImpersonation();
            }
            this.impersonatedProfile = profile;
            this.editedProfile = new GameProfile(this.getActualProfile().getId(), this.impersonatedProfile.getName());
            this.sync();
        }
    }

    @Override
    public void stopImpersonation() {
        if (this.isImpersonating()) {
            this.impersonatedProfile = null;
            this.editedProfile = null;
            this.sync();
        }
    }

    @Override
    public boolean isImpersonating() {
        return this.impersonatedProfile != null;
    }

    @Nullable
    @Override
    public GameProfile getImpersonatedProfile() {
        return this.impersonatedProfile;
    }

    @Override
    public @NotNull GameProfile getActualProfile() {
        return ((PlayerEntityExtensions) this.player).impersonate_getActualGameProfile();
    }

    @Override
    public @NotNull GameProfile getEditedProfile() {
        return this.editedProfile == null ? this.getActualProfile() : this.editedProfile;
    }

    private static final int ID_PRESENT = 0b01;
    private static final int NAME_PRESENT = 0b10;

    @Override
    public void writeToPacket(PacketByteBuf buf) {
        GameProfile profile = this.getImpersonatedProfile();
        UUID id = profile == null ? null : profile.getId();
        String name = profile == null ? null : profile.getName();
        buf.writeByte((id != null ? ID_PRESENT : 0) | (name != null ? NAME_PRESENT : 0));
        if (id != null) {
            buf.writeUuid(id);
        }
        if (name != null) {
            buf.writeString(name);
        }
    }

    @Override
    public void readFromPacket(PacketByteBuf buf) {
        byte flags = buf.readByte();
        UUID id = null;
        String name = null;
        if ((flags & ID_PRESENT) != 0) {
            id = buf.readUuid();
        }
        if ((flags & NAME_PRESENT) != 0) {
            name = buf.readString();
        }
        if (id != null || name != null) {
            this.impersonate(new GameProfile(id, name));
        } else {
            this.stopImpersonation();
        }
    }

    @Override
    public void fromTag(@NotNull CompoundTag tag) {
        GameProfile profile = NbtHelper.toGameProfile(tag);
        if (profile != null) {
            this.impersonate(profile);
        } else {
            this.stopImpersonation();
        }
    }

    @NotNull
    @Override
    public CompoundTag toTag(@NotNull CompoundTag tag) {
        if (this.impersonatedProfile != null) {
            NbtHelper.fromGameProfile(tag, this.impersonatedProfile);
        }
        return tag;
    }
}
