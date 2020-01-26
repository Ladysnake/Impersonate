package io.github.ladysnake.impersonate.impl.mixin.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import io.github.ladysnake.impersonate.PlayerSkins;
import io.github.ladysnake.impersonate.impl.mixin.PlayerEntityMixin;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntityMixin {
    protected AbstractClientPlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    private void fakeSkin(CallbackInfoReturnable<@NotNull Identifier> cir) {
        GameProfile impersonatedProfile = impersonate_self.getImpersonatedProfile();
        if (impersonatedProfile != null) {
            cir.setReturnValue(PlayerSkins.get(impersonatedProfile));
        }
    }

    @Inject(method = "getCapeTexture", at = @At("HEAD"), cancellable = true)
    private void fakeCape(CallbackInfoReturnable<@Nullable Identifier> cir) {
        fakeSpecialFeature(cir, MinecraftProfileTexture.Type.CAPE);
    }

    @Inject(method = "getElytraTexture", at = @At("HEAD"), cancellable = true)
    private void fakeElytra(CallbackInfoReturnable<@Nullable Identifier> cir) {
        fakeSpecialFeature(cir, MinecraftProfileTexture.Type.ELYTRA);
    }

    @Unique
    private void fakeSpecialFeature(CallbackInfoReturnable<@Nullable Identifier> cir, MinecraftProfileTexture.Type cape) {
        if (impersonate_self.shouldFakeCape()) {
            GameProfile impersonatedProfile = impersonate_self.getImpersonatedProfile();
            if (impersonatedProfile != null) {
                cir.setReturnValue(PlayerSkins.get(impersonatedProfile, cape));
            }
        }
    }
}
