package io.github.ladysnake.impersonate.impl.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.impl.ImpersonateGamerules;
import io.github.ladysnake.impersonate.impl.PlayerEntityExtensions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements PlayerEntityExtensions {
    public ServerPlayerEntityMixin(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    @Inject(method = "setClientSettings", at = @At("RETURN"))
    private void removeCapeIfDisallowed(ClientSettingsC2SPacket clientSettingsC2SPacket, CallbackInfo ci) {
        if (this.world.getGameRules().getBoolean(ImpersonateGamerules.FAKE_CAPES)) {
            this.impersonate_disableCape();
        }
    }
}
