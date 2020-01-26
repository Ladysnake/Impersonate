package io.github.ladysnake.impersonate.impl.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.ImpersonateText;
import net.minecraft.client.network.packet.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerListS2CPacket.class)
public abstract class PlayerListS2CPacketMixin {
    @SuppressWarnings("InvalidMemberReference") // bad dev plugin
    @Redirect(method = {"<init>(Lnet/minecraft/client/network/packet/PlayerListS2CPacket$Action;[Lnet/minecraft/server/network/ServerPlayerEntity;)V", "<init>(Lnet/minecraft/client/network/packet/PlayerListS2CPacket$Action;Ljava/lang/Iterable;)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getGameProfile()Lcom/mojang/authlib/GameProfile;"))
    private GameProfile preventEasyReveal(ServerPlayerEntity serverPlayerEntity) {
        GameProfile impersonatedProfile = Impersonator.get(serverPlayerEntity).getImpersonatedProfile();
        if (impersonatedProfile != null && !ImpersonateText.shouldBeRevealedBy(serverPlayerEntity)) {
            return impersonatedProfile;
        }
        return serverPlayerEntity.getGameProfile();
    }
}
