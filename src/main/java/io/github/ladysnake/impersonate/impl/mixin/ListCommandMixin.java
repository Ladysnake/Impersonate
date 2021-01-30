/*
 * Impersonate
 * Copyright (C) 2020-2021 Ladysnake
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
import io.github.ladysnake.impersonate.Impersonate;
import net.minecraft.server.command.ListCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ListCommand.class)
public abstract class ListCommandMixin {
    @Dynamic("Lambda method in executeUuids(ServerCommandSource), argument to execute(ServerCommandSource, Function)")
    @Inject(method = "method_30310", at = @At("RETURN"))
    private static void fakeNameAndUuid(ServerPlayerEntity player, CallbackInfoReturnable<Text> cir) {
        Text text = cir.getReturnValue();

        if (text instanceof TranslatableText) {
            Object[] args = ((TranslatableText) text).getArgs();
            // Defend against other mods changing the text
            if (args.length == 2 && args[0] instanceof Text && args[1] instanceof UUID) {
                GameProfile impersonatedProfile = Impersonate.IMPERSONATION.get(player).getImpersonatedProfile();

                if (impersonatedProfile != null) {
                    // Name is already covered by PlayerEntity#getName mixin
                    args[1] = impersonatedProfile.getId();
                }
            }
        }
    }
}
