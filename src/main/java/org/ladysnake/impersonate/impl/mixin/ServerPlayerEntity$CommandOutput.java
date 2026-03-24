/*
 * Impersonate
 * Copyright (C) 2020-2026 Ladysnake
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

package org.ladysnake.impersonate.impl.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.ladysnake.impersonate.impl.ImpersonateCommandOutput;
import org.ladysnake.impersonate.impl.ImpersonateTextContent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$3")
public abstract class ServerPlayerEntity$CommandOutput implements ImpersonateCommandOutput {
    @Shadow
    @Final
    ServerPlayerEntity field_54403;

    @Override
    public boolean impersonate$shouldRevealName() {
        return ImpersonateTextContent.shouldBeRevealedBy(this.field_54403);
    }
}
