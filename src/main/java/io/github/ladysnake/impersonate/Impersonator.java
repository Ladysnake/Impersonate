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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Impersonator extends Component {
    static Impersonator get(PlayerEntity player) {
        return ((PlayerEntityExtensions) player).impersonate_getAsImpersonator();
    }

    void impersonate(@NotNull GameProfile profile);

    void stopImpersonation();

    boolean isImpersonating();

    @Nullable
    GameProfile getImpersonatedProfile();

    /**
     * Returns the player's actual {@link GameProfile}, disregarding any impersonation.
     *
     * @return the player's actual profile
     */
    @NotNull
    GameProfile getActualProfile();

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
    @NotNull
    GameProfile getEditedProfile();
}
