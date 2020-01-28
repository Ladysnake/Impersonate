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
package io.github.ladysnake.impersonate.impl;

import com.mojang.brigadier.arguments.BoolArgumentType;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.mixin.GameRulesAccessor;
import io.github.ladysnake.impersonate.impl.mixin.RuleTypeAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

import java.util.function.BiConsumer;

public final class ImpersonateGamerules {
    public static final GameRules.RuleKey<GameRules.BooleanRule> FAKE_CAPES =
        register("impersonate:fakeCapes", createBooleanRule(false, (server, rule) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ((PlayerImpersonator) Impersonator.get(player)).sync();
            }
        }));

    public static final GameRules.RuleKey<GameRules.BooleanRule> OP_REVEAL_IMPERSONATIONS =
        register("impersonate:opRevealImpersonations", createBooleanRule(true, (server, rule) -> {}));

    @SuppressWarnings("unchecked")
    private static GameRules.RuleType<GameRules.BooleanRule> createBooleanRule(boolean initialValue, BiConsumer<MinecraftServer, GameRules.BooleanRule> changeCallback) {
        return RuleTypeAccessor.invokeNew(BoolArgumentType::bool, type -> new GameRules.BooleanRule((GameRules.RuleType<GameRules.BooleanRule>) type, initialValue), changeCallback);
    }

    private static <T extends GameRules.Rule<T>> GameRules.RuleKey<T> register(String name, GameRules.RuleType<T> type) {
        return GameRulesAccessor.invokeRegister(name, type);
    }

    public static void init() {
        // NO-OP
    }
}
