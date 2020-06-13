package io.github.ladysnake.impersonate.impl.mixin;

import io.github.ladysnake.impersonate.impl.RecipientAwareText;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TranslatableText.class)
public abstract class TranslatableTextMixin implements RecipientAwareText {
    @Shadow
    @Final
    private Object[] args;

    @Shadow
    public abstract Object[] getArgs();

    @Override
    public void impersonateResolve(CommandOutput recipient) {
        for (int i = 0; i < this.getArgs().length; i++) {
            Object arg = args[i];
            if (arg instanceof RecipientAwareText) {
                args[i] = ((RecipientAwareText) arg).impersonateResolveAll(recipient);
            }
        }
    }
}
