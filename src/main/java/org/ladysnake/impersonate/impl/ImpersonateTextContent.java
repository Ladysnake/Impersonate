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
package org.ladysnake.impersonate.impl;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.impersonate.Impersonator;

import java.util.Objects;
import java.util.Optional;

public class ImpersonateTextContent implements RecipientAwareTextContent {
    private static final MapCodec<ImpersonateTextContent> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(Codec.STRING.fieldOf("text").forGetter(ImpersonateTextContent::getString)).apply(instance, text -> new ImpersonateTextContent(text, text, false))
    );
    private final String trueText;
    private final String fakedText;
    private boolean revealed;

    public static TextContent get(PlayerEntity player) {
        return get(player, false);
    }

    public static TextContent get(PlayerEntity player, boolean reveal) {
        Impersonator impersonator = Impersonator.get(player);
        String fakeName = impersonator.getEditedProfile().name();
        String trueName = player.getGameProfile().name();
        String trueText = String.format("%s(%s)", fakeName, trueName);
        return new ImpersonateTextContent(trueText, fakeName, reveal && !Objects.equals(fakeName, trueName));
    }

    private ImpersonateTextContent(String trueText, String fakedText, boolean revealed) {
        this.trueText = trueText;
        this.fakedText = fakedText;
        this.revealed = revealed;
    }

    @Override
    public void impersonateResolve(CommandOutput recipient) {
        revealed = !(recipient instanceof ImpersonateCommandOutput impersonateOutput) || impersonateOutput.impersonate$shouldRevealName();
    }

    public boolean isRevealed() {
        return revealed;
    }

    public static boolean shouldBeRevealedBy(PlayerEntity player) {
        return player instanceof ServerPlayerEntity serverPlayer
            && serverPlayer.getEntityWorld().getGameRules().getValue(ImpersonateGamerules.OP_REVEAL_IMPERSONATIONS)
            && serverPlayer.getPermissions().hasPermission(DefaultPermissions.MODERATORS);
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
        return visitor.accept(style, this.getString());
    }

    @Override
    public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
        return visitor.accept(this.getString());
    }

    @Override
    public MutableText parse(@Nullable ServerCommandSource source, @Nullable Entity sender, int depth) throws CommandSyntaxException {
        return Text.literal(this.getString());
    }

    @Override
    public MapCodec<? extends TextContent> getCodec() {
        return CODEC;
    }

    public String getString() {
        return this.revealed ? this.trueText : this.fakedText;
    }

    @Override
    public String toString() {
        return "impersonate:literal{" + this.fakedText + "/" + this.trueText + ", revealed=" + this.revealed + "}";
    }
}
