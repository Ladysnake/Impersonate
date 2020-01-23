package io.github.ladysnake.impersonate.impl;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonator;

public interface PlayerEntityExtensions {
    Impersonator impersonate_getAsImpersonator();
    GameProfile impersonate_getActualGameProfile();
}
