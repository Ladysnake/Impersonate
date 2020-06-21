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

import io.github.fablabsmc.fablabs.api.gamerule.v1.GameRuleRegistry;
import io.github.fablabsmc.fablabs.api.gamerule.v1.RuleFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

public final class ImpersonateGamerules {
    public static final GameRules.Key<GameRules.BooleanRule> FAKE_CAPES =
        register("fakeCapes", RuleFactory.createBooleanRule(false, (server, rule) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (rule.get()) {
                    ((PlayerEntityExtensions)player).impersonate_resetCape();
                } else {
                    ((PlayerEntityExtensions) player).impersonate_disableCape();
                }
            }
        }));

    public static final GameRules.Key<GameRules.BooleanRule> OP_REVEAL_IMPERSONATIONS =
        register("opRevealImpersonations", RuleFactory.createBooleanRule(true));

    private static <T extends GameRules.Rule<T>> GameRules.Key<T> register(String name, GameRules.Type<T> type) {
        return GameRuleRegistry.register("impersonate:" + name, GameRules.Category.PLAYER, type);
    }

    public static void init() {
        // NO-OP
    }
}
