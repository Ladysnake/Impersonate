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
package org.ladysnake.impersonatest;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.ladysnake.impersonate.Impersonate;

public final class ImpersonatestClient implements ClientModInitializer {
    private static final boolean debugCredentials = Boolean.getBoolean("impersonate.debug.printCredentials");

    @Override
    public void onInitializeClient() {
        if (debugCredentials) {
            // Here to help login within a dev environment - be very careful when running this code at home
            Impersonate.LOGGER.info("ACCESS_TOKEN " + MinecraftClient.getInstance().getSession().getAccessToken());
            Impersonate.LOGGER.info("UUID " + MinecraftClient.getInstance().getSession().getUuidOrNull());
        }
    }
}
