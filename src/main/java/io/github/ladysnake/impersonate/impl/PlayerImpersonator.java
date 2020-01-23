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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerImpersonator implements Impersonator, EntitySyncedComponent {
    @NotNull
    private PlayerEntity entity;
    @Nullable
    private GameProfile fakedProfile;

    public PlayerImpersonator(@NotNull PlayerEntity entity) {
        this.entity = entity;
    }

    @NotNull
    @Override
    public PlayerEntity getEntity() {
        return this.entity;
    }

    @NotNull
    @Override
    public ComponentType<?> getComponentType() {
        return Impersonate.IMPERSONATION;
    }

    @Override
    public void impersonate(@NotNull GameProfile profile) {
        this.fakedProfile = profile;
    }

    @Override
    public void stopImpersonation() {
        this.fakedProfile = null;
    }

    @Override
    public boolean isImpersonating() {
        return this.fakedProfile != null;
    }

    @Nullable
    @Override
    public GameProfile getFakedProfile() {
        return this.fakedProfile;
    }

    @Override
    public void fromTag(@NotNull CompoundTag tag) {
        if (tag.contains("impersonating")) {
            this.fakedProfile = NbtHelper.toGameProfile(tag);
        }
    }

    @NotNull
    @Override
    public CompoundTag toTag(@NotNull CompoundTag tag) {
        if (this.fakedProfile != null) {
            tag.putBoolean("impersonating", true);
            NbtHelper.fromGameProfile(tag, this.fakedProfile);
        }
        return tag;
    }
}
