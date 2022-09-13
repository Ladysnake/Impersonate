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
package io.github.ladysnake.impersonate.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ladysnake.impersonate.Impersonator;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ImpersonateCommand {

    public static final Identifier DEFAULT_IMPERSONATION_KEY = new Identifier("impersonate:command");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("impersonate")
            // Require perms at the root to avoid showing empty "/impersonate" command to regular players
            .requires(Permissions.require("impersonate.command.query.self", 2))
            .then(literal("disguise")
                .requires(Permissions.require("impersonate.command.disguise.self", 2))
                .then(literal("as")
                    .then(argument("disguise", GameProfileArgumentType.gameProfile())
                        .executes(context -> startImpersonation(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "disguise"), Collections.singleton(context.getSource().getPlayer()), DEFAULT_IMPERSONATION_KEY))
                        .then(argument("targets", EntityArgumentType.players())
                            // Require another permission for disguising other people
                            .requires(Permissions.require("impersonate.command.disguise", 2))
                            .executes(context -> startImpersonation(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "disguise"), EntityArgumentType.getPlayers(context, "targets"), DEFAULT_IMPERSONATION_KEY))
                            .then(argument("key", IdentifierArgumentType.identifier())
                                .executes(context -> startImpersonation(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "disguise"), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "key")))
                            )
                        )
                    )
                )
                .then(literal("clear")
                    .executes(context -> stopImpersonation(context.getSource(), Collections.singleton(context.getSource().getPlayer()), null))
                    .then(argument("targets", EntityArgumentType.players())
                        // Require another permission for disguising other people
                        .requires(Permissions.require("impersonate.command.disguise", 2))
                        .executes(context -> stopImpersonation(context.getSource(), EntityArgumentType.getPlayers(context, "targets"), null))
                        .then(argument("key", IdentifierArgumentType.identifier())
                            .executes(context -> stopImpersonation(context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "key")))
                        )
                    )
                )
                .then(literal("query")
                    .requires(Permissions.require("impersonate.command.disguise.query.self", 2))
                    .executes(context -> queryImpersonation(context.getSource(), context.getSource().getPlayer(), null))
                    .then(argument("target", EntityArgumentType.player())
                        .requires(Permissions.require("impersonate.command.disguise.query", 2))
                        .executes(context -> queryImpersonation(context.getSource(), EntityArgumentType.getPlayer(context, "target"), null))
                        .then(argument("key", IdentifierArgumentType.identifier())
                            .executes(context -> queryImpersonation(context.getSource(), EntityArgumentType.getPlayer(context, "target"), IdentifierArgumentType.getIdentifier(context, "key")))
                        )
                    )
                )
            )
        );
    }

    private static int queryImpersonation(ServerCommandSource source, ServerPlayerEntity player, @Nullable Identifier key) {
        GameProfile profile;
        if (key == null) {
            profile = Impersonator.get(player).getImpersonatedProfile();
        } else {
            profile = Impersonator.get(player).getImpersonatedProfile(key);
        }
        sendImpersonationFeedback(source, player, profile == null
            ? "query.no_one"
            : "query",
            profile == null
            ? ""
            : profile.getName());
        return profile == null ? 1 : 0;
    }

    private static int stopImpersonation(ServerCommandSource source, Collection<ServerPlayerEntity> players, @Nullable Identifier key) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            Impersonator impersonator = Impersonator.get(player);
            GameProfile impersonated;
            if (key == null) {
                impersonated = impersonator.getImpersonatedProfile();
                impersonator.stopImpersonations();
            } else {
                impersonated = impersonator.stopImpersonation(key);
            }
            if (impersonated != null) {
                sendImpersonationFeedback(source, player, "clear", impersonated.getName());
                ++count;
            }
        }
        return count;
    }

    private static void sendImpersonationFeedback(ServerCommandSource source, ServerPlayerEntity player, String command, Object arg) {
        if (source.getEntity() == player) {
            source.sendFeedback(Text.translatable("impersonate:commands.disguise." + command + ".success.self", arg), true);
        } else {
            source.sendFeedback(Text.translatable("impersonate:commands.disguise." + command + ".success.other", player.getDisplayName(), arg), true);
        }
    }

    private static int startImpersonation(ServerCommandSource source, Collection<GameProfile> profiles, Collection<ServerPlayerEntity> players, Identifier impersonationKey) throws CommandSyntaxException {
        assert !profiles.isEmpty();
        Iterator<GameProfile> it = profiles.iterator();
        GameProfile disguise = it.next();
        if (it.hasNext()) {
            throw EntityArgumentType.TOO_MANY_PLAYERS_EXCEPTION.create();
        }
        int count = 0;
        stopImpersonation(source, players, impersonationKey);
        for (ServerPlayerEntity player : players) {
            Impersonator.get(player).impersonate(impersonationKey, disguise);
            sendImpersonationFeedback(source, player, "start", disguise.getName());
            ++count;
        }
        return count;
    }
}
