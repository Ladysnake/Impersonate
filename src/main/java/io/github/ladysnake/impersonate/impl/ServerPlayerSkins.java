/*
 * Impersonate
 * Copyright (C) 2020-2023 Ladysnake
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import io.github.ladysnake.impersonate.Impersonate;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.mixin.EntityTrackerAccessor;
import io.github.ladysnake.impersonate.impl.mixin.ThreadedAnvilChunkStorageAccessor;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.ChunkManager;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class contains methods to change a player's skin serverside.
 *
 * <p>This class has been adapted from the FabricTailor mod's <a href="https://github.com/samolego/FabricTailor/blob/1.0.0/src/main/java/org/samo_lego/fabrictailor/FabricTailor.java">source code</a>
 * under GNU Lesser General Public License.
 *
 * @author samo_lego
 */
public final class ServerPlayerSkins {
    public static final Identifier RELOAD_SKIN_PACKET = new Identifier("impersonate", "impersonation");
    private static final boolean FORCE_VANILLA_RELOADING = Boolean.getBoolean("impersonate.force_vanilla_reloading");
    private static final ExecutorService THREADPOOL = Executors.newCachedThreadPool();
    private static CompletableFuture<Pair<String, String>> currentSkinTask = CompletableFuture.completedFuture(null);

    public static synchronized void setSkin(@NotNull ServerPlayerEntity player, GameProfile profile) {
        CompletableFuture<?> previousSkinTask = currentSkinTask;
        currentSkinTask = CompletableFuture.<Pair<String, String>>supplyAsync(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + profile.getId().toString().replace("-", "") + "?unsigned=false").openConnection();

                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    String reply = IOUtils.toString(new InputStreamReader(connection.getInputStream()));
                    JsonObject json = JsonHelper.deserialize(reply);
                    for (JsonElement prop : JsonHelper.getArray(json, "properties")) {
                        JsonObject property = JsonHelper.asObject(prop, "property");
                        if (JsonHelper.getString(property, "name").equals("textures")) {
                            return Pair.of(
                                JsonHelper.getString(property, "value"),
                                JsonHelper.getString(property, "signature")
                            );
                        }
                    }
                    throw new JsonSyntaxException("No skin texture data in response for " + profile.getName());
                } else {
                    return Pair.of(null, null);    // no throwing exception to avoid spamming logs when offline
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, THREADPOOL).exceptionally(e -> {
            Impersonate.LOGGER.error("Failed to retrieve skin for " + profile.getName(), e);
            return Pair.of(null, null);
        });
        // we wait for the previous skin fetching to complete, to avoid setting skins in the wrong order
        currentSkinTask.thenAcceptBothAsync(previousSkinTask, (pair, o) -> setPlayerSkin(player, pair.getFirst(), pair.getSecond()), player.world.getServer());
    }

    /**
     * Sets the skin to the specified player and reloads it with {@link #reloadSkin(ServerPlayerEntity)}
     *
     * @param player    player whose skin needs to be changed
     * @param value     skin texture value
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
     *
     * @param player player that wants to have the skin reloaded
     */
    private static void reloadSkin(ServerPlayerEntity player) {
        for (ServerPlayerEntity other : Objects.requireNonNull(player.getServer()).getPlayerManager().getPlayerList()) {
            // Refreshing tablist for each player
            other.networkHandler.sendPacket(new PlayerRemoveS2CPacket(List.of(player.getUuid())));
            other.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, player));
        }

        ChunkManager manager = player.world.getChunkManager();
        assert manager instanceof ServerChunkManager;
        ThreadedAnvilChunkStorage storage = ((ServerChunkManager) manager).threadedAnvilChunkStorage;
        EntityTrackerAccessor trackerEntry = ((ThreadedAnvilChunkStorageAccessor) storage).getEntityTrackers().get(player.getId());

        for (ServerPlayerEntity tracking : PlayerLookup.tracking(player)) {
            trackerEntry.getEntry().startTracking(tracking);
        }

        if (FORCE_VANILLA_RELOADING || !ServerPlayNetworking.canSend(player, RELOAD_SKIN_PACKET)) {
            reloadSkinVanilla(player);
        } else {
            ServerPlayNetworking.send(player, RELOAD_SKIN_PACKET, PacketByteBufs.empty());
        }
    }

    private static void reloadSkinVanilla(ServerPlayerEntity player) {
        // need to change the player entity on the client
        ServerWorld targetWorld = (ServerWorld) player.world;
        player.networkHandler.sendPacket(new PlayerRespawnS2CPacket(
            targetWorld.getDimensionKey(),
            targetWorld.getRegistryKey(),
            BiomeAccess.hashSeed(targetWorld.getSeed()),
            player.interactionManager.getGameMode(),
            player.interactionManager.getPreviousGameMode(),
            targetWorld.isDebugWorld(),
            targetWorld.isFlat(),
            PlayerRespawnS2CPacket.KEEP_ATTRIBUTES,
            player.getLastDeathPos()
        ));
        player.networkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        player.server.getPlayerManager().sendCommandTree(player);
        player.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
        player.networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), player.getHungerManager().getFoodLevel(), player.getHungerManager().getSaturationLevel()));
        for (StatusEffectInstance statusEffect : player.getStatusEffects()) {
            player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), statusEffect));
        }
        player.sendAbilitiesUpdate();
        player.server.getPlayerManager().sendWorldInfo(player, targetWorld);
        Entity vehicle = player.getVehicle();
        if (vehicle != null) player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(vehicle));
        player.server.getPlayerManager().sendPlayerStatus(player);
    }
}
