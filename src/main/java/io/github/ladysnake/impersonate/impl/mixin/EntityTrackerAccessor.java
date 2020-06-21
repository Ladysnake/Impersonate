package io.github.ladysnake.impersonate.impl.mixin;

import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(targets = "net.minecraft.server.world.ThreadedAnvilChunkStorage$EntityTracker")
public interface EntityTrackerAccessor {
    @Accessor
    EntityTrackerEntry getEntry();
    @Accessor
    Set<ServerPlayerEntity> getPlayersTracking();
}
