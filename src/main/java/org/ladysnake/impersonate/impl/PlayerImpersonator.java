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

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.CopyableComponent;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.impersonate.Impersonate;
import org.ladysnake.impersonate.Impersonator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerImpersonator implements Impersonator, AutoSyncedComponent, CopyableComponent<PlayerImpersonator> {

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (Impersonator.get(handler.getPlayer()) instanceof PlayerImpersonator impersonator && impersonator.isImpersonating()) {
                impersonator.syncChanges(impersonator.impersonatedProfile);
            }
        });
    }

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
            this.impersonatedProfile = profile;
            this.editedProfile = profile == null ? null : new GameProfile(this.getActualProfile().getId(), this.impersonatedProfile.getName());
            this.syncChanges(profile);
        }
    }

    public void syncChanges(@Nullable GameProfile profile) {
        if (this.player instanceof ServerPlayerEntity serverPlayer && serverPlayer.networkHandler != null) {
            updatePlayerLists(new PlayerRemoveS2CPacket(List.of(this.player.getUuid())));
            this.applyCapeGamerule(serverPlayer, profile);
            ServerPlayerSkins.setSkin(serverPlayer, profile == null ? this.getActualProfile() : profile);
            updatePlayerLists(PlayerListS2CPacket.entryFromPlayer(List.of(serverPlayer)));
        }
        Impersonate.IMPERSONATION.sync(this.player);
    }

    private void applyCapeGamerule(ServerPlayerEntity player, GameProfile impersonatedProfile) {
        if (!player.getWorld().getGameRules().getBoolean(ImpersonateGamerules.FAKE_CAPES)) {
            if (impersonatedProfile == null) {
                ((PlayerEntityExtensions) player).impersonate_resetCape();
            } else {
                ((PlayerEntityExtensions) player).impersonate_disableCape();
            }
        }
    }

    private void updatePlayerLists(Packet<ClientPlayPacketListener> packet) {
        if (!player.getWorld().isClient) {
            PlayerManager playerManager = ((ServerPlayerEntity) player).server.getPlayerManager();
            if (isAloneOnServer(playerManager)) {
                playerManager.sendToAll(packet);
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
    public @Nullable GameProfile getImpersonatedProfile(@NotNull Identifier key) {
        return this.stackedImpersonations.get(key);
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
    public void copyFrom(PlayerImpersonator other, RegistryWrapper.WrapperLookup registryLookup) {
        this.stopImpersonations();
        this.stackedImpersonations.putAll(other.stackedImpersonations);
        this.resetImpersonation();
    }

    @Override
    public boolean isRequiredOnClient() {
        return false;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.player || player.server.getPlayerManager().isOperator(player.getGameProfile());
    }

    @Override
    public void writeSyncPacket(RegistryByteBuf buf, ServerPlayerEntity recipient) {
        GameProfile profile = this.getImpersonatedProfile();
        if (profile == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            PacketCodecs.GAME_PROFILE.encode(buf, profile);
        }
    }

    @Override
    public void applySyncPacket(RegistryByteBuf buf) {
        boolean present = buf.readBoolean();
        GameProfile profile = present ? PacketCodecs.GAME_PROFILE.decode(buf) : null;
        this.setImpersonatedProfile(profile);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (tag.contains("impersonations", NbtElement.LIST_TYPE)) {
            this.stopImpersonations();
            NbtList impersonations = tag.getList("impersonations", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < impersonations.size(); i++) {
                NbtCompound nbtEntry = impersonations.getCompound(i);
                Identifier key = Identifier.tryParse(nbtEntry.getString("impersonation_key"));
                if (key != null) {
                    ProfileComponent.CODEC
                        .parse(NbtOps.INSTANCE, nbtEntry)
                        .resultOrPartial(err -> Impersonate.LOGGER.error("Failed to load impersonated profile: {}", err))
                        .ifPresent(profile -> this.stackedImpersonations.put(key, profile.gameProfile()));
                }
            }
            this.resetImpersonation();
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (this.isImpersonating()) {
            NbtList profiles = new NbtList();
            for (var entry : this.stackedImpersonations.entrySet()) {
                NbtCompound nbtEntry = (NbtCompound) ProfileComponent.CODEC.encodeStart(NbtOps.INSTANCE, new ProfileComponent(entry.getValue())).getOrThrow();
                nbtEntry.putString("impersonation_key", entry.getKey().toString());
                profiles.add(nbtEntry);
            }
            tag.put("impersonations", profiles);
        }
    }
}
