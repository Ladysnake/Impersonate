package io.github.ladysnake.impersonate.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ladysnake.impersonate.Impersonator;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class ImpersonateCommand {

    public static final Identifier DEFAULT_IMPERSONATION_KEY = new Identifier("impersonate:command");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("impersonate")
            .requires(s -> s.hasPermissionLevel(2))
            .then(literal("disguise")
                .then(literal("as")
                    .then(argument("disguise", GameProfileArgumentType.gameProfile())
                        .executes(context -> startImpersonation(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "disguise"), Collections.singleton(context.getSource().getPlayer())))
                        .then(argument("targets", EntityArgumentType.players())
                            .executes(context -> startImpersonation(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "disguise"), EntityArgumentType.getPlayers(context, "targets")))
                        )
                    )
                )
                .then(literal("clear")
                    .executes(context -> stopImpersonation(context.getSource(), Collections.singleton(context.getSource().getPlayer())))
                    .then(argument("targets", EntityArgumentType.players())
                        .executes(context -> stopImpersonation(context.getSource(), EntityArgumentType.getPlayers(context, "targets")))
                    )
                )
            )
        );
    }

    private static int stopImpersonation(ServerCommandSource source, Collection<ServerPlayerEntity> players) {
        int count = 0;
        for (ServerPlayerEntity player : players) {
            Impersonator impersonator = Impersonator.get(player);
            if (impersonator.isImpersonating()) {
                GameProfile impersonated = Objects.requireNonNull(impersonator.getImpersonatedProfile());
                impersonator.stopImpersonations();
                sendImpersonationFeedback(source, player, impersonated, "clear");
                ++count;
            }
        }
        return count;
    }

    private static void sendImpersonationFeedback(ServerCommandSource source, ServerPlayerEntity player, GameProfile impersonated, String message) {
        String name = impersonated.getName();
        if (source.getEntity() == player) {
            source.sendFeedback(new TranslatableText("impersonate:commands.disguise." + message + ".success.self", name), true);
        } else {
            source.sendFeedback(new TranslatableText("impersonate:commands.disguise." + message + ".success.other", player.getDisplayName(), name), true);
        }
    }

    private static int startImpersonation(ServerCommandSource source, Collection<GameProfile> profiles, Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
        assert !profiles.isEmpty();
        Iterator<GameProfile> it = profiles.iterator();
        GameProfile disguise = it.next();
        if (it.hasNext()) {
            throw EntityArgumentType.TOO_MANY_PLAYERS_EXCEPTION.create();
        }
        int count = 0;
        stopImpersonation(source, players);
        for (ServerPlayerEntity player : players) {
            Impersonator.get(player).impersonate(DEFAULT_IMPERSONATION_KEY, disguise);
            sendImpersonationFeedback(source, player, disguise, "start");
            ++count;
        }
        return count;
    }
}
