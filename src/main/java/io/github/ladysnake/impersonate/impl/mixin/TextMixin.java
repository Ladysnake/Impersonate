package io.github.ladysnake.impersonate.impl.mixin;

import io.github.ladysnake.impersonate.impl.RecipientAwareText;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Text.class)
public interface TextMixin extends RecipientAwareText {
    @Shadow
    Text deepCopy();

    @Override
    default void impersonateResolve(CommandOutput recipient) {
        // NO-OP
    }

    @Override
    default Text impersonateResolveAll(CommandOutput recipient) {
        Text copy = this.deepCopy();
        copy.stream().forEach(t -> ((RecipientAwareText) t).impersonateResolve(recipient));
        return copy;
    }
}
