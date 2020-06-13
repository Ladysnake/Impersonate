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

import dev.onyxstudios.cca.api.v3.component.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.component.entity.StaticEntityComponentInitializer;
import io.github.ladysnake.impersonate.impl.ImpersonateCommand;
import io.github.ladysnake.impersonate.impl.ImpersonateGamerules;
import io.github.ladysnake.impersonate.impl.PlayerImpersonator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;

/**
 * Main entrypoint for Impersonate
 *
 * @see Impersonator
 * @see PlayerSkins
 */
public final class Impersonate implements ModInitializer, StaticEntityComponentInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ImpersonateCommand.register(dispatcher));
        ImpersonateGamerules.init();
    }

    @Override
    public Collection<Identifier> getSupportedComponentTypes() {
        return Collections.singletonList(new Identifier("impersonate", "impersonation"));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.register(Impersonator.COMPONENT_TYPE, PlayerEntity.class, PlayerImpersonator::new);
    }
}
