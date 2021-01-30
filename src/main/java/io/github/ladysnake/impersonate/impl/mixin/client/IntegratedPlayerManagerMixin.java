package io.github.ladysnake.impersonate.impl.mixin.client;

import io.github.ladysnake.impersonate.Impersonator;
import net.minecraft.server.integrated.IntegratedPlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(IntegratedPlayerManager.class)
public abstract class IntegratedPlayerManagerMixin {
    @Redirect(method = "savePlayerData", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getName()Lnet/minecraft/text/Text;"))
    private Text resolveText(ServerPlayerEntity player) {
        return new LiteralText(Impersonator.get(player).getActualProfile().getName());
    }
}
