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
import dev.onyxstudios.cca.api.v3.component.AutoSyncedComponent;
import io.github.ladysnake.impersonate.Impersonate;
import io.github.ladysnake.impersonate.Impersonator;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerImpersonator implements Impersonator, AutoSyncedComponent, CopyableComponent<PlayerImpersonator> {

    @NotNull
    private final PlayerEntity player;
    private final Map<@NotNull Identifier, @NotNull GameProfile> stackedImpersonations = new LinkedHashMap<>();
    @Nullable
    private GameProfile impersonatedProfile;
    @Nullable
    private GameProfile editedProfile;

    public PlayerImpersonator(@NotNull PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void impersonate(@NotNull Identifier key, @NotNull GameProfile profile) {
        this.stackedImpersonations.put(key, profile);
        this.setImpersonatedProfile(profile);
    }

    @Override
    public void stopImpersonations() {
        this.stackedImpersonations.clear();
        this.resetImpersonation();
    }

    @Override
    public GameProfile stopImpersonation(@NotNull Identifier key) {
        if (this.isImpersonating()) {
            GameProfile ret = this.stackedImpersonations.remove(key);
            this.resetImpersonation();
            return ret;
        }
        return null;
    }

    private void resetImpersonation() {
        this.setImpersonatedProfile(getActiveImpersonation());
    }

    private GameProfile getActiveImpersonation() {
        GameProfile active = null;
        for (GameProfile gameProfile : this.stackedImpersonations.values()) active = gameProfile;
        return active;
    }

    private void setImpersonatedProfile(@Nullable GameProfile profile) {
        if (this.getImpersonatedProfile() != profile) {
            updatePlayerLists(PlayerListS2CPacket.Action.REMOVE_PLAYER);
            this.impersonatedProfile = profile;
            this.editedProfile = profile == null ? null : new GameProfile(this.getActualProfile().getId(), this.impersonatedProfile.getName());
            if (this.player instanceof ServerPlayerEntity) {
                ServerPlayerSkins.setSkin(((ServerPlayerEntity) player), this.getEditedProfile().getName());
            }
            updatePlayerLists(PlayerListS2CPacket.Action.ADD_PLAYER);
            Impersonate.IMPERSONATION.sync(this.player);
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

    /**
     * Return {@code true} if this player is the only one with the impersonated identity.
     *
     * <p>This method will return false if the impersonated player exists on the server, or if someone else impersonates the same person
     */
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
    public @NotNull ComponentType<Impersonator> getComponentType() {
        return (ComponentType<Impersonator>) Impersonate.IMPERSONATION;
    }

    @Override
    public void copyFrom(PlayerImpersonator other) {
        this.stopImpersonations();
        this.stackedImpersonations.putAll(other.stackedImpersonations);
        this.resetImpersonation();
    }

    private static final int ID_PRESENT = 0b01;
    private static final int NAME_PRESENT = 0b10;

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player, int syncOp) {
        return player == this.player || player.server.getPlayerManager().isOperator(player.getGameProfile());
    }

    @Override
    public void writeToPacket(PacketByteBuf buf, ServerPlayerEntity recipient, int syncOp) {
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
        this.setImpersonatedProfile((id == null && name == null) ? null : new GameProfile(id, name));
    }

    @Override
    public void readFromNbt(@NotNull CompoundTag tag) {
        if (tag.contains("impersonations", NbtType.LIST)) {
            this.stopImpersonations();
            ListTag impersonations = tag.getList("impersonations", NbtType.COMPOUND);
            for (int i = 0; i < impersonations.size(); i++) {
                CompoundTag nbtEntry = impersonations.getCompound(i);
                Identifier key = Identifier.tryParse(nbtEntry.getString("impersonation_key"));
                GameProfile profile = NbtHelper.toGameProfile(nbtEntry);
                if (key != null && profile != null) {
                    this.stackedImpersonations.put(key, profile);
                }
            }
            this.resetImpersonation();
        }
    }

    @Override
    public void writeToNbt(@NotNull CompoundTag tag) {
        if (this.isImpersonating()) {
            ListTag profiles = new ListTag();
            for (Map.Entry<Identifier, GameProfile> entry : this.stackedImpersonations.entrySet()) {
                CompoundTag nbtEntry = new CompoundTag();
                nbtEntry.putString("impersonation_key", entry.getKey().toString());
                profiles.add(NbtHelper.fromGameProfile(nbtEntry, entry.getValue()));
            }
            tag.put("impersonations", profiles);
        }
    }
}
