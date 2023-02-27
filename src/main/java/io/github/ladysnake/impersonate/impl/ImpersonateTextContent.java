/*
 * Impersonate
 * Copyright (C) 2020-2023 Ladysnake
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

import io.github.ladysnake.impersonate.Impersonator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextContent;

import java.util.Optional;

public class ImpersonateTextContent implements RecipientAwareTextContent {
    private final String trueText;
    private final String fakedText;
    private boolean revealed;

    public static TextContent get(PlayerEntity player) {
        return get(player, false);
    }

    public static TextContent get(PlayerEntity player, boolean reveal) {
        Impersonator impersonator = Impersonator.get(player);
        String fakeName = impersonator.getEditedProfile().getName();
        String trueText = String.format("%s(%s)", fakeName, player.getGameProfile().getName());
        return new ImpersonateTextContent(trueText, fakeName, reveal);
    }

    private ImpersonateTextContent(String trueText, String fakedText, boolean revealed) {
        this.trueText = trueText;
        this.fakedText = fakedText;
        this.revealed = revealed;
    }

    @Override
    public void impersonateResolve(CommandOutput recipient) {
        revealed = !(recipient instanceof PlayerEntity player) || shouldBeRevealedBy(player);
    }

    public boolean isRevealed() {
        return revealed;
    }

    public static boolean shouldBeRevealedBy(PlayerEntity player) {
        return player instanceof ServerPlayerEntity
            && player.world.getGameRules().getBoolean(ImpersonateGamerules.OP_REVEAL_IMPERSONATIONS)
            && ((ServerPlayerEntity) player).server.getPlayerManager().isOperator(player.getGameProfile());
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        return visitor.accept(style, this.getString());
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        return visitor.accept(this.getString());
    }

    public String getString() {
        return this.revealed ? this.trueText : this.fakedText;
    }

    @Override
    public String toString() {
        return "impersonate:literal{" + this.fakedText + "/" + this.trueText + ", revealed=" + this.revealed + "}";
    }
}
