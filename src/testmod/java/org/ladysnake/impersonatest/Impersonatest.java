/*
 * Impersonate
 * Copyright (C) 2020-2026 Ladysnake
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
package org.ladysnake.impersonatest;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class Impersonatest implements ModInitializer {

    public static Identifier id(String path) {
        return Identifier.of("impersonatest", path);
    }

    @Override
    public void onInitialize() {
        Identifier impersonitemId = id("impersonitem");
        Registry.register(Registries.ITEM, impersonitemId, new ImpersonItem(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, impersonitemId))));
    }

}
