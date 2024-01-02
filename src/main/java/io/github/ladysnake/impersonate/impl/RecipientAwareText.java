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
package io.github.ladysnake.impersonate.impl;

import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;

public interface RecipientAwareText {
    default void impersonateResolve(CommandOutput recipient) {
        if (((Text) this).getContent() instanceof RecipientAwareTextContent txt) {
            txt.impersonateResolve(recipient);
        }
    }

    Text impersonateResolveAll(CommandOutput recipient);
}
