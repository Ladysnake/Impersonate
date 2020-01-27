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

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.impl.PlayerEntityExtensions;
import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Impersonator extends Component {
    static Impersonator get(@NotNull PlayerEntity player) {
        return ((PlayerEntityExtensions) player).impersonate_getAsImpersonator();
    }

    /**
     * Start impersonating a player designated by {@code profile}.
     *
     * <p> {@code profile} <em>may or may not</em> designate a player that is connected on the same server
     * as the impersonating player. Impersonations of offline players are valid.
     *
     * <p> If the player is currently impersonating {@code profile} (ie. {@code profile.equals(getImpersonatedProfile())}),
     * this method does nothing. If the player is impersonating someone else, this method will pause the current impersonation.
     *
     * @param profile the {@code GameProfile} of the player to impersonate
     * @param key     an identifying key for the source of the impersonation
     * @see #stopImpersonation(Identifier)
     */
    void impersonate(@NotNull Identifier key, @NotNull GameProfile profile);

    /**
     * Stops all ongoing impersonations.
     */
    void stopImpersonations();

    /**
     * Stops an ongoing impersonation.
     *
     * @param key the key identifying the source of the impersonation
     * @return the game profile that was impersonated under that impersonation key
     * @see #impersonate(Identifier, GameProfile)
     */
    @Nullable GameProfile stopImpersonation(@NotNull Identifier key);

    /**
     * Returns {@code true} if this player is currently impersonating another player.
     *
     * @return {@code true} if this player is currently impersonating someone, and {@code false} otherwise
     */
    boolean isImpersonating();

    /**
     * Returns the profile of the player that's being impersonated, or {@code null} if no impersonation
     * is taking place (ie. {@link #isImpersonating()} returns {@code false}).
     *
     * @return the impersonated profile, or {@code null} if no impersonation is taking place
     */
    @Nullable GameProfile getImpersonatedProfile();

    /**
     * Returns the player's actual {@link GameProfile}, disregarding any impersonation.
     *
     * @return the player's actual profile
     */
    @NotNull GameProfile getActualProfile();

    /**
     * Returns the player's profile, edited to account for an ongoing impersonation.
     *
     * <p> If the player is not impersonating anyone, this method behaves as if
     * calling {@link #getActualProfile()}. Otherwise, it returns a {@code GameProfile}
     * with the same {@link GameProfile#getId() id} as the original, but with the name
     * of the impersonated player.
     *
     * @return the player's current, possibly faked, profile
     */
    @NotNull GameProfile getEditedProfile();

    /**
     * Returns {@code true} if the player should mimic cape and elytra textures of impersonated players.
     *
     * <p> Whether players are allowed to fake capes is determined by the value of the
     * {@code impersonate:fakeCapes} gamerule.
     *
     * @return {@code true} if the player should fake capes and elytra textures
     */
    boolean shouldFakeCape();
}
