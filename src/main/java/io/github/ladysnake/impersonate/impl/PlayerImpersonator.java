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
import net.minecraft.client.network.packet.PlayerListS2CPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class PlayerImpersonator implements Impersonator, EntitySyncedComponent {
    @NotNull
    private PlayerEntity player;
    @Nullable
    private GameProfile impersonatedProfile;
    @Nullable
    private GameProfile editedProfile;
    private boolean fakeCape;

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
            assert this.impersonatedProfile != null;
            // if the player was the only one impersonating
            updatePlayerLists(PlayerListS2CPacket.Action.REMOVE_PLAYER);
            this.impersonatedProfile = null;
            this.editedProfile = null;
            updatePlayerLists(PlayerListS2CPacket.Action.ADD_PLAYER);
            this.sync();
        }
    }

    private void updatePlayerLists(PlayerListS2CPacket.Action action) {
        if (!player.world.isClient) {
            PlayerManager playerManager = ((ServerPlayerEntity) player).server.getPlayerManager();
            if (isAloneOnServer(playerManager)) {
                playerManager.sendToAll(new PlayerListS2CPacket(action, (ServerPlayerEntity) this.player));
            }
        }
    }

    private boolean isAloneOnServer(PlayerManager playerManager) {
        for (ServerPlayerEntity otherPlayer : playerManager.getPlayerList()) {
            if (this.isSamePersonAs(otherPlayer)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSamePersonAs(ServerPlayerEntity otherPlayer) {
        return otherPlayer != this.player && (Objects.equals(this.impersonatedProfile, Impersonator.get(otherPlayer).getImpersonatedProfile()) || Objects.equals(this.impersonatedProfile, otherPlayer.getGameProfile()));
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

    @Override
    public boolean shouldFakeCape() {
        return this.fakeCape;
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
        buf.writeBoolean(this.player.world.getGameRules().getBoolean(ImpersonateGamerules.FAKE_CAPES));
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
        this.fakeCape = buf.readBoolean();
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
