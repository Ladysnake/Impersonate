package io.github.ladysnake.impersonate.impl.mixin.client;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.PlayerSkins;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "getTexture", at = @At("HEAD"), cancellable = true)
    private void fakeSkin(AbstractClientPlayerEntity player, CallbackInfoReturnable<Identifier> cir) {
        GameProfile impersonatedProfile = Impersonator.get(player).getImpersonatedProfile();
        if (impersonatedProfile != null) {
            cir.setReturnValue(PlayerSkins.get(impersonatedProfile));
        }
    }
}
