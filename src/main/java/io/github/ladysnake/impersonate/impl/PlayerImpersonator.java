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
