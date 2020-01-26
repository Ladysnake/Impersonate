package io.github.ladysnake.impersonate.impl.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonate;
import io.github.ladysnake.impersonate.Impersonator;
import io.github.ladysnake.impersonate.impl.ImpersonateText;
import io.github.ladysnake.impersonate.impl.PlayerEntityExtensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityExtensions {
    @Shadow
    @Final
    private GameProfile gameProfile;

    @Unique
    protected Impersonator impersonate_self = Impersonate.IMPERSONATION.get(this);    // cache the component for faster access

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Override
    public Impersonator impersonate_getAsImpersonator() {
        return impersonate_self;
    }

    @Override
    public GameProfile impersonate_getActualGameProfile() {
        return this.gameProfile;
    }

    /**
     * Fakes the player's game profile on clients
     */
    @Inject(method = "getGameProfile", at = @At("HEAD"), cancellable = true)
    private void fakeGameProfile(CallbackInfoReturnable<GameProfile> cir) {
        if (this.world.isClient) {
            if (impersonate_self.isImpersonating()) {
                cir.setReturnValue(impersonate_self.getEditedProfile());
            }
        }
    }

    @ModifyArg(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;modifyText(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/Text;"))
    private Text fakeDisplayName(Text original) {
        // No need to fake on clients, as #fakeGameProfile already covers it
        if (!world.isClient && impersonate_self.isImpersonating()) {
            return ImpersonateText.get((PlayerEntity) (Object) this);
        }
        return original;
    }

    @ModifyArg(method = "getNameAndUuid", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/LiteralText;append(Lnet/minecraft/text/Text;)Lnet/minecraft/text/Text;", ordinal = 0))
    private Text fakeNameAndUuid(Text originalName) {
        if (impersonate_self.isImpersonating()) {
            return ImpersonateText.get((PlayerEntity) (Object) this);
        }
        return originalName;
    }

    @ModifyArg(method = "getNameAndUuid", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text;append(Ljava/lang/String;)Lnet/minecraft/text/Text;", ordinal = 1))
    private String fakeNameAndUuid(String originalUuid) {
        if (impersonate_self.isImpersonating()) {
            return impersonate_getActualGameProfile().getId().toString();
        }
        return originalUuid;
    }
}
