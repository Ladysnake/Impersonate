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

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.rule.GameRule;
import org.ladysnake.impersonate.Impersonate;
import org.ladysnake.impersonate.Impersonator;

public final class ImpersonateGamerules {
    public static final GameRule<Boolean> FAKE_CAPES = GameRuleBuilder.forBoolean(false).buildAndRegister(Impersonate.id("fake_capes"));

    public static final GameRule<Boolean> OP_REVEAL_IMPERSONATIONS =
        GameRuleBuilder.forBoolean(true).buildAndRegister(Impersonate.id("op_reveal_impersonations"));

    public static final GameRule<Boolean> LOG_REVEAL_IMPERSONATIONS =
        GameRuleBuilder.forBoolean(true).buildAndRegister(Impersonate.id("log_reveal_impersonations"));

    public static void init() {
        GameRuleEvents.changeCallback(FAKE_CAPES).register((value, server) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (value) {
                    ((PlayerEntityExtensions) player).impersonate_resetCape();
                } else {
                    ((PlayerEntityExtensions) player).impersonate_disableCape();
                }
            }
        });
        GameRuleEvents.changeCallback(OP_REVEAL_IMPERSONATIONS).register((value, server) -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (Impersonator.get(player) instanceof PlayerImpersonator playerImpersonator) {
                    playerImpersonator.syncChanges(playerImpersonator.getImpersonatedProfile());
                }
            }
        });
    }
}
