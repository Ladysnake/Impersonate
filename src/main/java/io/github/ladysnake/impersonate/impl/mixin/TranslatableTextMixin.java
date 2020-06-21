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

import io.github.ladysnake.impersonate.impl.RecipientAwareText;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TranslatableText.class)
public abstract class TranslatableTextMixin implements RecipientAwareText {
    @Shadow
    @Final
    private Object[] args;

    @Shadow
    public abstract Object[] getArgs();

    @Override
    public void impersonateResolve(CommandOutput recipient) {
        for (int i = 0; i < this.getArgs().length; i++) {
            Object arg = args[i];
            if (arg instanceof RecipientAwareText) {
                args[i] = ((RecipientAwareText) arg).impersonateResolveAll(recipient);
            }
        }
    }
}
