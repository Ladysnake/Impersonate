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
package io.github.ladysnake.impersonate.impl.mixin;

import io.github.ladysnake.impersonate.impl.ImpersonateGamerules;
import io.github.ladysnake.impersonate.impl.RecipientAwareText;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements CommandOutput {
    @Shadow
    public abstract GameRules getGameRules();

    @Shadow
    public abstract ServerWorld getOverworld();

    @ModifyVariable(method = "sendMessage", at = @At("HEAD"), argsOnly = true)
    private Text revealImpersonatorsInMessages(Text message) {
        return impersonate$reveal(message);
    }

    @ModifyVariable(method = "logChatMessage", at = @At("HEAD"), argsOnly = true)
    private Text revealImpersonatorsInChatMessageContent(Text messageContent) {
        return impersonate$reveal(messageContent);
    }

    @ModifyVariable(method = "logChatMessage", at = @At("HEAD"), argsOnly = true)
    private MessageType.Parameters revealImpersonatorsInChatMessageContent(MessageType.Parameters params) {
        return new MessageType.Parameters(params.type(), impersonate$reveal(params.name()), params.targetName() == null ? null : impersonate$reveal(params.targetName()));
    }

    @Unique
    private Text impersonate$reveal(Text message) {
        if (this.getOverworld() == null || this.getGameRules().getBoolean(ImpersonateGamerules.LOG_REVEAL_IMPERSONATIONS)) {
            return ((RecipientAwareText) message).impersonateResolveAll(this);
        }
        return message;
    }
}
