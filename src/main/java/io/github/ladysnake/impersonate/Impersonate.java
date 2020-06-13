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
package io.github.ladysnake.impersonate;

import io.github.ladysnake.impersonate.impl.ImpersonateCommand;
import io.github.ladysnake.impersonate.impl.ImpersonateGamerules;
import io.github.ladysnake.impersonate.impl.PlayerImpersonator;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Main entrypoint for Impersonate
 *
 * @see Impersonator
 * @see PlayerSkins
 */
public final class Impersonate implements ModInitializer {
    public static final ComponentType<Impersonator> IMPERSONATION = ComponentRegistry.INSTANCE.registerIfAbsent(
        new Identifier("impersonate", "impersonation"),
        Impersonator.class
    ).attach(EntityComponentCallback.event(PlayerEntity.class), PlayerImpersonator::new);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ImpersonateCommand.register(dispatcher));
        ImpersonateGamerules.init();
    }
}
