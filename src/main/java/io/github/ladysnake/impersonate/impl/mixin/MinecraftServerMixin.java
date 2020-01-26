package io.github.ladysnake.impersonate.impl.mixin;

import io.github.ladysnake.impersonate.impl.RecipientAwareText;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements CommandOutput {
    @ModifyVariable(method = "sendMessage", at = @At("HEAD"), argsOnly = true)
    private Text revealImpersonatorsInMessages(Text message) {
        return ((RecipientAwareText) message).impersonateResolveAll(this);
    }
}
