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
package io.github.ladysnake.impersonate.impl.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.ImpersonateText;
import io.github.ladysnake.impersonate.impl.PlayerEntityExtensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityExtensions {
    @Shadow
    @Final
    private GameProfile gameProfile;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Override
    public GameProfile impersonate_getActualGameProfile() {
        return this.gameProfile;
    }

    /**
     * Fakes the player's game profile on clients
     */
    @Inject(method = "getGameProfile", at = @At("HEAD"), cancellable = true)
    private void fakeGameProfile(CallbackInfoReturnable<GameProfile> cir) {
        if (this.world.isClient) {
            Impersonator self = Impersonator.COMPONENT_TYPE.get(this);
            if (self.isImpersonating()) {
                cir.setReturnValue(self.getEditedProfile());
            }
        }
    }

    @ModifyArg(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;modifyText(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"))
    private Text fakeDisplayName(Text original) {
        // No need to fake on clients, as #fakeGameProfile already covers it
        if (!world.isClient && Impersonator.COMPONENT_TYPE.get(this).isImpersonating()) {
            return ImpersonateText.get((PlayerEntity) (Object) this);
        }
        return original;
    }

    @ModifyArg(method = "getNameAndUuid", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/LiteralText;append(Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;", ordinal = 0))
    private Text fakeNameAndUuid(Text originalName) {
        if (Impersonator.COMPONENT_TYPE.get(this).isImpersonating()) {
            return ImpersonateText.get((PlayerEntity) (Object) this);
        }
        return originalName;
    }

    @ModifyArg(method = "getNameAndUuid", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/MutableText;append(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", ordinal = 1))
    private String fakeNameAndUuid(String originalUuid) {
        if (Impersonator.COMPONENT_TYPE.get(this).isImpersonating()) {
            return impersonate_getActualGameProfile().getId().toString();
        }
        return originalUuid;
    }
}
