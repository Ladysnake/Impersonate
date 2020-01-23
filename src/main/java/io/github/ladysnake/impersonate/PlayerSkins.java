/*
 * Sync
 * Copyright (C) 2014 iChun
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version ${gplVersion} of the License, or (at your option) any later version.
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * This class contains methods to retrieve player skins.
 *
 * <p>
 * This class has been adapted from the Sync mod's <a href="https://github.com/iChun/Sync/blob/master/src/main/java/me/ichun/mods/sync/client/core/SyncSkinManager.java">source code</a>
 * under GNU Lesser General Public License.
 *
 * @author IChun
 * @apiNote <strong>Methods in this class should only be called on a {@link EnvType#CLIENT client}</strong>
 */
public final class PlayerSkins {
    //Cache skins between calls to avoid hitting the rate limit for skin session servers
    //Hold values for a longer time, so they are loaded fast if many calls for the same player are made
    //Skin loading priority: Cache(fastest), ScoreboardEntry(only available when the player is online and in same dim as the client, fast), SessionService(slow)
    private static final LoadingCache<UUID, EnumMap<MinecraftProfileTexture.Type, Identifier>> skinCache = CacheBuilder.newBuilder()
        .expireAfterAccess(15,TimeUnit.MINUTES)
        .build(CacheLoader.from(() -> new EnumMap<>(MinecraftProfileTexture.Type.class)));
    private static final Set<UUID> queriedSkins = new HashSet<>();

    /**
     * Gets a player skin texture {@link Identifier} from its {@link GameProfile}.
     *
     * <p> This method caches its results to avoid freezes and rate limit issues caused by retrieving skin textures
     * from Mojang servers. If the player skin is immediately available, either through the cache or through
     * an existing {@link PlayerListEntry} for the {@code GameProfile}, the method returns that result.
     * Otherwise, a request to the skin servers is initiated, and the method returns immediately with the
     * default skin for the {@code profile}.
     *
     * @param profile the profile of the player of which to get the texture
     * @return the identifier for the skin texture
     */
    @NotNull
    public static Identifier get(GameProfile profile) {
        return Objects.requireNonNull(get(profile, MinecraftProfileTexture.Type.SKIN));
    }

    /**
     * Gets a player skin texture {@link Identifier} from its {@link GameProfile}.
     *
     * <p> This method caches its results to avoid freezes and rate limit issues caused by retrieving skin textures
     * from Mojang servers. If the player skin is immediately available, either through the cache or through
     * an existing {@link PlayerListEntry} for the {@code GameProfile}, the method returns that result.
     * Otherwise, a request to the skin servers is initiated, and the method returns immediately with the
     * default skin for the {@code profile}.
     *
     * <p> The return value is guaranteed to not be {@code null} if the {@code requestedType} is {@link MinecraftProfileTexture.Type#SKIN}.
     *
     * @param profile the profile of the player of which to get the texture
     * @param requestedType the part of the skin that is being requested
     * @return the identifier for the skin texture
     */
    @Nullable
    public static Identifier get(@NotNull GameProfile profile, @NotNull MinecraftProfileTexture.Type requestedType) {
        EnumMap<MinecraftProfileTexture.Type, Identifier> skins = skinCache.getUnchecked(profile.getId());
        Identifier loc = skins.get(requestedType);
        if (loc != null) {
            return loc;
        }
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        PlayerListEntry playerInfo = networkHandler == null ? null : networkHandler.getPlayerListEntry(profile.getId());
        if (playerInfo != null) { //load from network player
            switch (requestedType) {
                case SKIN: loc = playerInfo.getSkinTexture(); break;
                case CAPE: loc = playerInfo.getCapeTexture(); break;
                case ELYTRA: loc = playerInfo.getElytraTexture(); break;
            }
            if (loc != null && loc != DefaultSkinHelper.getTexture(playerInfo.getProfile().getId())) {
                skins.put(requestedType, loc);
                return loc;
            }
        }
        synchronized (queriedSkins) {
            if (!queriedSkins.contains(profile.getId())) {
                //Make one call per user - again rate limit protection
                MinecraftClient.getInstance().getSkinProvider().loadSkin(profile, (type, location, profileTexture) -> {
                    skins.put(type, location);
                    synchronized (queriedSkins) {
                        queriedSkins.remove(profile.getId());
                    }
                }, true);
            }
            queriedSkins.add(profile.getId());
        }
        return requestedType == MinecraftProfileTexture.Type.SKIN ? DefaultSkinHelper.getTexture(profile.getId()) : null;
    }

    public static void invalidateCaches() {
        synchronized (queriedSkins) {
            skinCache.invalidateAll();
            skinCache.cleanUp();
            queriedSkins.clear();
        }
    }
}
