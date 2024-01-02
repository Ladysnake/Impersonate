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
package org.ladysnake.impersonate;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ladysnake.impersonate.impl.ImpersonateCommand;
import org.ladysnake.impersonate.impl.ImpersonateGamerules;
import org.ladysnake.impersonate.impl.PlayerImpersonator;

/**
 * Main entrypoint for Impersonate
 *
 * @see Impersonator
 */
public final class Impersonate implements ModInitializer, EntityComponentInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Impersonate");
    public static final ComponentKey<Impersonator> IMPERSONATION = ComponentRegistryV3.INSTANCE.getOrCreate(
        new Identifier("impersonate", "impersonation"),
        Impersonator.class
    );

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, acc, dedicated) -> ImpersonateCommand.register(dispatcher));
        ImpersonateGamerules.init();
        PlayerImpersonator.init();
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(IMPERSONATION, PlayerImpersonator::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
