/*
 * Impersonate
 * Copyright (C) 2020-2021 Ladysnake
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

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.github.ladysnake.impersonate.Impersonate;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.mixin.EntityTrackerAccessor;
import io.github.ladysnake.impersonate.impl.mixin.ThreadedAnvilChunkStorageAccessor;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.ChunkManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class contains methods to change a player's skin serverside.
 *
 * <p>
 * This class has been adapted from the FabricTailor mod's <a href="https://github.com/samolego/FabricTailor/blob/1.0.0/src/main/java/org/samo_lego/fabrictailor/FabricTailor.java">source code</a>
 * under GNU Lesser General Public License.
 *
 * @author samo_lego
 */
public final class ServerPlayerSkins {
    private static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();

    public static void setSkin(@NotNull ServerPlayerEntity player, String playername) {
        THREADPOOL.submit(() -> {
            // If user has no skin data
            // Getting skin data from ely.by api, since it can be used with usernames
            // it also includes mojang skins
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("http://skinsystem.ely.by/textures/signed/" + playername + ".png?proxy=true").openConnection();
                String value;
                String signature;

                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();

                    if (reply.contains("error")) {
                        Impersonate.LOGGER.error("Failed to retrieve skin for {}", playername);
                        value = null;
                        signature = null;
                    } else {
                        value = reply.split("\"value\":\"")[1].split("\"")[0];
                        signature = reply.split("\"signature\":\"")[1].split("\"")[0];
                    }
                } else {
                    value = null;
                    signature = null;
                }

                Objects.requireNonNull(player.world.getServer()).execute(() ->
                    setPlayerSkin(player, value, signature)
                );
            } catch (IOException e) {
                Impersonate.LOGGER.error("Failed to retrieve skin for {}", playername, e);
            }
        });
    }

    /**
     * Sets the skin to the specified player and reloads it with {@link #reloadSkin(ServerPlayerEntity)}
     * @param player player whose skin needs to be changed
     * @param value skin texture value
     * @param signature skin texture signature
     */
    private static void setPlayerSkin(ServerPlayerEntity player, @Nullable String value, @Nullable String signature) {
        PropertyMap realProperties = Impersonator.get(player).getActualProfile().getProperties();
        PropertyMap editedProperties = Impersonator.get(player).getEditedProfile().getProperties();
        realProperties.removeAll("textures");
        editedProperties.removeAll("textures");

        if (value != null && signature != null) {
            realProperties.put("textures", new Property("textures", value, signature));
            editedProperties.put("textures", new Property("textures", value, signature));
        }

        // Reloading is needed in order to see the new skin
        reloadSkin(player);
    }

    /**
     * Reloads player's skin for all the players (including the one that has changed the skin)
     * @param player player that wants to have the skin reloaded
     */
    private static void reloadSkin(ServerPlayerEntity player) {
        for(ServerPlayerEntity other : Objects.requireNonNull(player.getServer()).getPlayerManager().getPlayerList()) {
            // Refreshing tablist for each player
            other.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, player));
            other.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));
        }

        ChunkManager manager = player.world.getChunkManager();
        assert manager instanceof ServerChunkManager;
        ThreadedAnvilChunkStorage storage = ((ServerChunkManager)manager).threadedAnvilChunkStorage;
        EntityTrackerAccessor trackerEntry = ((ThreadedAnvilChunkStorageAccessor) storage).getEntityTrackers().get(player.getEntityId());

        for (ServerPlayerEntity tracking : trackerEntry.getPlayersTracking()) {
            trackerEntry.getEntry().startTracking(tracking);
        }

        // need to change the player entity on the client
        ServerWorld targetWorld = (ServerWorld) player.world;
        player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(targetWorld.getDimension(), targetWorld.getRegistryKey(), BiomeAccess.hashSeed(targetWorld.getSeed()), player.interactionManager.getGameMode(), player.interactionManager.getPreviousGameMode(), targetWorld.isDebugWorld(), targetWorld.isFlat(), true));
        player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.yaw, player.pitch);
        player.server.getPlayerManager().sendCommandTree(player);
        player.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        player.networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), player.getHungerManager().getFoodLevel(), player.getHungerManager().getSaturationLevel()));
        for (StatusEffectInstance statusEffect : player.getStatusEffects()) {
            player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getEntityId(), statusEffect));
        }
        player.sendAbilitiesUpdate();
        player.server.getPlayerManager().sendWorldInfo(player, targetWorld);
        player.server.getPlayerManager().sendPlayerStatus(player);
    }
}
