package io.github.ladysnake.impersonate.impl;

import io.github.ladysnake.impersonate.impl.mixin.client.AbstractClientPlayerEntityAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ImpersonateClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ServerPlayerSkins.RELOAD_SKIN_PACKET, (client, handler, buf, responseSender) -> client.execute(() -> {
            assert client.player != null;
            ((AbstractClientPlayerEntityAccessor) client.player).setCachedScoreboardEntry(null);
        }));
    }
}
