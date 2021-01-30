package io.github.ladysnake.impersonate.impl.mixin;

import io.github.ladysnake.impersonate.Impersonator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Redirect(method = "loadPlayerData", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getName()Lnet/minecraft/text/Text;"))
    private Text resolvePlayerName(ServerPlayerEntity player) {
        return new LiteralText(Impersonator.get(player).getActualProfile().getName());
    }
    @Redirect(method = "createStatHandler", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getName()Lnet/minecraft/text/Text;"))
    private Text resolvePlayerStatsName(PlayerEntity player) {
        return new LiteralText(Impersonator.get(player).getActualProfile().getName());
    }
}
