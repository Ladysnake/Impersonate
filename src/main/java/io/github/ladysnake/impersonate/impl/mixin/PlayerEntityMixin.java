/*
 * Impersonate
 * Copyright (C) 2020-2022 Ladysnake
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
package io.github.ladysnake.impersonate.impl.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.ImpersonateText;
import io.github.ladysnake.impersonate.impl.PlayerEntityExtensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityExtensions {
    @Shadow
    @Final
    private GameProfile gameProfile;

    @Shadow
    @Final
    protected static TrackedData<Byte> PLAYER_MODEL_PARTS;

    @Unique
    private boolean wantsCapeDisplay;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Override
    public GameProfile impersonate_getActualGameProfile() {
        return this.gameProfile;
    }

    @Override
    public void impersonate_resetCape() {
        if (this.wantsCapeDisplay) {
            DataTracker dataTracker = this.getDataTracker();
            byte modelMask = dataTracker.get(PLAYER_MODEL_PARTS);
            byte newModelMask = (byte) (modelMask | 1);
            dataTracker.set(PLAYER_MODEL_PARTS, newModelMask);
        }
    }

    @Override
    public void impersonate_disableCape() {
        DataTracker dataTracker = this.getDataTracker();
        byte modelMask = dataTracker.get(PLAYER_MODEL_PARTS);
        this.wantsCapeDisplay = (modelMask & 1) != 0;
        byte newModelMask = (byte) (modelMask & ~1);
        dataTracker.set(PLAYER_MODEL_PARTS, newModelMask);
    }

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void fakeName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity self = ((PlayerEntity) (Object) this);
        if (Impersonator.get(self).isImpersonating()) {
            // if the client is aware that there is an impersonation, they should display it
            cir.setReturnValue(ImpersonateText.get(self, world.isClient));
        }
    }
}
