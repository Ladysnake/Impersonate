package io.github.ladysnake.impersonate;

import io.github.ladysnake.impersonate.impl.PlayerImpersonator;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class Impersonate implements ModInitializer {
    public static final ComponentType<Impersonator> IMPERSONATION = ComponentRegistry.INSTANCE.registerIfAbsent(
        new Identifier("impersonate", "impersonation"),
        Impersonator.class
    ).attach(EntityComponentCallback.event(PlayerEntity.class), PlayerImpersonator::new);

    @Override
    public void onInitialize() {
        EntityComponents.setRespawnCopyStrategy(IMPERSONATION, RespawnCopyStrategy.ALWAYS_COPY);
    }
}
