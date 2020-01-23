package io.github.ladysnake.impersonate;

import com.mojang.authlib.GameProfile;
import nerdhub.cardinal.components.api.component.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Impersonator extends Component {
    void impersonate(@NotNull GameProfile profile);

    void stopImpersonation();

    boolean isImpersonating();

    @Nullable
    GameProfile getFakedProfile();
}
