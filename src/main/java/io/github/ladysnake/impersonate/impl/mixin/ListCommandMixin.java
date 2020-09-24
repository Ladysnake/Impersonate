package io.github.ladysnake.impersonate.impl.mixin;

import com.mojang.authlib.GameProfile;
import io.github.ladysnake.impersonate.Impersonate;
import io.github.ladysnake.impersonate.impl.ImpersonateText;
import net.minecraft.server.command.ListCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ListCommand.class)
public abstract class ListCommandMixin {
    @Dynamic("Lambda method in executeUuids(ServerCommandSource), argument to execute(ServerCommandSource, Function)")
    @Inject(method = "method_30310", at = @At("RETURN"))
    private static void fakeNameAndUuid(ServerPlayerEntity player, CallbackInfoReturnable<Text> cir) {
        Text text = cir.getReturnValue();

        if (text instanceof TranslatableText) {
            Object[] args = ((TranslatableText) text).getArgs();
            // Defend against other mods changing the text
            if (args.length == 2 && args[0] instanceof Text && args[1] instanceof UUID) {
                GameProfile impersonatedProfile = Impersonate.IMPERSONATION.get(player).getImpersonatedProfile();

                if (impersonatedProfile != null) {
                    args[0] = ImpersonateText.get(player);
                    args[1] = impersonatedProfile.getId();
                }
            }
        }
    }
}
