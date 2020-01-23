package io.github.ladysnake.impersonate.impl.mixin.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.PlayerSkins;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractPlayerEntityMixin extends PlayerEntity {
    public AbstractPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    private void fakeSkin(CallbackInfoReturnable<@NotNull Identifier> cir) {
        GameProfile impersonatedProfile = Impersonator.get(this).getImpersonatedProfile();
        if (impersonatedProfile != null) {
            cir.setReturnValue(PlayerSkins.get(impersonatedProfile));
        }
    }

    @Inject(method = "getCapeTexture", at = @At("HEAD"), cancellable = true)
    private void fakeCape(CallbackInfoReturnable<@Nullable Identifier> cir) {
        GameProfile impersonatedProfile = Impersonator.get(this).getImpersonatedProfile();
        if (impersonatedProfile != null) {
            cir.setReturnValue(PlayerSkins.get(impersonatedProfile, MinecraftProfileTexture.Type.CAPE));
        }
    }

    @Inject(method = "getElytraTexture", at = @At("HEAD"), cancellable = true)
    private void fakeElytra(CallbackInfoReturnable<@Nullable Identifier> cir) {
        GameProfile impersonatedProfile = Impersonator.get(this).getImpersonatedProfile();
        if (impersonatedProfile != null) {
            cir.setReturnValue(PlayerSkins.get(impersonatedProfile, MinecraftProfileTexture.Type.ELYTRA));
        }
    }
}
